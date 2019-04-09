package sut.momtsaber.clikecompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            TokenizeContext context = new TokenizeContext();
            context.resetText(str);
            FileWriter fw = new FileWriter("/home/saber/IdeaProjects/Compiler_Project/src/main/java/sut/momtsaber/clikecompiler/lexicalanalysis/output.txt");
            PrintWriter writer = new PrintWriter(fw);
            while (context.hasNextToken())
            {
                Token token = context.getNextToken();
                if(token.getType() != TokenType.WHITESPACE && token.getType() != TokenType.COMMENT)
                writer.print(token.toString() + " ");
                if (context.endOfLine)
                    writer.print("\n");
            }
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
