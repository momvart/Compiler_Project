package sut.momtsaber.clikecompiler.cfg;

import java.util.*;
import java.util.function.BiFunction;

public class CFG
{
    //Just keeping them for testing, debugging and human-readability
    @Deprecated
    private HashMap<Integer, String> nonTerminalNames = new HashMap<>();

    private LinkedHashMap<Integer, CFGProduction> productions = new LinkedHashMap<>();

    private HashMap<Integer, Set<CFGTerminal>> cachedFirsts = new HashMap<>();
    private HashMap<Integer, Set<CFGTerminal>> cachedFollows = new HashMap<>();

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


    private static final BiFunction<Integer, Integer, Integer> firstKeyBuilder = (ntId, ruleIndex) ->
            (ntId << Integer.SIZE / 2) + (ruleIndex & 0xFFFF);

    public Set<CFGTerminal> findFirst(CFGSymbol subject)
    {
        if (subject instanceof CFGTerminal)
            return new HashSet<>(Collections.singletonList((CFGTerminal)subject));

        CFGNonTerminal nonTerminal = (CFGNonTerminal)subject;
        Integer key = firstKeyBuilder.apply(nonTerminal.getId(), -1);
        Set<CFGTerminal> firstSet = cachedFirsts.get(key);
        if (firstSet != null)       //cached
            return firstSet;

        firstSet = new HashSet<>();
        CFGProduction production = getProduction(nonTerminal.getId());
        for (int i = 0; i < production.getRightHands().size(); i++)
            firstSet.addAll(findFirst(production, i));
        cachedFirsts.put(key, firstSet);
        return firstSet;
    }

    public Set<CFGTerminal> findFirst(CFGProduction production, int index)
    {
        Integer key = firstKeyBuilder.apply(production.getLeftHand().getId(), index);
        Set<CFGTerminal> firstSet = cachedFirsts.get(key);
        if (firstSet != null)       //cached
            return firstSet;

        //TODO: linked list accessed by index
        firstSet = findFirstInternal(Collections.unmodifiableList(production.getRightHands().get(index)), null, true);
        cachedFirsts.put(key, firstSet);
        return firstSet;
    }

    private Set<CFGTerminal> findFirstInternal(List<CFGSymbol> seri, List<CFGSymbol> parent, boolean firstTimeCall)
    {
        Set<CFGTerminal> firstSet = new HashSet<>();
        if (seri.size() == 0)
            if (firstTimeCall || parent.size() <= 0)
                return new HashSet<>(Collections.singletonList(CFGTerminal.EPSILON));
            else
                firstSet.addAll(findFirstInternal(parent.subList(0, 1), parent.subList(1, parent.size()), false));
        else
        {
            CFGSymbol startSymbol = seri.get(0);
            if (startSymbol instanceof CFGTerminal)
                firstSet.add((CFGTerminal)startSymbol);
            else
            {
                CFGNonTerminal nonTerminal = (CFGNonTerminal)startSymbol;
                for (List<CFGSymbol> rightHand : this.getProductions().get(nonTerminal.getId()).getRightHands())
                    firstSet.addAll(findFirstInternal(Collections.unmodifiableList(rightHand), seri.subList(1, seri.size()), false));
            }
        }
        return firstSet;
    }

    public Set<CFGTerminal> findFollow(CFGNonTerminal myCase)
    {
        Set<CFGTerminal> followSet = cachedFollows.get(myCase.getId());
        if (followSet != null)      //cached
            return followSet;

        followSet = new HashSet<>();
        cachedFollows.put(myCase.getId(), followSet);
        if (myCase.getId() == 0)      //initial state
            followSet.add(CFGTerminal.EOF);
        for (CFGProduction production : getProductions().values())
            for (CFGRule rightHand : production.getRightHands())
                for (int iSymbol = 0; iSymbol < rightHand.size(); iSymbol++)
                {
                    CFGSymbol symbol = rightHand.get(iSymbol);
                    if (symbol.equals(myCase))
                        if (iSymbol == rightHand.size() - 1 && !production.getLeftHand().equals(symbol))
                            followSet.addAll(findFollow(production.getLeftHand()));
                        else
                            for (int i = iSymbol + 1; i < rightHand.size(); i++)
                            {
                                Set<CFGTerminal> found = findFirst(rightHand.get(i));
                                followSet.addAll(found);
                                if (!found.contains(CFGTerminal.EPSILON))
                                    break;
                                if (i == rightHand.size() - 1 && !production.getLeftHand().equals(symbol))
                                    followSet.addAll(findFollow(production.getLeftHand()));
                            }
                }
        followSet.remove(CFGTerminal.EPSILON);
        return followSet;
    }
}
