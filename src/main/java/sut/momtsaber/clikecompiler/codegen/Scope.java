package sut.momtsaber.clikecompiler.codegen;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.HashMap;
import java.util.List;

public class Scope
{
    private Scope parent;
    private ArrayListValuedHashMap<String, Definition> definitions = new ArrayListValuedHashMap<>();

    public Scope(Scope parent)
    {
        this.parent = parent;
    }

    public List<Definition> getDefinition(String name)
    {
        List<Definition> retVal = this.definitions.get(name);
        if (!retVal.isEmpty())
            return retVal;
        return parent.getDefinition(name);
    }
}
