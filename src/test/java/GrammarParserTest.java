import org.junit.Test;

import java.util.List;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGRule;
import sut.momtsaber.clikecompiler.cfg.CFGSymbol;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.lexicalanalysis.TokenType;
import sut.momtsaber.clikecompiler.utils.GrammarParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GrammarParserTest
{
    @Test
    public void testSimple()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("S -> NUMBER + S | EPS");
        CFG grammar = parser.closeAndProduce();
        List<CFGRule> rightHands = grammar.getProductions().get(0).getRightHands();
        CFGSymbol numSymbol = rightHands.get(0).get(0);
        assertTrue(numSymbol instanceof CFGTerminal);
        assertEquals(TokenType.NUMBER, ((CFGTerminal)numSymbol).getTokenType());
        CFGSymbol plusSymbol = rightHands.get(0).get(1);
        assertTrue(plusSymbol instanceof CFGTerminal);
        assertEquals(TokenType.SYMBOL, ((CFGTerminal)plusSymbol).getTokenType());
        assertEquals("+", ((CFGTerminal)plusSymbol).getValue());
        CFGSymbol recursionSymbol = rightHands.get(0).get(2);
        assertTrue(recursionSymbol instanceof CFGNonTerminal);
        assertEquals(0, ((CFGNonTerminal)recursionSymbol).getId());

        assertTrue(rightHands.get(1).isEmpty());
    }
}
