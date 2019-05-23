package sut.momtsaber.clikecompiler.dfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;

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

    public void addExitEdge(DFAEdge<T> exitEdge)
    {
        if (exitingEdges == null)
            exitingEdges = new ArrayList<>();
        this.exitingEdges.add(exitEdge);
    }

    public NextStateResult<T> getNextState(T input)
    {
        return exitingEdges.stream()
                .filter(edge -> edge.getEntrance().canEnter(input))
                .findFirst()
                .map(edge -> new NextStateResult<>(
                        edge,
                        edge.getNextState() == SELF ? DFAState.this : edge.getNextState(),
                        edge.isConsuming()))
                .orElse(null);
    }

    public static class NextStateResult<T>
    {
        private final DFAEdge<T> edge;
        private final DFAState<T> nextState;
        private final boolean consuming;

        public NextStateResult(DFAEdge<T> edge, DFAState<T> nextState, boolean consuming)
        {
            this.edge = edge;
            this.nextState = nextState;
            this.consuming = consuming;
        }

        public DFAEdge<T> getEdge()
        {
            return edge;
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
