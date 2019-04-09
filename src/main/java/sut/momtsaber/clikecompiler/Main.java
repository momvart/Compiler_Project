package sut.momtsaber.clikecompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;

import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenizeContext;

public class Main
{
    public static void main(String[] args)
    {
        fn("/home/saber/IdeaProjects/Compiler_Project/src/main/java/sut/momtsaber/clikecompiler/lexicalanalysis/input.txt");
    }

    private static void fn(String fileName)
    {
        try
        {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            TokenizeContext context = new TokenizeContext();
            context.resetText(str);
            FileWriter fw = new FileWriter("/home/saber/IdeaProjects/Compiler_Project/src/main/java/sut/momtsaber/clikecompiler/lexicalanalysis/output.txt");
            PrintWriter writer = new PrintWriter(fw);
            int line = 1;
            int actualLine = 0;
            boolean first = true;
            while (context.hasNextToken())
            {
                Token token = context.getNextToken();
                if((token.getType() == TokenType.COMMENT && token.getValue().charAt(token.getValue().length() - 1) == '\n') ||
                        (token.getType() == TokenType.WHITESPACE && token.getValue().chars().anyMatch(c-> c == '\n')))
                    line++;

                if (token.getType() != TokenType.WHITESPACE && token.getType() != TokenType.COMMENT)
                {
                    if (actualLine != line)
                    {
                        if (first)
                        {
                            writer.print(line + ": ");
                            first = false;
                        }
                        else
                            writer.print("\n"+ line + ". ");
                        actualLine = line;
                    }
                    writer.print(token.toString() + " ");
                }
            }
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void scan(InputStream input, OutputStream output, OutputStream error)
    {
        TokenizeContext context = new TokenizeContext();
        Scanner scanner = new Scanner(input);
        PrintStream tokensWriter = new PrintStream(output);
        PrintStream errorsWriter = new PrintStream(error);
        int lineNumber = 1;
        while (scanner.hasNextLine())
        {
            context.resetText(scanner.nextLine());

            StringBuilder validTokens = new StringBuilder();
            StringBuilder invalidTokens = new StringBuilder();
            Token token;
            while (context.hasNextToken())
                switch ((token = context.getNextToken()).getType())
                {
                    case COMMENT:
                    case WHITESPACE:
                        continue;
                    case INVALID:
                        invalidTokens.append('(').append(token.getValue()).append(", ")
                                .append("invalid input").append(") ");
                        break;
                    default:
                        validTokens.append(token.toString()).append(" ");
                        break;
                }

            writeTokensIfNotEmpty(tokensWriter, lineNumber, validTokens.toString());
            writeTokensIfNotEmpty(errorsWriter, lineNumber, invalidTokens.toString());
            lineNumber++;
        }
    }


    private static void writeTokensIfNotEmpty(PrintStream out, int lineNumber, String tokensList)
    {
        if (!tokensList.isEmpty())
            out.printf("%d. %s\n", lineNumber, tokensList);
    }
}
