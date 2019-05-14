package sut.momtsaber.clikecompiler.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CFG
{
    //Just keeping them for testing, debugging and human-readability
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

    public void putNonTerminalName(CFGNonTerminal nt, String name)
    {
        nonTerminalNames.put(nt.getId(), name);
    }
}
