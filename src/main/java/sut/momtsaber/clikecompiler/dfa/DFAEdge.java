package sut.momtsaber.clikecompiler.dfa;

public class DFAEdge<T>
{
    private Entrance<? super T> entrance;
    private DFAState<T> nextState;
    private boolean consuming;

    public DFAEdge(Entrance<? super T> entrance, DFAState<T> nextState, boolean consuming)
    {
        this.entrance = entrance;
        this.nextState = nextState;
        this.consuming = consuming;
    }

    public Entrance<? super T> getEntrance()
    {
        return entrance;
    }

    public DFAState<T> getNextState()
    {
        return nextState;
    }

    public boolean isConsuming()
    {
        return consuming;
    }
}
