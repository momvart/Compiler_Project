package sut.momtsaber.clikecompiler.lexicalanalysis;

import sut.momtsaber.clikecompiler.errors.CompileError;

public class LexicalError extends CompileError
{
    private Token invalidToken;

    public LexicalError(int lineNumber, Token invalidToken)
    {
        super(lineNumber, "Invalid token: " + invalidToken);
    }

    public LexicalError(TokenWithLineNum invalidToken)
    {
        this(invalidToken.getLineNum(), invalidToken);
    }

    @Override
    public String getName()
    {
        return "Lexical Error";
    }
}
