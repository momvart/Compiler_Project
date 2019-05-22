import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGNonTerminal;
import sut.momtsaber.clikecompiler.cfg.CFGTerminal;
import sut.momtsaber.clikecompiler.utils.FuncProvider;
import sut.momtsaber.clikecompiler.utils.GrammarParser;

import static org.junit.Assert.assertTrue;

public class FirstFollowTest
{
    private CFG grammar;
    private ArrayList<CFGNonTerminal> nonTerminals = new ArrayList<>();
    private ArrayList<CFGTerminal> terminals = new ArrayList<>();
    private ArrayList<Set<CFGTerminal>> expectedFirsts = new ArrayList<>();
    private ArrayList<Set<CFGTerminal>> expectedFollows = new ArrayList<>();

    @Before
    public void initialize()
    {
        GrammarParser parser = new GrammarParser();
        parser.parseAndAddProduction("E -> T R");
        parser.parseAndAddProduction("R -> + T R | EPS");
        parser.parseAndAddProduction("T -> F Y");
        parser.parseAndAddProduction("Y -> * F Y | EPS");
        parser.parseAndAddProduction("F -> ( E ) | -");
        grammar = parser.closeAndProduce();
        grammar.getProductions().forEach((k, v) -> nonTerminals.add(v.getLeftHand()));
        grammar.getProductions().forEach((k, v) -> v.getRightHands().forEach(rhs -> {
            rhs.forEach(symbol -> {
                if (symbol instanceof CFGTerminal) terminals.add((CFGTerminal)symbol);
            });
        }));
        terminals.add(CFGTerminal.EOF);
        terminals = FuncProvider.removeDuplicates(terminals);
        nonTerminals = FuncProvider.removeDuplicates(nonTerminals);

        expectedFirsts.add(new HashSet<>(Arrays.asList(ter("("), ter("-"))));
        expectedFirsts.add(new HashSet<>(Arrays.asList(ter("+"), CFGTerminal.EPSILON)));
        expectedFirsts.add(new HashSet<>(Arrays.asList(ter("("), ter("-"))));
        expectedFirsts.add(new HashSet<>(Arrays.asList(ter("*"), CFGTerminal.EPSILON)));
        expectedFirsts.add(new HashSet<>(Arrays.asList(ter("("), ter("-"))));


        expectedFollows.add(new HashSet<>(Arrays.asList(CFGTerminal.EOF, ter(")"))));
        expectedFollows.add(new HashSet<>(Arrays.asList(CFGTerminal.EOF, ter(")"))));
        expectedFollows.add(new HashSet<>(Arrays.asList(ter("+"), CFGTerminal.EOF, ter(")"))));
        expectedFollows.add(new HashSet<>(Arrays.asList(ter("+"), CFGTerminal.EOF, ter(")"))));
        expectedFollows.add(new HashSet<>(Arrays.asList(ter("*"), ter("+"), CFGTerminal.EOF, ter(")"))));
    }

    private CFGTerminal ter(String raw)
    {
        return CFGTerminal.parse(raw);
    }

    private boolean checkEqual(ArrayList<Set<CFGTerminal>> expected, ArrayList<Set<CFGTerminal>> observed)
    {
        if (!(expected.size() == observed.size()))
            return false;
        for (Set<CFGTerminal> item : expected)
            if (!checkEqual(item, observed.get(expected.indexOf(item))))
            {
                System.out.println(item);
                System.out.println(observed.get(expected.indexOf(item)));
                return false;
            }

        return true;
    }

    private boolean checkEqual(Set<CFGTerminal> first, Set<CFGTerminal> second)
    {
        if (first.size() != second.size())
            return false;
        ArrayList<CFGTerminal> copyfirst = new ArrayList<>(first);
        ArrayList<CFGTerminal> copysecond = new ArrayList<>(second);
        ArrayList<CFGTerminal> matched = new ArrayList<>();
        for (CFGTerminal firstTerminal : copyfirst)
            for (CFGTerminal secondTerminal : copysecond)
            {
                if (matched.contains(firstTerminal) || matched.contains(secondTerminal))
                    continue;
                if (firstTerminal.getTokenType() == secondTerminal.getTokenType() &&
                        ((firstTerminal.getValue() == null && secondTerminal.getValue() == null) ||
                                (firstTerminal.getValue() != null && firstTerminal.getValue().equals(secondTerminal.getValue()))
                        ))
                {
                    matched.add(firstTerminal);
                    matched.add(secondTerminal);
                }
            }
        return matched.size() == 2 * first.size();
    }

    @Test
    public void testFirst()
    {
        ArrayList<Set<CFGTerminal>> observedFirsts = new ArrayList<>();
        for (CFGNonTerminal nonTerminal : nonTerminals)
            observedFirsts.add(grammar.findFirst(nonTerminal));
        assertTrue(checkEqual(expectedFirsts, observedFirsts));
    }

    @Test
    public void testFollow()
    {
        ArrayList<Set<CFGTerminal>> observedFollows = new ArrayList<>();
        for (CFGNonTerminal nonTerminal : nonTerminals)
            observedFollows.add(grammar.findFollow(nonTerminal));
        assertTrue(checkEqual(expectedFollows, observedFollows));
    }

}
