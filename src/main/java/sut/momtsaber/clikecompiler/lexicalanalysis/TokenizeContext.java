package sut.momtsaber.clikecompiler.lexicalanalysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class TokenizeContext
{
    private static final String SYMBOLS = ";:,[](){}+-*=<";
    private static final String TYPE_1_SYMBOLS = ";:,[](){}+-*<";
    private static final String TYPE_2_SYMBOLS = "=";
    private static final Character slash = '/';
    public boolean endOfLine = false;
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

        Entrance singCommentContEntr = Entrance.negative(Entrance.anyOf("\n\0"));
        Entrance multiCommentContEntr = Entrance.negative(Entrance.anyOf("*"));

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
                new DFAEdge(Entrance.ANY , startState));

        DFAState symbolIntermediateState = new DFAState(TokenType.SYMBOL,
                new DFAEdge(type2SymbolEntrance, symbolTerminalState),
                new DFAEdge(Entrance.ANY, startState)
        );

        DFAState commentTerminalState = new DFAState(TokenType.COMMENT,
                new DFAEdge(Entrance.ANY, startState));
        DFAState commentEndStarState = new DFAState(TokenType.COMMENT);

        DFAState multiLineCommentContent = new DFAState(TokenType.COMMENT,
            new DFAEdge(multiCommentContEntr, DFAState.SELF),
            new DFAEdge(Entrance.STAR, commentEndStarState)
        );

        commentEndStarState.setExitingEdges(
                new DFAEdge(Entrance.SLASH, commentTerminalState),
                new DFAEdge(Entrance.STAR, DFAState.SELF),
                new DFAEdge(Entrance.negative(Entrance.or(Entrance.STAR, Entrance.SLASH)), multiLineCommentContent));
        DFAState singleLineCommentContent = new DFAState(TokenType.COMMENT,
                new DFAEdge(singCommentContEntr, DFAState.SELF),
                new DFAEdge(Entrance.anyOf("\n"), commentTerminalState),
                new DFAEdge(Entrance.anyOf("\0"), startState)
                );
        DFAState commentStartState = new DFAState(TokenType.COMMENT,
                new DFAEdge(Entrance.STAR, multiLineCommentContent),
                new DFAEdge(Entrance.SLASH, singleLineCommentContent)
        );

        this.startState.setExitingEdges(
                new DFAEdge(Entrance.DIGIT, numberState),
                new DFAEdge(Entrance.LETTER, idState),
                new DFAEdge(Entrance.WHITESPACE, whiteSpaceState),
                new DFAEdge(type1SymbolEntrance, symbolTerminalState),
                new DFAEdge(type2SymbolEntrance, symbolIntermediateState),
                new DFAEdge(Entrance.anyOf("/"), commentStartState)
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
        char c = text.charAt(++currentReadPosition);
        endOfLine = c == '\n';
        return c;
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
        this.endOfLine = false;
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
