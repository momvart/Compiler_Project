package sut.momtsaber.clikecompiler.codegen.scopes;

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import sut.momtsaber.clikecompiler.codegen.Definition;
import sut.momtsaber.clikecompiler.codegen.FuncDefinition;
import sut.momtsaber.clikecompiler.codegen.VarDefinition;
import sut.momtsaber.clikecompiler.codegen.exceptions.UndefinedFunctionException;
import sut.momtsaber.clikecompiler.codegen.exceptions.UndefinedVariableException;

public class Scope
{
    private Scope parent;
    private ArrayListValuedHashMap<String, Definition> definitions = new ArrayListValuedHashMap<>();
    private int startLine;
    private int endLine;

    public Scope(Scope parent, int startLine)
    {
        this.parent = parent;
        this.startLine = startLine;
    }

    public boolean addDefinition(Definition definition)
    {
        if (definitions.get(definition.getId()).stream()
                .anyMatch(def -> def.getClass().isInstance(definition)))
            return false;
        definitions.put(definition.getId(), definition);
        return true;
    }

    public VarDefinition getVarDefinition(String id) throws UndefinedVariableException
    {
        return (VarDefinition)getDefinition(id).stream()
                .filter(VarDefinition.class::isInstance)
                .findFirst()
                .orElseThrow(() -> new UndefinedVariableException(0, id));
    }

    public FuncDefinition getFuncDefinition(String id) throws UndefinedFunctionException
    {
        return (FuncDefinition)getDefinition(id).stream()
                .filter(FuncDefinition.class::isInstance)
                .findFirst()
                .orElseThrow(() -> new UndefinedFunctionException(0, id));
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

    public int getStartLine()
    {
        return startLine;
    }

    public int getEndLine()
    {
        return endLine;
    }

    public void setEndLine(int endLine)
    {
        this.endLine = endLine;
    }

    public Scope getParent()
    {
        return parent;
    }
}
