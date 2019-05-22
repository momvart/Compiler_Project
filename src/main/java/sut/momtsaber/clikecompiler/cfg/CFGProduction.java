package sut.momtsaber.clikecompiler.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import sut.momtsaber.clikecompiler.parser.DFA;

public class CFGProduction implements Cloneable
{
    public static Map<Integer, CFGProduction> leftFactor(CFGProduction product, int freeId)
    {
        Map<Integer, CFGProduction> retVal = new HashMap<>();

        CFGProduction clone = (CFGProduction)product.clone();
        LinkedList<CFGRule> finalRightHands = new LinkedList<>();
        while (clone.rightHands.size() > 0)
        {
            CFGRule rule = clone.rightHands.peek();
            if (rule.isEpsilon())
            {
                finalRightHands.add(clone.rightHands.poll());
                continue;
            }

            ArrayList<Integer> commonsCount = clone.rightHands.stream()
                    .map(r ->
                    {
                        int i = 0;
                        for (; i < Math.min(r.size(), rule.size()); i++)
                            if (!r.get(i).equals(rule.get(i)))
                                break;
                        return i;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
            Set<Integer> possibleTermsIndices = IntStream.range(0, clone.rightHands.size())
                    .filter(i -> commonsCount.get(i) > 0)
                    .boxed()
                    .collect(Collectors.toSet());

            if (possibleTermsIndices.size() > 1)
            {
                Optional<Integer> minIndex = possibleTermsIndices.stream()
                        .min(Comparator.comparing(commonsCount::get));

                CFGNonTerminal uncommon = new CFGNonTerminal(freeId++);
                int minCount = commonsCount.get(minIndex.get());
                CFGRule newRule = rule.subRule(0, minCount);
                newRule.add(uncommon);
                finalRightHands.add(newRule);

                CFGProduction uncommonProduct = new CFGProduction(uncommon, possibleTermsIndices.stream()
                        .map(clone.rightHands::get)
                        .map(it -> it.subRule(minCount, it.size()))
                        .collect(Collectors.toCollection(LinkedList::new)));

                retVal.putAll(leftFactor(uncommonProduct, freeId));
            }
            else
                finalRightHands.add(rule);
            //Removing
            {
                Iterator it = clone.rightHands.iterator();
                for (int i = 0; it.hasNext(); i++)
                {
                    it.next();
                    if (possibleTermsIndices.contains(i))
                        it.remove();
                }
            }
        }

        retVal.put(product.leftHand.getId(), new CFGProduction(product.leftHand, finalRightHands));
        return retVal;
    }

    public static List<CFGProduction> eliminateImmediateLeftRecursion(CFGProduction production, int freeId)
    {
        CFGNonTerminal prime = new CFGNonTerminal(freeId);
        LinkedList<CFGRule> primeRightHands = production.getRightHands().stream()
                .filter(rh -> !rh.isEmpty() && rh.get(0).equals(production.getLeftHand()))  //Recursive rules
                .map(recursive -> recursive.subRule(1, recursive.size()))
                .peek(recursive -> recursive.add(prime))
                .collect(Collectors.toCollection(LinkedList::new));
        if (primeRightHands.size() == 0)    //No recursive rule found
            return Collections.singletonList(production);

        primeRightHands.add(CFGRule.createEpsilon()); //epsilon
        LinkedList<CFGRule> newRightHands = production.getRightHands().stream()
                .filter(rh -> rh.isEpsilon() || !rh.get(0).equals(production.getLeftHand()))
                .map(CFGRule::clone)    //keeping the source unchanged
                .peek(nonRecursive -> nonRecursive.add(prime))
                .collect(Collectors.toCollection(LinkedList::new));

        return Arrays.asList(new CFGProduction(production.getLeftHand(), newRightHands),
                new CFGProduction(prime, primeRightHands));
    }


    private CFGNonTerminal leftHand;
    private LinkedList<CFGRule> rightHands;

    public CFGProduction(CFGNonTerminal leftHand, List<CFGRule> rightHands)
    {
        this.leftHand = leftHand;
        this.rightHands = new LinkedList<>(rightHands);
    }

    public CFGNonTerminal getLeftHand()
    {
        return leftHand;
    }

    public LinkedList<CFGRule> getRightHands()
    {
        return rightHands;
    }

    @Override
    protected Object clone()
    {
        LinkedList<CFGRule> rightCopy = new LinkedList<>();
        rightHands.forEach(rule -> rightCopy.add(new CFGRule(rule)));
        return new CFGProduction(leftHand, rightCopy);
    }

    @Override
    public String toString()
    {
        return leftHand + " -> " +
                String.join(" | ", (Iterable<String>)() -> rightHands.stream()
                        .map(rh -> rh.isEmpty() ? "EPS" :
                                String.join(" ", (Iterable<String>)() -> rh.stream()
                                        .map(Object::toString).iterator())).iterator());
    }
}
