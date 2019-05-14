import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import sut.momtsaber.clikecompiler.cfg.*;
import sut.momtsaber.clikecompiler.utils.GrammarParser;
import sut.momtsaber.clikecompiler.utils.GrammarTrimmer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void epsilonShouldNotBreakLeftFactor()
    {
        CFGProduction product = new CFGProduction(new CFGNonTerminal(0), new LinkedList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse("else"))),
                new ArrayList<>(Arrays.asList(CFGTerminal.parse("if"), CFGTerminal.parse("NUMBER"), CFGTerminal.parse(";"))),
                new ArrayList<>())
        ));

        Map<Integer, CFGProduction> newProducts = CFGProduction.leftFactor(product, 1);
        assertEquals(2, newProducts.size());
        LinkedList<ArrayList<CFGSymbol>> factored = newProducts.get(0).getRightHands();
        assertEquals(2, factored.size());
        assertEquals(CFGTerminal.parse("if"), factored.get(0).get(0));
        assertEquals(CFGTerminal.parse("NUMBER"), factored.get(0).get(1));
        assertEquals(new CFGNonTerminal(1), factored.get(0).get(2));
        assertTrue(factored.get(1).isEmpty());
        LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(1).getRightHands();
        assertEquals(2, uncommon.size());
        assertEquals(CFGTerminal.parse("else"), uncommon.get(0).get(0));
        assertEquals(CFGTerminal.parse(";"), uncommon.get(1).get(0));
    }

    @Test
    public void testImmediateLeftRecursion()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("S -> S else | int | if");
        CFG grammar = GrammarTrimmer.eliminateLeftRecursions(parser.closeAndProduce());
        assertEquals(2, grammar.getProductions().size());

        CFGProduction newS = grammar.getProduction(0);
        assertEquals(2, newS.getRightHands().size());
        assertEquals(CFGTerminal.parse("int"), newS.getRightHands().get(0).get(0));
        assertEquals(new CFGNonTerminal(1), newS.getRightHands().get(0).get(1));
        assertEquals(CFGTerminal.parse("if"), newS.getRightHands().get(1).get(0));
        assertEquals(new CFGNonTerminal(1), newS.getRightHands().get(1).get(1));

        CFGProduction sPrime = grammar.getProduction(1);
        assertEquals(2, newS.getRightHands().size());
        assertEquals(CFGTerminal.parse("else"), sPrime.getRightHands().get(0).get(0));
        assertEquals(new CFGNonTerminal(1), sPrime.getRightHands().get(0).get(1));
        assertTrue(sPrime.getRightHands().get(1).isEmpty()); //epsilon
    }

    @Test
    public void testLeftRecursion1()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("S -> A else | int | if");
        parser.parseAndAddProduction("A -> A NUMBER | S +");
        CFG grammar = GrammarTrimmer.eliminateLeftRecursions(parser.closeAndProduce());
        grammar.getProductions().values().forEach(System.err::println);


        CFGProduction newA = grammar.getProduction(1);
        assertEquals(CFGTerminal.parse("int"), newA.getRightHands().get(0).get(0));
        assertEquals(CFGTerminal.parse("+"), newA.getRightHands().get(0).get(1));
        assertEquals(new CFGNonTerminal(2), newA.getRightHands().get(0).get(2));
        assertEquals(CFGTerminal.parse("if"), newA.getRightHands().get(1).get(0));
        assertEquals(CFGTerminal.parse("+"), newA.getRightHands().get(1).get(1));
        assertEquals(new CFGNonTerminal(2), newA.getRightHands().get(1).get(2));

        CFGProduction aPrime = grammar.getProduction(2);
        assertEquals(CFGTerminal.parse("NUMBER"), aPrime.getRightHands().get(0).get(0));
        assertEquals(new CFGNonTerminal(2), aPrime.getRightHands().get(0).get(1));
        assertEquals(CFGTerminal.parse("else"), aPrime.getRightHands().get(1).get(0));
        assertEquals(CFGTerminal.parse("+"), aPrime.getRightHands().get(1).get(1));
        assertEquals(new CFGNonTerminal(2), aPrime.getRightHands().get(1).get(2));
        assertTrue(aPrime.getRightHands().get(2).isEmpty());
    }
}
