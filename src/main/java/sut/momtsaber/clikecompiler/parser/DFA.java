package sut.momtsaber.clikecompiler.parser;

import java.util.Set;

import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGProduction;
import sut.momtsaber.clikecompiler.cfg.CFGRule;
import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.dfa.DFAEdge;
import sut.momtsaber.clikecompiler.dfa.DFAState;
import sut.momtsaber.clikecompiler.dfa.Entrance;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.utils.FirstFollowProducer;

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

    static DFA getDFA(CFGProduction production)
    {
        DFA dfa = new DFA(new DFAState<>(), new DFAState<>());
        CFGNonTerminal nonTerminal = production.getLeftHand();
        for (CFGRule rule : production.getRightHands())
        {
            DFAState<Token> currentPosition = dfa.getStartState();
            if (rule.isEpsilon())
            {
                Set<CFGTerminal> followSet = FirstFollowProducer.findFollow(nonTerminal);
                Entrance<Token> epsilonEntrance = input -> followSet.stream().anyMatch(follow -> follow.getTokenType() == input.getType() &&
                        (!(follow.getTokenType() == TokenType.KEYWORD || follow.getTokenType() == TokenType.SYMBOL) || follow.getValue().equals(input.getValue())));
                currentPosition.addExitEdge(new DFAEdge<>(epsilonEntrance, dfa.getAcceptState(), false));
            }
            else
            {
                for (CFGSymbol symbol : rule)
                {
                    boolean isLast = false;
                    if (rule.indexOf(symbol) == rule.size() - 1)
                        isLast = true;
                    if (symbol instanceof CFGTerminal)
                    {
                        DFAState<Token> newState;
                        if (isLast)
                            newState = dfa.getAcceptState();
                        else
                            newState = new DFAState<>();
                        currentPosition.addExitEdge(new DFAEdge<>(input -> input.getType() == ((CFGTerminal)symbol).getTokenType() &&
                                (!(input.getType() == TokenType.SYMBOL || input.getType() == TokenType.KEYWORD) || ((CFGTerminal)symbol).getValue().equals(input.getValue())),
                                newState, true));
                        currentPosition = newState;
                        currentPosition.setConsumed((CFGTerminal)symbol);
                        continue;
                    }
                    if (symbol instanceof CFGNonTerminal)
                    {
                        DFAState<Token> intermediateState = new DFAState<>();
                        DFAState<Token> newState;
                        if (isLast)
                            newState = dfa.getAcceptState();
                        else
                            newState = new DFAState<>();
                        currentPosition.addExitEdge(new DFAEdge<>(Entrance.any, intermediateState, false));
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
