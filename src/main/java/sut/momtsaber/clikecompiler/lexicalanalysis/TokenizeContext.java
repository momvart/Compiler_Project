package sut.momtsaber.clikecompiler.lexicalanalysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.CharacterProvider;
import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.ReaderCharacterProvider;


public class TokenizeContext
{
    private static final String SYMBOLS = ";:,[](){}+-*=<";
    private static final String TYPE_1_SYMBOLS = ";:,[](){}+-*<";
    private static final String TYPE_2_SYMBOLS = "=";
    private static final Set<String> KEYWORDS = new HashSet<>(
            Arrays.asList("if", "else", "void", "int",
                    "while", "break", "continue", "switch",
                    "default", "case", "return"));

    private CharacterProvider charProvider;
    private StringBuffer buffer;

    private int currentReadPosition;

    private DFAState startState;
    private DFAState elseState;
    private DFAState currentState;


    public TokenizeContext(CharacterProvider charProvider)
    {
        this.resetCharProvider(charProvider);
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

        DFAEdge returnEdge = new DFAEdge(       //The exiting edge from id and number to start state
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

        //Symbols
        DFAState symbolTerminalState = new DFAState(TokenType.SYMBOL,
                new DFAEdge(Entrance.ANY, startState));

        DFAState symbolIntermediateState = new DFAState(TokenType.SYMBOL,
                new DFAEdge(type2SymbolEntrance, symbolTerminalState),
                new DFAEdge(Entrance.ANY, startState)
        );

        //Comment
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

        //The start (main) state
        this.startState.setExitingEdges(
                new DFAEdge(Entrance.DIGIT, numberState),
                new DFAEdge(Entrance.LETTER, idState),
                new DFAEdge(Entrance.WHITESPACE, whiteSpaceState),
                new DFAEdge(type1SymbolEntrance, symbolTerminalState),
                new DFAEdge(type2SymbolEntrance, symbolIntermediateState),
                new DFAEdge(Entrance.anyOf("/"), commentStartState)
        );

        //the state to detect invalid inputs
        this.elseState = new DFAState(TokenType.INVALID,
                new DFAEdge(Entrance.ANY, startState));
        this.currentState = startState;
    }

    private boolean hasNextChar()
    {
        return charProvider.hasNext() ||
                (buffer.length() > 0 && currentReadPosition < buffer.length() && buffer.charAt(0) != '\0');
    }

    private char nextChar()
    {
        if (currentReadPosition + 1 == buffer.length())
            buffer.append(charProvider.next());

        return buffer.charAt(++currentReadPosition);
    }

    char getCurrentChar() { return buffer.charAt(currentReadPosition); }

    public void resetCharProvider(CharacterProvider charProvider)
    {
        this.charProvider = charProvider;
        this.buffer = new StringBuffer();
        this.currentReadPosition = -1;
    }

    public void resetCharProvider(String text)
    {
        this.resetCharProvider(new ReaderCharacterProvider(text));
    }

    public boolean hasNextToken()
    {
        return hasNextChar();
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
        while (this.hasNextChar() && currentState != startState);

        Token retVal = new Token(lastTokenType, buffer.substring(0, currentReadPosition));
        if (retVal.getType() == TokenType.ID && KEYWORDS.contains(retVal.getValue()))
            retVal.setType(TokenType.KEYWORD);

        buffer.replace(0, currentReadPosition, "");
        currentReadPosition = -1;          //Putting the pointer back for next the token
        return retVal;
    }
}
