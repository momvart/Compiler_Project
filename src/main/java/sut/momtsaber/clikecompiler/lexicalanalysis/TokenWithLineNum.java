package sut.momtsaber.clikecompiler.lexicalanalysis;

public class TokenWithLineNum extends Token
{
    private int lineNum;

    public TokenWithLineNum(Token token)
    {
        this(token.getType(), token.getValue(), -1);
    }

    public TokenWithLineNum(TokenType type, String value, int lineNum)
    {
        super(type, value);
        this.lineNum = lineNum;
    }

    public TokenWithLineNum(Token token, int lineNum)
    {
        this(token.getType(), token.getValue(), lineNum);
    }

    public int getLineNum()
    {
        return lineNum;
    }
}
