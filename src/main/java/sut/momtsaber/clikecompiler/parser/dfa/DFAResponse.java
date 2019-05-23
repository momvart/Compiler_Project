package sut.momtsaber.clikecompiler.parser.dfa;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;

public class DFAResponse
{
    private boolean isEndOfDFA;
    private CFGTerminal consumedTerminal;
    private CFGNonTerminal referencing;

    public DFAResponse(boolean isEndOfDFA, CFGTerminal consumedTerminal, CFGNonTerminal referencing)
    {
        this.isEndOfDFA = isEndOfDFA;
        this.consumedTerminal = consumedTerminal;
        this.referencing = referencing;
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
}
