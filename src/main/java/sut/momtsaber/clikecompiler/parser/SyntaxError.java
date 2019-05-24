package sut.momtsaber.clikecompiler.parser;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;

public class SyntaxError
{
    private int lineNumber;
    private SyntaxErrorType type;
    private String message;
    private CFGNonTerminal nonTerminal;

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

    public SyntaxErrorType getType()
    {
        return type;
    }

    public String getMessage(CFG grammar)
    {
        if (message == null)
        {
            return grammar.getProductions().get(nonTerminal.getId()).getRightHands().get(0).toString();
        }
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public CFGNonTerminal getNonTerminal()
    {
        return nonTerminal;
    }

    public void setNonTerminal(CFGNonTerminal nonTerminal)
    {
        this.nonTerminal = nonTerminal;
    }
}
