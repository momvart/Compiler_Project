package sut.momtsaber.clikecompiler.dfa;

import java.util.Arrays;
import java.util.List;

public class DFAState<T>
{
    public static final DFAState SELF = new DFAState();

    private List<DFAEdge<T>> exitingEdges;

    public DFAState()
    { }

    public DFAState(DFAEdge<T>... edges)
    {
        this.exitingEdges = Arrays.asList(edges);
    }

    public void setExitingEdges(DFAEdge<T>... exitingEdges)
    {
        this.exitingEdges = Arrays.asList(exitingEdges);
    }

    public NextStateResult<T> getNextState(T input)
    {
        return exitingEdges.stream()
                .filter(edge -> edge.getEntrance().canEnter(input))
                .findFirst()
                .map(edge -> new NextStateResult<>(
                        edge.getNextState() == SELF ? DFAState.this : edge.getNextState(),
                        edge.isConsuming()))
                .orElse(null);
    }

    public static class NextStateResult<T>
    {
        private final DFAState<T> nextState;
        private final boolean consuming;

        public NextStateResult(DFAState<T> nextState, boolean consuming)
        {
            this.nextState = nextState;
            this.consuming = consuming;
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
}
