package sut.momtsaber.clikecompiler.lexicalanalysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Token
{
    public static final String SYMBOLS = ";:,[](){}+-*==<";
    public static final Set<String> KEYWORDS = new HashSet<>(
            Arrays.asList("if", "else", "void", "int",
                    "while", "break", "continue", "switch",
                    "default", "case", "return"));

    private TokenType type;
    private String value;

    public Token(TokenType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    public TokenType getType()
    {
        return type;
    }

    void setType(TokenType type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "(" + this.type + ", " + this.value + ")";
    }
}
