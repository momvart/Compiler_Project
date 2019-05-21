package sut.momtsaber.clikecompiler.utils;

import java.util.*;

import sut.momtsaber.clikecompiler.cfg.*;

public class FirstFollowProducer
{
    private static CFG cfg;

    public static void init(CFG grammar)
    {
        cfg = grammar;
    }

    public static Set<CFGTerminal> findFirst(CFGSymbol subject)
    {
        Set<CFGTerminal> firstSet = new HashSet<>();
        if (subject instanceof CFGTerminal)
            return new HashSet<>(Collections.singletonList((CFGTerminal)subject));
        CFGNonTerminal nonTerminal = (CFGNonTerminal)subject;
        for (List<CFGSymbol> rightHand : cfg.getProductions().get(nonTerminal.getId()).getRightHands())
            firstSet.addAll(findFirst(new ArrayList<>(rightHand), null, true));
        return firstSet;
    }

    private static Set<CFGTerminal> findFirst(ArrayList<CFGSymbol> seri, ArrayList<CFGSymbol> parent, boolean first_time_call)
    {
        Set<CFGTerminal> firstSet = new HashSet<>();
        if (seri.size() == 0)
            if (first_time_call || parent.size() <= 0)
                return new HashSet<>(Collections.singletonList(CFGTerminal.EPSILON));
            else
                firstSet.addAll(findFirst(new ArrayList<>(parent.subList(0, 1)), new ArrayList<>(parent.subList(1, parent.size())), false));
        else
        {
            CFGSymbol startSymbol = seri.get(0);
            if (startSymbol instanceof CFGTerminal)
                firstSet.add((CFGTerminal)startSymbol);
            else
            {
                CFGNonTerminal nonTerminal = (CFGNonTerminal)startSymbol;
                for (List<CFGSymbol> rightHand : cfg.getProductions().get(nonTerminal.getId()).getRightHands())
                    firstSet.addAll(findFirst(new ArrayList<>(rightHand), new ArrayList<>(seri.subList(1, seri.size())), false));
            }
        }
        return firstSet;
    }


    public static Set<CFGTerminal> findFollow(CFGNonTerminal myCase)
    {
        Set<CFGTerminal> followSet = new HashSet<>();
        if (myCase.equals(cfg.getProductions().get(0).getLeftHand()))
            followSet.add(CFGTerminal.EOF);
        for (Map.Entry<Integer, CFGProduction> entry : cfg.getProductions().entrySet())
            for (List<CFGSymbol> rightHand : entry.getValue().getRightHands())
                for (CFGSymbol symbol : rightHand)
                    if (symbol.equals(myCase))
                        if (rightHand.indexOf(symbol) == rightHand.size() - 1 && !entry.getValue().getLeftHand().equals(symbol))
                            followSet.addAll(findFollow(entry.getValue().getLeftHand()));
                        else
                            for (int i = rightHand.indexOf(symbol) + 1; i < rightHand.size(); i++)
                            {
                                Set<CFGTerminal> found = findFirst(rightHand.get(i));
                                followSet.addAll(found);
                                if (!found.contains(CFGTerminal.EPSILON))
                                    break;
                                if (i == rightHand.size() - 1)
                                    followSet.addAll(findFollow(entry.getValue().getLeftHand()));
                            }
        followSet.remove(CFGTerminal.EPSILON);
        return followSet;
    }
}

