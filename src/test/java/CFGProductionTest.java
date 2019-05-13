import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import sut.momtsaber.clikecompiler.cfg.*;

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
        Assert.assertEquals(2, newProducts.size());
        LinkedList<ArrayList<CFGSymbol>> factored = newProducts.get(0).getRightHands();
        Assert.assertEquals(1, factored.size());
        Assert.assertEquals(CFGTerminal.parse("if"), factored.get(0).get(0));
        Assert.assertEquals(CFGTerminal.parse("NUMBER"), factored.get(0).get(1));
        Assert.assertEquals(new CFGNonTerminal(1), factored.get(0).get(2));
        LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(1).getRightHands();
        Assert.assertEquals(2, uncommon.size());
        Assert.assertEquals(CFGTerminal.parse("else"), uncommon.get(0).get(0));
        Assert.assertEquals(CFGTerminal.parse(";"), uncommon.get(1).get(0));
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
        Assert.assertEquals(3, newProducts.size());
        LinkedList<ArrayList<CFGSymbol>> factored = newProducts.get(0).getRightHands();
        Assert.assertEquals(1, factored.size());
        Assert.assertEquals(CFGTerminal.parse("if"), factored.get(0).get(0));
        Assert.assertEquals(CFGTerminal.parse("NUMBER"), factored.get(0).get(1));
        Assert.assertEquals(new CFGNonTerminal(1), factored.get(0).get(2));

        {
            LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(1).getRightHands();
            Assert.assertEquals(2, uncommon.size());
            Assert.assertEquals(CFGTerminal.parse("else"), uncommon.get(0).get(0));
            Assert.assertEquals(CFGTerminal.parse(";"), uncommon.get(1).get(0));
            Assert.assertEquals(CFGTerminal.parse("if"), uncommon.get(1).get(1));
        }

        {
            LinkedList<ArrayList<CFGSymbol>> uncommon = newProducts.get(2).getRightHands();
            Assert.assertEquals(2, uncommon.size());
            Assert.assertEquals(CFGTerminal.parse(","), uncommon.get(0).get(0));
            Assert.assertEquals(CFGTerminal.parse("*"), uncommon.get(1).get(0));
        }

    }
}
