package sut.momtsaber.clikecompiler.parser.dfa;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.parser.SyntaxError;

public class DFAResponse
{
    private boolean isEndOfDFA;
    private CFGTerminal consumedTerminal;
    private CFGNonTerminal referencing;
    private SyntaxError error;
    private boolean isGarbage;

    public DFAResponse(boolean isEndOfDFA, CFGTerminal consumedTerminal, CFGNonTerminal referencing, SyntaxError error, boolean isGarbage)
    {
        this.isEndOfDFA = isEndOfDFA;
        this.consumedTerminal = consumedTerminal;
        this.referencing = referencing;
        this.error = error;
        this.isGarbage = isGarbage;
    }

    public boolean isEndOfDFA()
    {
        return isEndOfDFA;
    }


    public CFGTerminal getConsumedTerminal()
    {
        return consumedTerminal;
    }

    public CFGNonTerminal getReferencing()
    {
        return referencing;
    }

    public SyntaxError getError()
    {
        return error;
    }

    public boolean isGarbage()
    {
        return isGarbage;
    }
}
