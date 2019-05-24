package sut.momtsaber.clikecompiler.parser;

public enum SyntaxErrorType
{
    UNEXPECTED("Unexpected"),
    MISSING("Missing"),
    UNEXPECTED_END_OF_FILE("Unexpected endOfFile"),
    MALFORMED_INPUT("Malformed input");

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
