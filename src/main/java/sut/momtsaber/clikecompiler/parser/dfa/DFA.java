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
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenWithLineNum;
import sut.momtsaber.clikecompiler.parser.SyntaxError;
import sut.momtsaber.clikecompiler.parser.SyntaxErrorType;

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

    public DFAResponse advance(TokenWithLineNum input, CFG grammar)
    {
        DFAState.NextStateResult<Token> result = currentState.getNextState(input);
        if (result == null)
        {
            DFAEdge<Token> edge = currentState.getExitingEdges().get(0);
            if (input.getType() == TokenType.EOF)
            {
                return new DFAResponse(false, null, null,
                        new SyntaxError(input.getLineNum(), SyntaxErrorType.UNEXPECTED_END_OF_FILE, new Token(TokenType.EOF, null)));
            }

            // error on terminal edge
            if (consumptionMap.get(edge) != null)
            {
                if (consumptionMap.get(edge).getTokenType() == TokenType.EOF)
                {
                    return new DFAResponse(false, null, null,
                            new SyntaxError(input.getLineNum(), SyntaxErrorType.MALFORMED_INPUT, input));
                }
                currentState = edge.getNextState();
                return new DFAResponse(currentState.equals(acceptState), consumptionMap.get(edge), currentReferenceMap.get(currentState),
                        new SyntaxError(input.getLineNum(), SyntaxErrorType.MISSING, consumptionMap.get(edge)));
            }

            // error on nonTerminal edge
            else if (currentReferenceMap.get(edge.getNextState()) != null)
            {
                if (matchEntrance(grammar.findFollow(currentReferenceMap.get(edge.getNextState()))).canEnter(input))
                {
                    currentState = edge.getNextState().getExitingEdges().get(0).getNextState();
                    SyntaxError error = new SyntaxError(input.getLineNum(), SyntaxErrorType.MISSING, currentReferenceMap.get(edge.getNextState()), grammar);
                    return new DFAResponse(currentState.equals(acceptState), null, null, error);
                }
                else
                {
                    // putting unexpected tokens into garbage
                    return new DFAResponse(currentState.equals(acceptState), null, null,
                            new SyntaxError(input.getLineNum(), SyntaxErrorType.UNEXPECTED, input));
                }
            }
            // it is not possible to get into this section because we have an any Entrance on the other edges and it always matches and never gets into null result
            else
                return null;
        }
        else
        {
            currentState = result.getNextState();
            return new DFAResponse(currentState.equals(acceptState), this.consumptionMap.get(result.getEdge()), this.currentReferenceMap.get(currentState), null);
        }
    }

    public static DFA getDFA(CFGProduction production, CFG grammar)
    {
        DFA dfa = new DFA(production);
        for (int i = 0; i < production.getRightHands().size(); i++)
        {
            CFGRule rule = production.getRightHands().get(i);
            // restarting the currentPosition in the beginning of the rule
            DFAState<Token> buildingTail = dfa.getStartState();

            //calculating the entrance with which the input should go into this rule
            Entrance<Token> ruleEntrance;
            Set<CFGTerminal> matchList = grammar.findFirst(production, i);
            if (matchList.contains(CFGTerminal.EPSILON))
                matchList.addAll(grammar.findFollow(production.getLeftHand()));
            ruleEntrance = matchEntrance(matchList);

            if (rule.isEpsilon())
                buildingTail.addExitEdge(new DFAEdge<>(ruleEntrance, dfa.getAcceptState(), false));
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
                        buildingTail.addExitEdge(edge);
                        buildingTail = newState;
                        dfa.consumptionMap.put(edge, (CFGTerminal)symbol);
                    }
                    else if (symbol instanceof CFGNonTerminal)
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
                        {
                            Set<CFGTerminal> firstSet = grammar.findFirst(symbol);
                            if (firstSet.contains(CFGTerminal.EPSILON))
                                nonTerminalEntrance = Entrance.ANY;
                            else
                                nonTerminalEntrance = matchEntrance(firstSet);
                        }
                        buildingTail.addExitEdge(new DFAEdge<>(nonTerminalEntrance, intermediateState, false));
                        intermediateState.addExitEdge(new DFAEdge<>(Entrance.ANY, newState, false));
                        dfa.trueReferenceMap.put(intermediateState, (CFGNonTerminal)symbol);
                        buildingTail = newState;
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

    public DFAState<Token> getAcceptState()
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
