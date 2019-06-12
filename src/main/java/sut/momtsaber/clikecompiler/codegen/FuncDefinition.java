package sut.momtsaber.clikecompiler.codegen;

import java.util.List;

public class FuncDefinition extends Definition
{
    private int lineNum;
    private List<Definition> args;

    public FuncDefinition(String id, int lineNum)
    {
        super(id);
        this.lineNum = lineNum;
    }
}
