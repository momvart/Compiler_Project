package sut.momtsaber.clikecompiler.parser.dfa;

import java.util.*;

import sut.momtsaber.clikecompiler.cfg.*;
import sut.momtsaber.clikecompiler.dfa.DFAEdge;
import sut.momtsaber.clikecompiler.dfa.DFAState;
import sut.momtsaber.clikecompiler.dfa.Entrance;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;

public class DFA
{
    private ParseDFAState startState;
    private ParseDFAState acceptState;
    private ParseDFAState currentState;

    private DFA(ParseDFAState startState, ParseDFAState acceptState)
    {
        this.startState = startState;
        this.acceptState = acceptState;
        this.currentState = startState;
    }

    private ParseDFAState getStartState()
    {
        return startState;
    }

    private ParseDFAState getAcceptState()
    {
        return acceptState;
    }

    public ParseDFAState getCurrentState()
    {
        return currentState;
    }

    public DFAResponse advance(Token input)
    {
        DFAState.NextStateResult<Token> result = currentState.getNextState(input);
        if (result == null)
            System.out.println(input);
        currentState = (ParseDFAState)result.getNextState();
        return new DFAResponse(currentState.equals(acceptState), result.isConsuming(), currentState.getConsumed(), currentState.getReferencing());
    }

    public static DFA getDFA(CFGProduction production, CFG grammar)
    {
        DFA dfa = new DFA(new ParseDFAState(), new ParseDFAState());
        for (int i = 0; i < production.getRightHands().size(); i++)
        {
            CFGRule rule = production.getRightHands().get(i);
            // restarting the currentPosition in the beginning of the rule
            ParseDFAState currentPosition = dfa.getStartState();

            //calculating the entrance with which the input should go into this rule
            Entrance<Token> ruleEntrance;
            Set<CFGTerminal> matchList = grammar.findFirst(production, i);
            if (matchList.contains(CFGTerminal.EPSILON))
                matchList.addAll(grammar.findFollow(production.getLeftHand()));
            ruleEntrance = matchEntrance(matchList);

            if (rule.isEpsilon())
                currentPosition.addExitEdge(new DFAEdge<>(ruleEntrance, dfa.getAcceptState(), false));
            else
            {
                for (int iSymbol = 0; iSymbol < rule.size(); iSymbol++)
                {
                    CFGSymbol symbol = rule.get(iSymbol);
                    if (symbol instanceof CFGTerminal)
                    {
                        ParseDFAState newState;
                        if (iSymbol == rule.size() - 1)
                            newState = dfa.getAcceptState();
                        else
                            newState = new ParseDFAState();
                        currentPosition.addExitEdge(new DFAEdge<>(matchEntrance(Collections.singletonList((CFGTerminal)symbol)),
                                newState, true));
                        currentPosition = newState;
                        currentPosition.setConsumed((CFGTerminal)symbol);
                    }
                    else if (symbol instanceof CFGNonTerminal)
                    {
                        ParseDFAState intermediateState = new ParseDFAState();
                        ParseDFAState newState;
                        if (iSymbol == rule.size() - 1)
                            newState = dfa.getAcceptState();
                        else
                            newState = new ParseDFAState();

                        Entrance<? super Token> nonTerminalEntrance;
                        if (iSymbol == 0)
                            nonTerminalEntrance = ruleEntrance;
                        else
                            nonTerminalEntrance = Entrance.ANY;
                        currentPosition.addExitEdge(new DFAEdge<>(nonTerminalEntrance, intermediateState, false));
                        intermediateState.addExitEdge(new DFAEdge<>(Entrance.ANY, newState, false));
                        intermediateState.setReferencing((CFGNonTerminal)symbol);
                        currentPosition = newState;
                    }
                }
            }
        }
        return dfa;
    }

    private static Entrance<Token> matchEntrance(Collection<CFGTerminal> pattern)
    {
        return input -> pattern.stream().anyMatch(member -> member.getTokenType() == input.getType() &&
                (member.getValue() == null || member.getValue().equals(input.getValue())));
    }
}
