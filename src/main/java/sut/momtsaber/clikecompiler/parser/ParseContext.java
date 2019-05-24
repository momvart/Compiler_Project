package sut.momtsaber.clikecompiler.parser;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenWithLineNum;
import sut.momtsaber.clikecompiler.parser.dfa.DFA;
import sut.momtsaber.clikecompiler.parser.dfa.DFAResponse;
import sut.momtsaber.clikecompiler.parser.tree.ParseTree;

public class ParseContext
{
    private CFG grammar;
    private Map<Integer, DFA> DFAs;
    private DFA currentDFA;
    private Stack<DFA> dfaStack;

    public ParseContext(CFG grammar)
    {
        init(grammar);
    }

    private void init(CFG cfg)
    {
        grammar = cfg;
        DFAs = grammar.getProductions().keySet().stream()
                .map(key -> grammar.getProductions().get(key))
                .map(production -> new DFA(production, grammar))
                .collect(Collectors.toMap(dfa -> dfa.getProduction().getLeftHand().getId(), dfa -> dfa));
        currentDFA = new DFA(cfg.getProduction(0), grammar);
        dfaStack = new Stack<>();
        dfaStack.push(currentDFA);
    }

    public ParseTree parse(BlockingQueue<TokenWithLineNum> tokens, BlockingQueue<? super SyntaxError> errors) throws InterruptedException
    {
        Stack<ParseTree> treeStack = new Stack<>();
        ParseTree mainTree = new ParseTree(grammar.getProductions().get(0).getLeftHand());
        treeStack.push(mainTree);
        TokenWithLineNum currentToken = null;
        while (!dfaStack.isEmpty())
        {
            currentDFA = dfaStack.peek();
            DFAResponse response;
            if (currentToken == null)
            {
                do
                {
                    currentToken = tokens.take();
                }
                while (currentToken.getType() == TokenType.COMMENT || currentToken.getType() == TokenType.WHITESPACE);
            }
            response = currentDFA.advance(currentToken, grammar);
            if (response.getError() != null)
            {
                errors.put(response.getError());
            }
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
                treeStack.peek().addTerminal(response.getConsumedTerminal(), new Token(response.getConsumedTerminal().getTokenType(), response.getConsumedTerminal().getValue()));//todo should be made the token corresponding by the symbol
                if (response.getError() == null)
                    currentToken = null;
            }
            if (response.isGarbage())
                currentToken = null;
            if (response.isEndOfDFA())
            {
                dfaStack.pop();
                treeStack.pop();
            }
        }
        return mainTree;
    }
}
