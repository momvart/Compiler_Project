package sut.momtsaber.clikecompiler.parser.dfa;

import java.util.List;

import sut.momtsaber.clikecompiler.cfg.CFGAction;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.parser.SyntaxError;

public class DFAResponse
{
    private boolean isEndOfDFA;
    private CFGTerminal consumedTerminal;
    private CFGNonTerminal referencing;
    private SyntaxError error;
    private List<CFGAction> actions;

    public DFAResponse(boolean isEndOfDFA, CFGTerminal consumedTerminal, CFGNonTerminal referencing, SyntaxError error, List<CFGAction> action)
    {
        this.isEndOfDFA = isEndOfDFA;
        this.consumedTerminal = consumedTerminal;
        this.referencing = referencing;
        this.error = error;
        this.actions = action;
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

    public List<CFGAction> getActions()
    {
        return actions;
    }
}
