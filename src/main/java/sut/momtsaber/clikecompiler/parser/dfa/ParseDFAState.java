package sut.momtsaber.clikecompiler.parser.dfa;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.dfa.DFAState;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;

public class ParseDFAState extends DFAState<Token>
{
    private CFGNonTerminal referencing;
    private CFGTerminal consumed;

    public CFGNonTerminal getReferencing()
    {
        return referencing;
    }

    public void setReferencing(CFGNonTerminal referencing)
    {
        this.referencing = referencing;
    }

    public CFGTerminal getConsumed()
    {
        return consumed;
    }

    public void setConsumed(CFGTerminal consumed)
    {
        this.consumed = consumed;
    }
}
