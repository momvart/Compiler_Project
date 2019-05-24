package sut.momtsaber.clikecompiler.parser;

public abstract class SyntaxError
{
    private int lineNumber;
    private SyntaxErrorType type;
    private String message;

    public SyntaxError(int lineNumber, SyntaxErrorType type, String message)
    {
        this.lineNumber = lineNumber;
        this.type = type;
        this.message = message;
    }

    public SyntaxError(int lineNumber, SyntaxErrorType type)
    {
        this.lineNumber = lineNumber;
        this.type = type;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    public SyntaxErrorType getType()
    {
        return type;
    }

    public void setType(SyntaxErrorType type)
    {
        this.type = type;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
