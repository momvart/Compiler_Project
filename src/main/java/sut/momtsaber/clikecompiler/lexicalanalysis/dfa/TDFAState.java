package sut.momtsaber.clikecompiler.lexicalanalysis.dfa;

import sut.momtsaber.clikecompiler.dfa.DFAEdge;
import sut.momtsaber.clikecompiler.dfa.DFAState;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;

public class TDFAState extends DFAState<Character>
{
    private TokenType tokenType;

    public TDFAState()
    {
    }

    public TDFAState(TokenType tokenType, TDFAEdge... edges)
    {
        super(edges);
        this.tokenType = tokenType;
    }

    public TokenType getTokenType()
    {
        return tokenType;
    }

    public TDFAState getNextStateOld(Character input)
    {
        NextStateResult<Character> res = super.getNextState(input);
        return res == null ? null : (TDFAState)res.getNextState();
    }
}
