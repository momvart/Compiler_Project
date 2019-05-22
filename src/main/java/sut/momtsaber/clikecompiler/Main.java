package sut.momtsaber.clikecompiler;


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

    }
}
