package sut.momtsaber.clikecompiler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.parser.tree.ParseTree;

public class ParseContext
{
    private CFG grammar;
    private List<DFA> DFAs;
    private DFA currentDFA;
    private Stack<DFA> dfaStack;

    public List<DFA> getDFAs()
    {
        return DFAs;
    }

    public ParseContext(CFG grammar)
    {
        init(grammar);
    }

    public void init(CFG cfg)
    {
        grammar = cfg;
        DFAs = grammar.getProductions().keySet().stream().map(key -> grammar.getProductions().get(key)).map(DFA::getDFA).collect(Collectors.toList());
        currentDFA = DFA.getDFA(grammar.getProductions().get(0));
        dfaStack = new Stack<>();
        dfaStack.push(currentDFA);
    }

    public ParseTree parse(ArrayList<Token> parsingContent)
    {
        int currentToken = 0;
        Stack<ParseTree> treeStack = new Stack<>();
        ParseTree mainTree = new ParseTree(grammar.getProductions().get(0).getLeftHand());
        treeStack.push(mainTree);
        while (!dfaStack.isEmpty())
        {
            currentDFA = dfaStack.peek();
            DFAResponse response = currentDFA.advance(parsingContent.get(currentToken));
            if (response.isEndOfDFA())
            {
                dfaStack.pop();
                treeStack.pop();
            }
            if (response.getReferencing() != null)
            {
                int reference = response.getReferencing().getId();
                currentDFA.getCurrentState().setReferencing(null);
                dfaStack.push(DFA.getDFA(grammar.getProductions().get(reference)));
                ParseTree newTree = new ParseTree(grammar.getProductions().get(reference).getLeftHand());
                treeStack.peek().addNonTerminal(grammar.getProductions().get(reference).getLeftHand(), newTree);
                treeStack.push(newTree);
            }
            if (response.isConsumed())
            {
                treeStack.peek().addTerminal(response.getConsumedTerminal(), parsingContent.get(currentToken));
                currentToken++;
            }
        }
        return mainTree;
    }

}
