package sut.momtsaber.clikecompiler.parser.dfa;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;

public class DFAResponse
{
    private boolean isEndOfDFA;
    private boolean isConsumed;
    private CFGTerminal consumedTerminal;
    private CFGNonTerminal referencing;

    public DFAResponse(boolean isEndOfDFA, boolean isConsumed, CFGTerminal consumedTerminal, CFGNonTerminal referencing)
    {
        this.isEndOfDFA = isEndOfDFA;
        this.isConsumed = isConsumed;
        this.consumedTerminal = consumedTerminal;
        this.referencing = referencing;
    }

    public boolean isEndOfDFA()
    {
        return isEndOfDFA;
    }

    public boolean isConsumed()
    {
        return isConsumed;
    }

    public CFGTerminal getConsumedTerminal()
    {
        return consumedTerminal;
    }

    public CFGNonTerminal getReferencing()
    {
        return referencing;
    }
}
