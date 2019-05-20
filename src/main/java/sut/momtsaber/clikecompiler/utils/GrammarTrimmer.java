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
                    LinkedList<CFGRule> rightHands = p.getRightHands().stream()
                            .flatMap(rule ->
                            {
                                if (!rule.isEpsilon() &&
                                        rule.get(0) instanceof CFGNonTerminal &&
                                        ((CFGNonTerminal)rule.get(0)).getId() < p.getLeftHand().getId())
                                    //Replacing A_i -> A_j a with A_i -> g1 a | g2 a where A_j -> g1 | g2
                                    return newGrammar.getProduction(((CFGNonTerminal)rule.get(0)).getId()).getRightHands().stream()
                                            .map(r -> r.concat(rule.subRule(1)));
                                else
                                    return Stream.of(rule);
                            })
                            .collect(Collectors.toCollection(LinkedList::new));

                    List<CFGProduction> imm = CFGProduction.eliminateImmediateLeftRecursion(
                            new CFGProduction(p.getLeftHand(), rightHands), newGrammar.getProductions().size());

                    //Updating grammar and push to the new grammar
                    input.putProduction(imm.get(0));
                    newGrammar.putAllProduction(imm.stream()
                            .collect(Collectors.toMap(np -> np.getLeftHand().getId(), np -> np)));
                });

        return newGrammar;
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
