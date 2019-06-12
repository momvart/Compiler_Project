package sut.momtsaber.clikecompiler.codegen;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Scope
{
    private Scope parent;
    private ArrayListValuedHashMap<String, Definition> definitions = new ArrayListValuedHashMap<>();

    public Scope(Scope parent)
    {
        this.parent = parent;
    }

    public boolean addDefinition(Definition definition)
    {
        if (getDefinition(definition.getId()).stream()
                .anyMatch(def -> def.getClass().isInstance(definition)))
            return false;
        definitions.put(definition.getId(), definition);
        return true;
    }

    public VarDefinition getVarDefinition(String id) throws NoSuchElementException
    {
        return (VarDefinition)getDefinition(id).stream()
                .filter(def -> def instanceof VarDefinition)
                .findFirst()
                .get();
    }

    public List<Definition> getDefinition(String id)
    {
        List<Definition> retVal = this.definitions.get(id);
        if (!retVal.isEmpty())
            return retVal;
        if (parent != null)
            return parent.getDefinition(id);
        return new ArrayList<>();
    }
}
