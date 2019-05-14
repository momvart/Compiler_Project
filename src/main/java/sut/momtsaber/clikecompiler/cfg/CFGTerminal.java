package sut.momtsaber.clikecompiler.cfg;

import java.util.Objects;

import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;

public class CFGTerminal extends CFGSymbol
{
    private final TokenType tokenType;
    private final String value;

    private CFGTerminal(TokenType tokenType, String value)
    {
        this.tokenType = tokenType;
        this.value = value;
    }

    public static CFGTerminal parse(String raw)
    {
        TokenType type;
        String value = null;
        try { type = TokenType.valueOf(raw.toUpperCase()); }
        catch (Exception ignored)
        {
            if (Token.KEYWORDS.contains(raw))
                type = TokenType.KEYWORD;
            else if (Token.SYMBOLS.contains(raw))
                type = TokenType.SYMBOL;
            else
                throw new IllegalArgumentException("No token type found for: " + raw);

            value = raw;
        }

        return new CFGTerminal(type, value);
    }


    public TokenType getTokenType()
    {
        return tokenType;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CFGTerminal)) return false;
        CFGTerminal that = (CFGTerminal)o;
        return getTokenType() == that.getTokenType() &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getTokenType(), getValue());
    }

    @Override
    public String toString()
    {
        return value == null ? tokenType == null ? "EPS" : tokenType.toString() : value;
    }
}
