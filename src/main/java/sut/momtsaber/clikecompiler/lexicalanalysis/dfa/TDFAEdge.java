package sut.momtsaber.clikecompiler.lexicalanalysis.dfa;

import sut.momtsaber.clikecompiler.dfa.*;

public class TDFAEdge extends DFAEdge<Character>
{
    public TDFAEdge(Entrance<? super Character> entrance, DFAState<Character> nextState)
    {
        super(entrance, nextState, false);
    }
}
