package sut.momtsaber.clikecompiler.lexicalanalysis;

import java.util.Arrays;
import java.util.List;

public class DFAState
{
    public static final DFAState SELF = new DFAState();

    private TokenType tokenType;
    private List<DFAEdge> exitingEdges;

    public DFAState()
    { }

    public DFAState(TokenType tokenType, DFAEdge... edges)
    {
        this.tokenType = tokenType;
        this.exitingEdges = Arrays.asList(edges);
    }

    public TokenType getTokenType()
    {
        return tokenType;
    }

    public void setExitingEdges(DFAEdge... exitingEdges)
    {
        this.exitingEdges = Arrays.asList(exitingEdges);
    }

    public DFAState getNextState(char c)
    {
        return exitingEdges.stream()
                .filter(edge -> edge.getEntrance().canEnter(c))
                .findFirst()
                .map(DFAEdge::getNextState)
                .map(state -> state == SELF ? DFAState.this : state)
                .orElse(null);
    }

    public static void main(String[] args)
    {
        System.out.println("this // this is a single line comment \n if /*hello and welcome to you all this is john champion in the commentary box */ that ".length());
    }
}
