package sut.momtsaber.clikecompiler.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import sut.momtsaber.clikecompiler.cfg.CFG;
import sut.momtsaber.clikecompiler.cfg.CFGProduction;

public class GrammarTrimmer
{
    public static CFG removeLeftRecursions()
    {
        return null;
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
