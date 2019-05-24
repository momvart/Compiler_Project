package sut.momtsaber.clikecompiler.parser;

public enum SyntaxErrorType
{
    UNEXPECTED("Unexpected"),
    MISSING("Missing");

    private String text;

    SyntaxErrorType(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}
