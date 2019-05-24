package sut.momtsaber.clikecompiler.parser.dfa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    private CFGProduction production;   //for debug purpose
    private DFAState<Token> startState;
    private DFAState<Token> acceptState;
    private DFAState<Token> currentState;
    private Map<DFAState<Token>, CFGNonTerminal> trueReferenceMap;
    private Map<DFAState<Token>, CFGNonTerminal> currentReferenceMap;
    private Map<DFAEdge<Token>, CFGTerminal> consumptionMap;

    private DFA(CFGProduction production)
    {
        this.production = production;
        this.startState = new DFAState<>();
        this.acceptState = new DFAState<>();
        this.trueReferenceMap = new HashMap<>();
        this.consumptionMap = new HashMap<>();
    }

    public DFA(CFGProduction production, CFG grammar)
    {
        DFA dfa = getDFA(production, grammar);
        this.production = dfa.production;
        this.startState = dfa.getStartState();
        this.acceptState = dfa.getAcceptState();
        this.currentState = this.getStartState();
        this.currentReferenceMap = new HashMap<>(dfa.trueReferenceMap);
        this.trueReferenceMap = new HashMap<>(dfa.trueReferenceMap);
        this.consumptionMap = new HashMap<>(dfa.consumptionMap);
    }

    public DFA(DFA pattern)
    {
        this.production = pattern.production;
        this.startState = pattern.getStartState();
        this.acceptState = pattern.getAcceptState();
        this.currentState = this.getStartState();
        this.currentReferenceMap = new HashMap<>(pattern.trueReferenceMap);
        this.trueReferenceMap = new HashMap<>(pattern.trueReferenceMap);
        this.consumptionMap = new HashMap<>(pattern.consumptionMap);
    }

    public DFAResponse advance(Token input)
    {
        DFAState.NextStateResult<Token> result = currentState.getNextState(input);
        currentState = result.getNextState();
        return new DFAResponse(currentState.equals(acceptState),
                this.consumptionMap.get(result.getEdge()),
                this.currentReferenceMap.get(currentState));// what if not present in the hashMap?
    }

    public static DFA getDFA(CFGProduction production, CFG grammar)
    {
        DFA dfa = new DFA(production);
        for (int i = 0; i < production.getRightHands().size(); i++)
        {
            CFGRule rule = production.getRightHands().get(i);
            // restarting the currentPosition in the beginning of the rule
            DFAState<Token> currentPosition = dfa.getStartState();

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
                        DFAState<Token> newState;
                        if (iSymbol == rule.size() - 1)
                            newState = dfa.getAcceptState();
                        else
                            newState = new DFAState<>();

                        DFAEdge<Token> edge = new DFAEdge<>(matchEntrance(Collections.singletonList((CFGTerminal)symbol)),
                                newState, true);
                        currentPosition.addExitEdge(edge);
                        currentPosition = newState;
                        dfa.consumptionMap.put(edge, (CFGTerminal)symbol);
                        continue;
                    }
                    if (symbol instanceof CFGNonTerminal)
                    {
                        DFAState<Token> intermediateState = new DFAState<>();
                        DFAState<Token> newState;
                        if (iSymbol == rule.size() - 1)
                            newState = dfa.getAcceptState();
                        else
                            newState = new DFAState<>();

                        Entrance<? super Token> nonTerminalEntrance;
                        if (iSymbol == 0)
                            nonTerminalEntrance = ruleEntrance;
                        else
                            nonTerminalEntrance = Entrance.ANY;
                        currentPosition.addExitEdge(new DFAEdge<>(nonTerminalEntrance, intermediateState, false));
                        intermediateState.addExitEdge(new DFAEdge<>(Entrance.ANY, newState, false));
                        dfa.trueReferenceMap.put(intermediateState, (CFGNonTerminal)symbol);
                        currentPosition = newState;
                    }
                }
            }
        }
        return dfa;
    }

    public CFGProduction getProduction()
    {
        return production;
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

    public Map<DFAState<Token>, CFGNonTerminal> getCurrentReferenceMap()
    {
        return currentReferenceMap;
    }


    private static Entrance<Token> matchEntrance(Collection<CFGTerminal> pattern)
    {
        return input -> pattern.stream().anyMatch(member -> member.getTokenType() == input.getType() &&
                (member.getValue() == null || member.getValue().equals(input.getValue())));
    }
}
