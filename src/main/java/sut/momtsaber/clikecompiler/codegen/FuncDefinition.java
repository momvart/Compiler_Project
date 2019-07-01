package sut.momtsaber.clikecompiler.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FuncDefinition extends Definition
{
    public static final String RETURN_ADDR_VAR_NAME = "0ra";
    public static final String RETURN_VAL_VAR_NAME = "1rv";

    private int lineNum;
    private List<VarDefinition> args;
    private final VarDefinition returnAddress;
    private final VarDefinition returnValue;

    public FuncDefinition(String id, int lineNum, int returnAddressAddress, int returnValAddress)
    {
        super(id);
        this.lineNum = lineNum;
        this.args = new ArrayList<>();
        this.returnAddress = new VarDefinition(RETURN_ADDR_VAR_NAME, returnAddressAddress);
        this.returnValue = returnValAddress > 0 ? new VarDefinition(RETURN_VAL_VAR_NAME, returnValAddress) : null;
    }

    public int getLineNum()
    {
        return lineNum;
    }

    public List<VarDefinition> getArgs()
    {
        return Collections.unmodifiableList(args);
    }

    public void addArg(VarDefinition definition)
    {
        args.add(definition);
    }

    public VarDefinition getReturnAddress()
    {
        return returnAddress;
    }

    public VarDefinition getReturnValue()
    {
        return returnValue;
    }
}
