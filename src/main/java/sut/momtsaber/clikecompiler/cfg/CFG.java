package sut.momtsaber.clikecompiler.cfg;

import java.util.HashMap;

public class CFG
{
    //Just keeping them for testing, debugging and human-readability
    @Deprecated
    private HashMap<Integer, String> nonTerminalNames = new HashMap<>();

    private HashMap<Integer, CFGProduction> productions = new HashMap<>();

    private CFGNonTerminal startSymbol;

    public void addProduction(CFGProduction production)
    {
        if (productions.size() == 0)
            startSymbol = production.getLeftHand();
        productions.put(production.getLeftHand().getId(), production);
    }

    public HashMap<Integer, CFGProduction> getProductions()
    {
        return productions;
    }

    @Deprecated
    public void putNonTerminalName(CFGNonTerminal nt, String name)
    {
        nonTerminalNames.put(nt.getId(), name);
    }
}
