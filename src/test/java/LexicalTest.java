import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import sut.momtsaber.clikecompiler.lexicalanalysis.Token;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenizeContext;
import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.ReaderCharacterProvider;

public class LexicalTest
{
    private TokenizeContext context;

    @Before
    public void init()
    {
        context = new TokenizeContext(new ReaderCharacterProvider(""));
    }

    @Test
    public void testNumber()
    {
        context.resetCharProvider("12344");
        Token token = context.getNextToken();
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("12344", token.getValue());

        context.resetCharProvider("1234 5678 9");
        token = context.getNextToken();
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("1234", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("5678", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.NUMBER, token.getType());
        assertEquals("9", token.getValue());
    }

    @Test
    public void testIdentifier()
    {
        context.resetCharProvider("salam123");

    }

    @Test
    public void testKeyword()
    {
        context.resetCharProvider("if");
        Token token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("if", token.getValue());

        context.resetCharProvider("else while if continue  ");
        token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("else", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("while", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("if", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("continue", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals("  ", token.getValue());
    }

    @Test
    public void testSymbol()
    {
        context.resetCharProvider("compiler< database =hoosh == dsd");
        Token token = context.getNextToken();
        assertEquals(TokenType.ID, token.getType());
        assertEquals("compiler", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.SYMBOL, token.getType());
        assertEquals("<", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.ID, token.getType());
        assertEquals("database", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.SYMBOL, token.getType());
        assertEquals("=", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.ID, token.getType());
        assertEquals("hoosh", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.SYMBOL, token.getType());
        assertEquals("==", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.ID, token.getType());
        assertEquals("dsd", token.getValue());
    }

    @Test
    public void testComment()
    {
        context.resetCharProvider("this // this is a single line comment \n if " +
                "/*hello and welcome to you all this is john champion in the commentary box */" +
                "that //this is another single comment\n" +
                "/* this is\n" +
                "a multiline\n" +
                "comment*/ else");
        Token token = context.getNextToken();
        assertEquals(TokenType.ID, token.getType());
        assertEquals("this", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.COMMENT, token.getType());
        assertEquals("// this is a single line comment \n", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("if", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.COMMENT, token.getType());
        assertEquals("/*hello and welcome to you all this is john champion in the commentary box */", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.ID, token.getType());
        assertEquals("that", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.COMMENT, token.getType());
        assertEquals("//this is another single comment\n", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.COMMENT, token.getType());
        assertEquals("/* this is\n" +
                "a multiline\n" +
                "comment*/", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.WHITESPACE, token.getType());
        assertEquals(" ", token.getValue());
        token = context.getNextToken();
        assertEquals(TokenType.KEYWORD, token.getType());
        assertEquals("else", token.getValue());
    }

    @Test
    public void testHasNext()
    {
        context.resetCharProvider("123 salam 10");
        assertTrue(context.hasNextToken());
        context.getNextToken();     //123
        context.getNextToken();     //whitespace
        assertTrue(context.hasNextToken());
        context.getNextToken();     //salam
        context.getNextToken();     //whitespace
        context.getNextToken();     //10
        assertFalse(context.hasNextToken());

        context.resetCharProvider("");
        assertFalse(context.hasNextToken());
    }
}
