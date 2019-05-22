package sut.momtsaber.clikecompiler.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CFG
{
    //Just keeping them for testing, debugging and human-readability
    @Deprecated
    private HashMap<Integer, String> nonTerminalNames = new HashMap<>();

    private LinkedHashMap<Integer, CFGProduction> productions = new LinkedHashMap<>();

    public void putProduction(CFGProduction production)
    {
        productions.put(production.getLeftHand().getId(), production);
    }

    public CFGProduction getProduction(int id)
    {
        return productions.get(id);
    }

    public void putAllProduction(Map<Integer, CFGProduction> newProductions)
    {
        this.productions.putAll(newProductions);
    }

    public Map<Integer, CFGProduction> getProductions()
    {
        return Collections.unmodifiableMap(productions);
    }

    @Deprecated
    public void putNonTerminalName(CFGNonTerminal nt, String name)
    {
        nonTerminalNames.put(nt.getId(), name);
    }


    public Set<CFGTerminal> findFirst(CFGSymbol subject)
    {
        Set<CFGTerminal> firstSet = new HashSet<>();
        if (subject instanceof CFGTerminal)
            return new HashSet<>(Collections.singletonList((CFGTerminal)subject));
        CFGNonTerminal nonTerminal = (CFGNonTerminal)subject;
        for (List<CFGSymbol> rightHand : this.getProductions().get(nonTerminal.getId()).getRightHands())
            firstSet.addAll(findFirst(new ArrayList<>(rightHand), null, true));
        return firstSet;
    }

    public Set<CFGTerminal> findFirst(ArrayList<CFGSymbol> seri, ArrayList<CFGSymbol> parent, boolean first_time_call)
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
                for (List<CFGSymbol> rightHand : this.getProductions().get(nonTerminal.getId()).getRightHands())
                    firstSet.addAll(findFirst(new ArrayList<>(rightHand), new ArrayList<>(seri.subList(1, seri.size())), false));
            }
        }
        return firstSet;
    }


    public Set<CFGTerminal> findFollow(CFGNonTerminal myCase)
    {
        Set<CFGTerminal> followSet = new HashSet<>();
        if (myCase.equals(this.getProductions().get(0).getLeftHand()))
            followSet.add(CFGTerminal.EOF);
        for (Map.Entry<Integer, CFGProduction> entry : this.getProductions().entrySet())
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
