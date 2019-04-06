package sut.momtsaber.clikecompiler.lexicalanalysis;

public class DFAEdge
{
    private Entrance entrance;
    private DFAState nextState;

    public DFAEdge(Entrance entrance, DFAState nextState)
    {
        this.entrance = entrance;
        this.nextState = nextState;
    }

    public Entrance getEntrance()
    {
        return entrance;
    }

    public DFAState getNextState()
    {
        return nextState;
    }
}
