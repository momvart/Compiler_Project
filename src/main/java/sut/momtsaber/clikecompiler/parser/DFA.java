package sut.momtsaber.clikecompiler.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGProduction;
import sut.momtsaber.clikecompiler.cfg.CFGRule;
import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.dfa.DFAEdge;
import sut.momtsaber.clikecompiler.dfa.DFAState;
import sut.momtsaber.clikecompiler.dfa.Entrance;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;

public class DFA
{
    private DFAState<Token> startState;
    private DFAState<Token> acceptState;
    private DFAState<Token> currentState;

    private DFA(DFAState<Token> startState, DFAState<Token> acceptState)
    {
        this.startState = startState;
        this.acceptState = acceptState;
        this.currentState = startState;
    }

    private DFAState<Token> getStartState()
    {
        return startState;
    }

    private DFAState<Token> getAcceptState()
    {
        return acceptState;
    }

    public DFAState<Token> getCurrentState()
    {
        return currentState;
    }

    public DFAResponse advance(Token input)
    {
        DFAState.NextStateResult<Token> result = currentState.getNextState(input);
        if (result == null)
            System.out.println(input);
        currentState = result.getNextState();
        return new DFAResponse(currentState.equals(acceptState), result.isConsuming(), currentState.getConsumed(), currentState.getReferencing());
    }

    static DFA getDFA(CFGProduction production, CFG grammar)
    {
        DFA dfa = new DFA(new DFAState<>(), new DFAState<>());
        for (CFGRule rule : production.getRightHands())
        {
            // restarting the currentPosition in the beginning of the rule
            DFAState<Token> currentPosition = dfa.getStartState();

            //calculating the entrance with which the input should go into this rule
            Entrance<Token> ruleEntrance;
            Set<CFGTerminal> matchList = grammar.findFirst(new ArrayList<>(rule), null, true);
            if (matchList.contains(CFGTerminal.EPSILON))
                matchList.addAll(grammar.findFollow(production.getLeftHand()));
            ruleEntrance = Entrance.matches(matchList);

            if (rule.isEpsilon())
                currentPosition.addExitEdge(new DFAEdge<>(ruleEntrance, dfa.getAcceptState(), false));
            else
            {
                for (CFGSymbol symbol : rule)
                {
                    if (symbol instanceof CFGTerminal)
                    {
                        DFAState<Token> newState;
                        if (rule.indexOf(symbol) == rule.size() - 1)
                            newState = dfa.getAcceptState();
                        else
                            newState = new DFAState<>();
                        currentPosition.addExitEdge(new DFAEdge<>(Entrance.matches(new HashSet<>(Collections.singletonList((CFGTerminal)symbol))),
                                newState, true));
                        currentPosition = newState;
                        currentPosition.setConsumed((CFGTerminal)symbol);
                        continue;
                    }
                    if (symbol instanceof CFGNonTerminal)
                    {
                        DFAState<Token> intermediateState = new DFAState<>();
                        DFAState<Token> newState;
                        if (rule.indexOf(symbol) == rule.size() - 1)
                            newState = dfa.getAcceptState();
                        else
                            newState = new DFAState<>();

                        Entrance<Token> nonTerminalEntrance;
                        if (rule.indexOf(symbol) == 0)
                            nonTerminalEntrance = ruleEntrance;
                        else
                            nonTerminalEntrance = Entrance.any;
                        currentPosition.addExitEdge(new DFAEdge<>(nonTerminalEntrance, intermediateState, false));
                        intermediateState.addExitEdge(new DFAEdge<>(Entrance.any, newState, false));
                        intermediateState.setReferencing((CFGNonTerminal)symbol);
                        currentPosition = newState;
                    }
                }
            }
        }
        return dfa;
    }
}
