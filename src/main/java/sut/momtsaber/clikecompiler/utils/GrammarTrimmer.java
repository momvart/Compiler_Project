package sut.momtsaber.clikecompiler.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sut.momtsaber.clikecompiler.cfg.*;

public class GrammarTrimmer
{
    public static CFG eliminateLeftRecursions(CFG input)
    {
        CFG newGrammar = new CFG();
        newGrammar.putAllProduction(input.getProductions());
        input.getProductions().values().stream()
                .sorted(Comparator.comparingInt(p -> p.getLeftHand().getId()))
                .forEach(p ->
                {
                    LinkedList<ArrayList<CFGSymbol>> rightHands = p.getRightHands().stream()
                            .flatMap(term ->
                            {
                                if (term.get(0) instanceof CFGNonTerminal &&
                                        ((CFGNonTerminal)term.get(0)).getId() < p.getLeftHand().getId())
                                    return newGrammar.getProduction(((CFGNonTerminal)term.get(0)).getId()).getRightHands().stream()
                                            .map(t ->
                                            {
                                                ArrayList<CFGSymbol> newTerm = new ArrayList<>(t); //TODO: epsilon
                                                newTerm.addAll(term.subList(1, term.size()));
                                                return newTerm;
                                            });
                                else
                                    return Stream.of(term);
                            })
                            .collect(Collectors.toCollection(LinkedList::new));
                    List<CFGProduction> imm = eliminateImmediateLeftRecursion(new CFGProduction(p.getLeftHand(), rightHands),
                            newGrammar.getProductions().size());
                    input.putProduction(imm.get(0));
                    newGrammar.putAllProduction(imm.stream()
                            .collect(Collectors.toMap(np -> np.getLeftHand().getId(), np -> np)));
                });

        return newGrammar;
    }

    private static List<CFGProduction> eliminateImmediateLeftRecursion(CFGProduction production, int freeId)
    {
        CFGNonTerminal prime = new CFGNonTerminal(freeId);
        LinkedList<ArrayList<CFGSymbol>> primeRightHands = production.getRightHands().stream()
                .filter(rightHand -> rightHand.get(0).equals(production.getLeftHand()))
                .map(recursive -> new ArrayList<>(recursive.subList(1, recursive.size())))
                .peek(recursive -> recursive.add(prime))
                .collect(Collectors.toCollection(LinkedList::new));
        if (primeRightHands.size() == 0)
            return Collections.singletonList(production);

        primeRightHands.add(new ArrayList<>());
        LinkedList<ArrayList<CFGSymbol>> newRightHands = production.getRightHands().stream()
                .filter(rightHand -> !rightHand.get(0).equals(production.getLeftHand()))
                .map(ArrayList::new)
                .peek(nonRecursive -> nonRecursive.add(prime)) //TODO: epsilon
                .collect(Collectors.toCollection(LinkedList::new));

        return Arrays.asList(new CFGProduction(production.getLeftHand(), newRightHands),
                new CFGProduction(prime, primeRightHands));
    }

    public static CFG doLeftFactoring(CFG input)
    {
        LinkedHashMap<Integer, CFGProduction> factoredProductions = new LinkedHashMap<>(input.getProductions());
        for (CFGProduction production : new ArrayList<>(input.getProductions().values()))
            factoredProductions.putAll(CFGProduction.leftFactor(production, factoredProductions.size()));

        CFG retVal = new CFG();
        retVal.putAllProduction(factoredProductions);
        return retVal;


    }

}
