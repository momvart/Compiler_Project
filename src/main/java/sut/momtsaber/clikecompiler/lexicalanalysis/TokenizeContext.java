package sut.momtsaber.clikecompiler.lexicalanalysis;

import sut.momtsaber.clikecompiler.dfa.Entrance;
import sut.momtsaber.clikecompiler.lexicalanalysis.dfa.*;
import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.*;


public class TokenizeContext
{
    private static final String TYPE_1_SYMBOLS = ";:,[](){}+-*<";
    private static final String TYPE_2_SYMBOLS = "=";

    private CharacterProvider charProvider;
    private StringBuffer buffer;
    private int currentReadPosition;
    private int currentLineNumber;

    private TDFAState startState;
    private TDFAState elseState;
    private TDFAState currentState;


    public TokenizeContext(CharacterProvider charProvider)
    {
        this.resetCharProvider(charProvider);
        init();
    }

    private void init()
    {
        CharEntrance symbolEntrance = CharEntrance.anyOf(Token.SYMBOLS);
        CharEntrance type1SymbolEntrance = CharEntrance.anyOf(TYPE_1_SYMBOLS);
        CharEntrance type2SymbolEntrance = CharEntrance.anyOf(TYPE_2_SYMBOLS);

        Entrance<Character> singCommentContEntr = Entrance.negative(CharEntrance.anyOf("\n\0"));
        Entrance<Character> multiCommentContEntr = Entrance.negative(CharEntrance.anyOf("*"));

        this.startState = new TDFAState();

        TDFAEdge returnEdge = new TDFAEdge(       //The exiting edge from id and number to start state
                Entrance.or(CharEntrance.WHITESPACE,
                        CharEntrance.of('\0'),
                        symbolEntrance),
                startState);

        TDFAState numberState = new TDFAState(TokenType.NUMBER,
                new TDFAEdge(CharEntrance.DIGIT, TDFAState.SELF),
                returnEdge);

        TDFAState idState = new TDFAState(TokenType.ID,
                new TDFAEdge(CharEntrance.LETTER_OR_DIGIT, TDFAState.SELF),
                returnEdge);

        TDFAState whiteSpaceState = new TDFAState(TokenType.WHITESPACE,
                new TDFAEdge(CharEntrance.WHITESPACE, TDFAState.SELF),
                new TDFAEdge(Entrance.negative(CharEntrance.WHITESPACE), startState));

        //Symbols
        TDFAState symbolTerminalState = new TDFAState(TokenType.SYMBOL,
                new TDFAEdge(CharEntrance.ANY, startState));

        TDFAState symbolIntermediateState = new TDFAState(TokenType.SYMBOL,
                new TDFAEdge(type2SymbolEntrance, symbolTerminalState),
                new TDFAEdge(CharEntrance.ANY, startState)
        );

        //Comment
        TDFAState commentTerminalState = new TDFAState(TokenType.COMMENT,
                new TDFAEdge(CharEntrance.ANY, startState));
        TDFAState commentEndStarState = new TDFAState(TokenType.COMMENT);

        TDFAState multiLineCommentContent = new TDFAState(TokenType.COMMENT,
                new TDFAEdge(multiCommentContEntr, TDFAState.SELF),
                new TDFAEdge(CharEntrance.STAR, commentEndStarState)
        );

        commentEndStarState.setExitingEdges(
                new TDFAEdge(CharEntrance.SLASH, commentTerminalState),
                new TDFAEdge(CharEntrance.STAR, TDFAState.SELF),
                new TDFAEdge(Entrance.negative(Entrance.or(CharEntrance.STAR, CharEntrance.SLASH)), multiLineCommentContent));
        TDFAState singleLineCommentContent = new TDFAState(TokenType.COMMENT,
                new TDFAEdge(singCommentContEntr, TDFAState.SELF),
                new TDFAEdge(CharEntrance.of('\n'), commentTerminalState),
                new TDFAEdge(CharEntrance.of('\0'), startState)
        );
        TDFAState commentStartState = new TDFAState(TokenType.COMMENT,
                new TDFAEdge(CharEntrance.STAR, multiLineCommentContent),
                new TDFAEdge(CharEntrance.SLASH, singleLineCommentContent)
        );

        //The start (main) state
        this.startState.setExitingEdges(
                new TDFAEdge(CharEntrance.DIGIT, numberState),
                new TDFAEdge(CharEntrance.LETTER, idState),
                new TDFAEdge(CharEntrance.WHITESPACE, whiteSpaceState),
                new TDFAEdge(type1SymbolEntrance, symbolTerminalState),
                new TDFAEdge(type2SymbolEntrance, symbolIntermediateState),
                new TDFAEdge(CharEntrance.of('/'), commentStartState)
        );

        //the state to detect invalid inputs
        this.elseState = new TDFAState(TokenType.INVALID,
                new TDFAEdge(CharEntrance.ANY, startState));
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
        {
            char c = charProvider.next();
            if (c == '\n')
                currentLineNumber += 1;
            buffer.append(c);
        }

        return buffer.charAt(++currentReadPosition);
    }

    char getCurrentChar() { return buffer.charAt(currentReadPosition); }

    public int getCurrentLineNumber()
    {
        return currentLineNumber;
    }

    public void resetCharProvider(CharacterProvider charProvider)
    {
        this.charProvider = charProvider;
        this.buffer = new StringBuffer();
        this.currentReadPosition = -1;
        this.currentLineNumber = 1;
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
            currentState = currentState.getNextStateOld(nextChar());

            if (currentState == null)
                currentState = elseState;

        } //When we are back to start, a token is generated
        while (this.hasNextChar() && currentState != startState);

        Token retVal = new Token(lastTokenType, buffer.substring(0, currentReadPosition));
        if (retVal.getType() == TokenType.ID && Token.KEYWORDS.contains(retVal.getValue()))
            retVal.setType(TokenType.KEYWORD);

        buffer.replace(0, currentReadPosition, "");
        currentReadPosition = -1;          //Putting the pointer back for next the token
        return retVal;
    }
}
