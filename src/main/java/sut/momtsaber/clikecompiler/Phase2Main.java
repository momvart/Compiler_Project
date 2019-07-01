package sut.momtsaber.clikecompiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import org.junit.Test;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.StreamSupport;

import sut.momtsaber.clikecompiler.cfg.*;
import sut.momtsaber.clikecompiler.codegen.CodeGenerationContext;
import sut.momtsaber.clikecompiler.errors.CompileError;
import sut.momtsaber.clikecompiler.lexicalanalysis.*;
import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.*;
import sut.momtsaber.clikecompiler.parser.ParseContext;
import sut.momtsaber.clikecompiler.parser.tree.ParseTree;
import sut.momtsaber.clikecompiler.utils.CFGSymbolDeserializer;

public class Phase2Main
{
    public static void main(String[] args) throws InterruptedException
    {
        String in, out, err;
        if (args.length >= 1)
            in = args[0];
        else
            in = "input.txt";
        if (args.length >= 2)
            out = args[1];
        else
            out = "output.txt";
        if (args.length >= 3)
            err = args[2];
        else
            err = "errors.txt";

        parse(System.in, System.out, System.err);
    }

    public static void parse(String inputPath, String outputPath, String errorPath)
    {
        try (InputStream input = new FileInputStream(inputPath);
             OutputStream output = new FileOutputStream(outputPath, false);
             OutputStream error = new FileOutputStream(errorPath, false))
        {
            parse(input, output, error);
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void parse(InputStream input, OutputStream output, OutputStream errStream) throws InterruptedException
    {
        PrintStream outPrinter = new PrintStream(output),
                errPrinter = new PrintStream(errStream);

        BlockingQueue<TokenWithLineNum> tokenPipe = new LinkedBlockingQueue<>(20);
        BlockingQueue<CompileError> errorPipe = new LinkedBlockingQueue<>();

        Thread scannerThread = new Thread(() ->
        {
            try
            {
                TokenizeContext context = new TokenizeContext(new ReaderCharacterProvider(input));

                int outputLineNum = 1;
                int prevOutputLineNum = 0;
                boolean first = true;

                while (context.hasNextToken())
                {
                    TokenWithLineNum token = context.getNextToken();

                    switch (token.getType())
                    {
                        case COMMENT:
                        case WHITESPACE:
                            break;
                        case INVALID:
                            errorPipe.put(new LexicalError(token));
                            break;
                        default:
                            tokenPipe.put(token);
                            break;
                    }
                    outputLineNum = context.getCurrentLineNumber();
                }
                tokenPipe.put(new TokenWithLineNum(TokenType.EOF, null, context.getCurrentLineNumber()));
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }, "Scanner");
        scannerThread.start();

        CodeGenerationContext cgContext = new CodeGenerationContext();

        Thread parserThread = new Thread(() ->
        {
            try
            {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(CFGSymbol.class, new CFGSymbolDeserializer())
                        .registerTypeAdapter(CFGRule.class, (JsonDeserializer<CFGRule>)(json, typeOfT, context) ->
                                new CFGRule(StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                                        .map(element -> context.deserialize(element, CFGSymbol.class))))
                        .create();
                CFG grammar = gson.fromJson(new InputStreamReader(Test.class.getClassLoader().getResourceAsStream("parsed_grammar.cfgjson")), CFG.class);

                ParseContext context = new ParseContext(grammar, cgContext);
                ParseTree outTree = context.parse(tokenPipe, errorPipe);

                errorPipe.put(new CompileError(-1, null));
                outPrinter.println(outTree.toHumanReadableString(grammar));
                outPrinter.println(outTree.toInorderString());
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }, "Parser");

        Thread errorThread = new Thread(() ->
        {
            try
            {
                CompileError err;
                while ((err = errorPipe.take()).getLineNumber() != -1)
                {
                    errPrinter.printf("%d: %s! %s%n", err.getLineNumber(), err.getName(), err.getMessage());
                }
            }
            catch (Exception ex) {ex.printStackTrace();}
        }, "Error Writer");
        errorThread.start();

        parserThread.start();

        scannerThread.join();
        parserThread.join();
        errorThread.join();

        cgContext.getCodeBlock().forEach(stmt -> System.out.println(stmt.toString()));
    }
}
