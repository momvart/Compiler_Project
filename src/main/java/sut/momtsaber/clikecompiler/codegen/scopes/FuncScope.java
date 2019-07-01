package sut.momtsaber.clikecompiler.codegen.scopes;

import sut.momtsaber.clikecompiler.codegen.FuncDefinition;

public class FuncScope extends Scope
{
    private final FuncDefinition funcDefinition;

    public FuncScope(Scope parent, int startLine, FuncDefinition funcDefinition)
    {
        super(parent, startLine);
        this.funcDefinition = funcDefinition;
    }

    public FuncDefinition getFuncDefinition()
    {
        return funcDefinition;
    }
}
