package sut.momtsaber.clikecompiler.cfg;

import java.util.*;
import java.util.stream.*;

public class CFGProduction implements Cloneable
{
    public static Map<Integer, CFGProduction> leftFactor(CFGProduction product, int freeId)
    {
        Map<Integer, CFGProduction> retVal = new HashMap<>();

        CFGProduction clone = (CFGProduction)product.clone();
        LinkedList<ArrayList<CFGSymbol>> finalRightHands = new LinkedList<>();
        while (clone.rightHands.size() > 0)
        {
            ArrayList<CFGSymbol> term = clone.rightHands.get(0);
            ArrayList<Integer> commonsCount = clone.rightHands.stream()
                    .map(t ->
                    {
                        int i = 0;
                        for (; i < Math.min(t.size(), term.size()); i++)
                            if (!t.get(i).equals(term.get(i)))
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
                ArrayList<CFGSymbol> newTerm = new ArrayList<>(term.subList(0, minCount));
                newTerm.add(uncommon);
                finalRightHands.add(newTerm);

                CFGProduction uncommonProduct = new CFGProduction(uncommon, possibleTermsIndices.stream()
                        .map(clone.rightHands::get)
                        .map(it -> it.subList(minCount, it.size()))
                        .map(it -> it.isEmpty() ? new ArrayList<CFGSymbol>(Arrays.asList(CFGTerminal.EPSILON)) : new ArrayList<>(it))
                        .collect(Collectors.toCollection(LinkedList::new)));

                retVal.putAll(leftFactor(uncommonProduct, freeId));
            }
            else
                finalRightHands.add(term);
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

    private CFGNonTerminal leftHand;
    private LinkedList<ArrayList<CFGSymbol>> rightHands;

    public CFGProduction(CFGNonTerminal leftHand, LinkedList<ArrayList<CFGSymbol>> rightHands)
    {
        this.leftHand = leftHand;
        this.rightHands = rightHands;
    }

    public CFGNonTerminal getLeftHand()
    {
        return leftHand;
    }

    public LinkedList<ArrayList<CFGSymbol>> getRightHands()
    {
        return rightHands;
    }

    @Override
    protected Object clone()
    {
        LinkedList<ArrayList<CFGSymbol>> rightCopy = new LinkedList<>();
        rightHands.forEach(term -> rightCopy.add(new ArrayList<>(term)));
        return new CFGProduction(leftHand, rightCopy);
    }
}
