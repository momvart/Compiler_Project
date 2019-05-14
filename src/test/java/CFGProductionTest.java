import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import sut.momtsaber.clikecompiler.cfg.*;
import sut.momtsaber.clikecompiler.utils.GrammarParser;
import sut.momtsaber.clikecompiler.utils.GrammarTrimmer;

import static org.junit.Assert.assertEquals;

public class CFGProductionTest
{
    @Test
    public void testSimpleLeftFactor()
    {
        CFGProduction product = new CFGProduction(new CFGNonTerminal(0), new LinkedList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse("else"))),
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse(";"))))
        ));

        Map<Integer, CFGProduction> newProducts = CFGProduction.leftFactor(product, 1);
        assertEquals(2, newProducts.size());
        LinkedList<ArrayList<CFGSymbol>> factored = newProducts.get(0).getRightHands();
        assertEquals(1, factored.size());
        assertEquals(CFGTerminal.parse("if"), factored.get(0).get(0));
        assertEquals(CFGTerminal.parse("NUMBER"), factored.get(0).get(1));
        assertEquals(new CFGNonTerminal(1), factored.get(0).get(2));
        LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(1).getRightHands();
        assertEquals(2, uncommon.size());
        assertEquals(CFGTerminal.parse("else"), uncommon.get(0).get(0));
        assertEquals(CFGTerminal.parse(";"), uncommon.get(1).get(0));
    }

    @Test
    public void testRecursiveLeftFactor()
    {
        CFGProduction product = new CFGProduction(new CFGNonTerminal(0), new LinkedList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse("else"))),
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse(";"), CFGTerminal.parse("if"), CFGTerminal.parse(","))),
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse(";"), CFGTerminal.parse("if"), CFGTerminal.parse("*"))))
        ));

        Map<Integer, CFGProduction> newProducts = CFGProduction.leftFactor(product, 1);
        assertEquals(3, newProducts.size());
        LinkedList<ArrayList<CFGSymbol>> factored = newProducts.get(0).getRightHands();
        assertEquals(1, factored.size());
        assertEquals(CFGTerminal.parse("if"), factored.get(0).get(0));
        assertEquals(CFGTerminal.parse("NUMBER"), factored.get(0).get(1));
        assertEquals(new CFGNonTerminal(1), factored.get(0).get(2));

        {
            LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(1).getRightHands();
            assertEquals(2, uncommon.size());
            assertEquals(CFGTerminal.parse("else"), uncommon.get(0).get(0));
            assertEquals(CFGTerminal.parse(";"), uncommon.get(1).get(0));
            assertEquals(CFGTerminal.parse("if"), uncommon.get(1).get(1));
        }

        {
            LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(2).getRightHands();
            assertEquals(2, uncommon.size());
            assertEquals(CFGTerminal.parse(","), uncommon.get(0).get(0));
            assertEquals(CFGTerminal.parse("*"), uncommon.get(1).get(0));
        }

    }

    @Test
    public void testImmediateLeftRecursion()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("S -> S else | int | if");
        CFG grammar = GrammarTrimmer.eliminateLeftRecursions(parser.closeAndProduce());
        assertEquals(2, grammar.getProductions().size());
        grammar.getProductions().values().forEach(System.err::println);
    }
}
