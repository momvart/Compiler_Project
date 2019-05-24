package sut.momtsaber.clikecompiler.parser;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.errors.CompileError;
import sut.momtsaber.clikecompiler.lexicalanalysis.Token;

public class SyntaxError extends CompileError
{
    private CFG grammar;
    private SyntaxErrorType type;
    private CFGSymbol symbol;

    public SyntaxError(int lineNumber, SyntaxErrorType type, CFGSymbol symbol, CFG grammar)
    {
        super(lineNumber, null);
        this.type = type;
        this.grammar = grammar;
        this.symbol = symbol;
    }

    public SyntaxError(int lineNumber, SyntaxErrorType type, CFGTerminal terminal)
    {
        this(lineNumber, type, terminal, null);
    }

    public SyntaxError(int lineNumber, SyntaxErrorType type, Token token)
    {
        this(lineNumber, type, new CFGTerminal(token.getType(), token.getValue()));
    }

    public SyntaxError(int lineNumber, SyntaxErrorType type, CFGNonTerminal nonTerminal, CFG grammar)
    {
        this(lineNumber, type, (CFGSymbol)nonTerminal, grammar);
    }

    @Override
    public String getName()
    {
        return "Syntax Error";
    }

    public SyntaxErrorType getType()
    {
        return type;
    }

    @Override
    public String getMessage()
    {
        String terminalPart = symbol instanceof CFGNonTerminal ?
                grammar.getProduction(((CFGNonTerminal)symbol).getId()).getRightHands().get(0).toString(grammar) :
                symbol.toString();
        return String.format("%s %s", type.getText(), terminalPart);
    }

}
