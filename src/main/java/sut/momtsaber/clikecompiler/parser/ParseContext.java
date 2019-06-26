package sut.momtsaber.clikecompiler.parser;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import sut.momtsaber.clikecompiler.cfg.*;
import sut.momtsaber.clikecompiler.lexicalanalysis.*;
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
            // read a valuable token when we haven't got one
            if (currentToken == null)
            {
                do
                {
                    currentToken = tokens.take();
                }
                while (currentToken.getType() == TokenType.COMMENT || currentToken.getType() == TokenType.WHITESPACE);
            }
            response = currentDFA.advance(currentToken, grammar);
            // put error in error data set if present and finish the job while encountering a terminating error type
            if (response.getError() != null)
            {
                errors.put(response.getError());
                if (response.getError().getType() == SyntaxErrorType.UNEXPECTED_END_OF_FILE || response.getError().getType() == SyntaxErrorType.MALFORMED_INPUT)
                    break;
            }
            // switch into a new dfa when we reach a Noneterminal State and go into it's dfa
            if (response.getReferencing() != null)
            {
                int reference = response.getReferencing().getId();
                currentDFA.getCurrentReferenceMap().put(currentDFA.getCurrentState(), null);
                dfaStack.push(new DFA(DFAs.get(reference)));
                ParseTree newTree = new ParseTree(grammar.getProductions().get(reference).getLeftHand());
                treeStack.peek().addNonTerminal(grammar.getProductions().get(reference).getLeftHand(), newTree);
                treeStack.push(newTree);
            }
            //update tree when a terminal is used or if it is missing but should have been used if it was present.
            if (response.getConsumedTerminal() != null)
            {
                if (response.getError() == null)
                    treeStack.peek().addTerminal(response.getConsumedTerminal(), currentToken);
                else
                    treeStack.peek().addTerminal(response.getConsumedTerminal(), new Token(response.getConsumedTerminal().getTokenType(), response.getConsumedTerminal().getValue()));
            }
            if (response.isEndOfDFA())
            {
                dfaStack.pop();
                treeStack.pop();
            }
            // if we find a token unexpected or used up we should go to the next token
            if (gotUnexpected(response.getError()) || (response.getConsumedTerminal() != null && response.getError() == null))
            {
                currentToken = null;
            }
        }
        errors.put(new SyntaxError(-1, SyntaxErrorType.MALFORMED_INPUT, new CFGTerminal(null, null)));
        return mainTree;
    }

    private boolean gotUnexpected(SyntaxError error)
    {
        return (error != null && (error.getType() == SyntaxErrorType.MALFORMED_INPUT || error.getType() == SyntaxErrorType.UNEXPECTED));
    }
}
