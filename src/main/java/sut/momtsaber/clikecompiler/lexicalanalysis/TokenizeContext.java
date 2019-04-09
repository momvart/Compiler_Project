package sut.momtsaber.clikecompiler.lexicalanalysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TokenizeContext
{
    private static final String SYMBOLS = ";:,[](){}+-*=<";
    private static final String TYPE_1_SYMBOLS = ";:,[](){}+-*<";
    private static final String TYPE_2_SYMBOLS = "=";
    private static final Set<String> KEYWORDS = new HashSet<>(
            Arrays.asList("if", "else", "void", "int",
                    "while", "break", "continue", "switch",
                    "default", "case", "return"));

    private String text;
    private int cursorStartPosition;
    private int currentReadPosition;

    private DFAState startState;
    private DFAState elseState;
    private DFAState currentState;

    public TokenizeContext()
    {
        init();
    }

    private void init()
    {
        Entrance symbolEntrance = Entrance.anyOf(SYMBOLS);
        Entrance type1SymbolEntrance = Entrance.anyOf(TYPE_1_SYMBOLS);
        Entrance type2SymbolEntrance = Entrance.anyOf(TYPE_2_SYMBOLS);
        
        this.startState = new DFAState();
        DFAEdge returnEdge = new DFAEdge(
                Entrance.or(Entrance.WHITESPACE,
                        Entrance.of('\0'),
                        symbolEntrance),
                startState);
        DFAState numberState = new DFAState(TokenType.NUMBER,
                new DFAEdge(Entrance.DIGIT, DFAState.SELF),
                returnEdge);
        DFAState idState = new DFAState(TokenType.ID,
                new DFAEdge(Entrance.LETTER_OR_DIGIT, DFAState.SELF),
                returnEdge);

        DFAState whiteSpaceState = new DFAState(TokenType.WHITESPACE,
                new DFAEdge(Entrance.WHITESPACE, DFAState.SELF),
                new DFAEdge(Entrance.negative(Entrance.WHITESPACE), startState));

        DFAState symbolTerminalState = new DFAState(TokenType.SYMBOL,
                new DFAEdge(Entrance.negative(symbolEntrance) , startState));

        DFAState symbolIntermediateState = new DFAState(TokenType.SYMBOL,
                new DFAEdge(type2SymbolEntrance, symbolTerminalState),
                new DFAEdge(Entrance.negative(symbolEntrance), startState)
        );

        this.startState.setExitingEdges(
                new DFAEdge(Entrance.DIGIT, numberState),
                new DFAEdge(Entrance.LETTER, idState),
                new DFAEdge(Entrance.WHITESPACE, whiteSpaceState),
                new DFAEdge(type1SymbolEntrance, symbolTerminalState),
                new DFAEdge(type2SymbolEntrance, symbolIntermediateState)
                );


        this.elseState = new DFAState(TokenType.INVALID, returnEdge);
        this.currentState = startState;
    }

    private boolean hasNext()
    {
        return currentReadPosition < text.length();
    }

    private char nextChar()
    {
        return text.charAt(++currentReadPosition);
    }

    char getCurrentChar() { return text.charAt(currentReadPosition); }

    public void resetText(String text)
    {
        this.text = text.concat("\0");
        this.cursorStartPosition = 0;
        this.currentReadPosition = -1;
    }

    public boolean hasNextToken()
    {
        return cursorStartPosition < text.length() - 1;
    }

    public Token getNextToken()
    {
        TokenType lastTokenType;
        do
        {
            lastTokenType = currentState.getTokenType();
            currentState = currentState.getNextState(nextChar());

            if (currentState == null)
                currentState = elseState;

        } //When we are back to start, a token is generated
        while (this.hasNext() && currentState != startState);

        Token retVal = new Token(lastTokenType, text.substring(cursorStartPosition, currentReadPosition));
        if (retVal.getType() == TokenType.ID && KEYWORDS.contains(retVal.getValue()))
            retVal.setType(TokenType.KEYWORD);

        cursorStartPosition = currentReadPosition;
        currentReadPosition--;          //Putting the pointer back for next the token
        return retVal;
    }
}
