package sut.momtsaber.clikecompiler;

import java.io.*;

import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenizeContext;
import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.ReaderCharacterProvider;

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
        scan(in, out, err);
    }

    public static void scan(String inputPath, String outputPath, String errorPath)
    {
        try (InputStream input = new FileInputStream(inputPath);
             OutputStream output = new FileOutputStream(outputPath, false);
             OutputStream error = new FileOutputStream(errorPath, false))
        {
            scan(input, output, error);
        }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    public static void scan(InputStream input, OutputStream output, OutputStream error)
    {
        int outputLineNum = 1;
        int prevOutputLineNum = 0;
        PrintStream outPrinter = new PrintStream(output),
                errPrinter = new PrintStream(error);

        TokenizeContext context = new TokenizeContext(new ReaderCharacterProvider(input));

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
                    errPrinter.printf("%d. (%s, invalid input)%s", outputLineNum, token.getValue(), System.lineSeparator());
                    break;
                default:
                    if (prevOutputLineNum != outputLineNum)
                    {
                        if (first)
                        {
                            outPrinter.printf("%d. ", outputLineNum);
                            first = false;
                        }
                        else
                            outPrinter.printf("%s%d. ", System.lineSeparator(), outputLineNum);
                        prevOutputLineNum = outputLineNum;
                    }
                    outPrinter.printf("%s ", token.toString());
                    break;
            }
            outputLineNum = context.getCurrentLineNumber();
        }

    }
}
