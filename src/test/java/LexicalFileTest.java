
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import sut.momtsaber.clikecompiler.Main;

public class LexicalFileTest
{
    @Test
    public void testSimple()
    {
        String inputString = "void main()\n\n{ int a = 10; }";
        String outputString = "", errorString = "";
        try (InputStream in = new ByteArrayInputStream(inputString.getBytes());
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayOutputStream err = new ByteArrayOutputStream())
        {
            Main.scan(in, out, err);
            outputString = out.toString();
            errorString = err.toString();
            //TODO: write some asserts
            System.out.println(outputString);
            System.err.println(errorString);
        }
        catch (IOException ignored)
        {

        }
    }

    @Test
    public void testExample()
    {
        String outputString = "", errorString = "";
        try (InputStream in = getClass().getResourceAsStream("input.txt");
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayOutputStream err = new ByteArrayOutputStream())
        {
            Main.scan(in, out, err);
            outputString = out.toString();
            errorString = err.toString();
            //TODO: write some asserts
            System.out.println(outputString);
            System.out.println("------Errors:");
            System.out.println(errorString);
        }
        catch (IOException ignored)
        {

        }
    }
}
