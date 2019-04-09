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
        scan("/home/saber/IdeaProjects/Compiler_Project/src/main/java/sut/momtsaber/clikecompiler/lexicalanalysis/input.txt" ,
                "/home/saber/IdeaProjects/Compiler_Project/src/main/java/sut/momtsaber/clikecompiler/lexicalanalysis/output.txt",
                "/home/saber/IdeaProjects/Compiler_Project/src/main/java/sut/momtsaber/clikecompiler/lexicalanalysis/error.txt");
    }


    public static void scan(String input, String output, String error)
    {
        StringBuilder validTokens = new StringBuilder();
        StringBuilder invalidTokens = new StringBuilder();
        try
        {
            File file = new File(input);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            TokenizeContext context = new TokenizeContext();
            context.resetText(str);
            int line = 1;
            int actualLine = 0;
            boolean first = true;

            while (context.hasNextToken())
            {
                Token token = context.getNextToken();
                if((token.getType() == TokenType.COMMENT && token.getValue().charAt(token.getValue().length() - 1) == '\n') ||
                        (token.getType() == TokenType.WHITESPACE && token.getValue().chars().anyMatch(c-> c == '\n')))
                    line++;

                switch (token.getType())
                {
                    case COMMENT:
                    case WHITESPACE:
                        continue;
                    case INVALID:
                        invalidTokens.append(line).append(". ").append('(').append(token.getValue()).append(", ")
                                .append("invalid input").append(")\n");
                        break;
                    default:
                        if (actualLine != line)
                        {
                            if (first)
                            {
                                validTokens.append(line + ": ");
                                first = false;
                            }
                            else
                                validTokens.append("\n" + line + ". ");
                            actualLine = line;
                        }
                        validTokens.append(token.toString() + " ");
                        break;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        writeTokensIfNotEmpty(output, validTokens.toString());
        writeTokensIfNotEmpty(error, invalidTokens.toString());
    }


    private static void writeTokensIfNotEmpty(String fileName, String content)
    {
        if (!content.isEmpty()){
            FileWriter fw = null;
            try
            {
                fw = new FileWriter(fileName);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            PrintWriter writer = new PrintWriter(fw);
            writer.print(content);
            writer.close();
        }
    }
}
