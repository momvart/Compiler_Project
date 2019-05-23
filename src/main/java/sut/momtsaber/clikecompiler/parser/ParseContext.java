package sut.momtsaber.clikecompiler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.parser.dfa.DFA;
import sut.momtsaber.clikecompiler.parser.dfa.DFAResponse;
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

    private void init(CFG cfg)
    {
        grammar = cfg;
        DFAs = grammar.getProductions().keySet().stream().map(key -> grammar.getProductions().get(key)).map(production -> new DFA(production, grammar)).collect(Collectors.toList());
        currentDFA = DFAs.get(0);
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
            if (response.getReferencing() != null)
            {
                int reference = response.getReferencing().getId();
                currentDFA.getCurrentReferenceMap().put(currentDFA.getCurrentState(), null);
                dfaStack.push(new DFA(DFAs.get(reference)));
                ParseTree newTree = new ParseTree(grammar.getProductions().get(reference).getLeftHand());
                treeStack.peek().addNonTerminal(grammar.getProductions().get(reference).getLeftHand(), newTree);
                treeStack.push(newTree);
            }
            if (response.getConsumedTerminal() != null)
            {
                treeStack.peek().addTerminal(response.getConsumedTerminal(), parsingContent.get(currentToken));
                currentToken++;
            }
            if (response.isEndOfDFA())
            {
                dfaStack.pop();
                treeStack.pop();
            }
        }
        return mainTree;
    }
}
