package sut.momtsaber.clikecompiler.errors;

public class CompileError extends RuntimeException
{
    private int lineNumber;
    private final String message;

    public CompileError(int lineNumber, String message)
    {
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public String getName() {return "Compile Error";}

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getMessage()
    {
        return message;
    }
}
