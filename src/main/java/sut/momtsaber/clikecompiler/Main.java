package sut.momtsaber.clikecompiler;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.StreamSupport;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGRule;
import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenizeContext;
import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.ReaderCharacterProvider;
import sut.momtsaber.clikecompiler.parser.ParseContext;
import sut.momtsaber.clikecompiler.parser.tree.ParseTree;
import sut.momtsaber.clikecompiler.utils.CFGSymbolDeserializer;

public class Main
{
    public static void main(String[] args)
    {
        String in, out, err;
        if (args.length >= 1)
            in = args[0];
        else
            in = "input.txt";
        if (args.length >= 2)
            out = args[1];
        else
            out = "scanner.txt";
        if (args.length >= 3)
            err = args[2];
        else
            err = "lexical_errors.txt";

        parse(in, out, err);
    }

    public static void parse(String inputPath, String outputPath, String errorPath)
    {
        try (InputStream input = /*new FileInputStream(inputPath)*/ System.in;
             OutputStream output = /*new FileOutputStream(outputPath, false)*/ System.out;
             OutputStream error = /*new FileOutputStream(errorPath, false)*/ System.err)
        {
            parse(input, output, error);
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    public static void parse(InputStream input, OutputStream output, OutputStream error) throws InterruptedException
    {
        PrintStream outPrinter = new PrintStream(output),
                errPrinter = new PrintStream(error);

        BlockingQueue<Token> pipeline = new LinkedBlockingQueue<>(20);

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
                    Token token = context.getNextToken();

                    switch (token.getType())
                    {
                        case COMMENT:
                        case WHITESPACE:
                            break;
                        case INVALID:
                            errPrinter.printf("%d. (%s, invalid input)%n", outputLineNum, token.getValue());
                            break;
                        default:
                            pipeline.put(token);
                            break;
                    }
                    outputLineNum = context.getCurrentLineNumber();
                }
                pipeline.put(new Token(TokenType.EOF, null));
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }, "Scanner");
        scannerThread.start();

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

                ParseContext context = new ParseContext(grammar);
                ParseTree outTree = context.parse(pipeline);

                outPrinter.println(outTree.toHumanReadableString(grammar));
                outPrinter.println(outTree.toInorderString());
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }, "Parser");

        parserThread.start();

        scannerThread.join();
        parserThread.join();
    }
}
