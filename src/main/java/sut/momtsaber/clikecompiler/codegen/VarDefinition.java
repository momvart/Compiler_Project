package sut.momtsaber.clikecompiler.codegen;

public class VarDefinition extends Definition
{
    private final int address;
    private final boolean isArray;

    public VarDefinition(String id, int address)
    {
        this(id, address, false);
    }

    public VarDefinition(String id, int address, boolean isArray)
    {
        super(id);
        this.address = address;
        this.isArray = isArray;
    }

    public int getAddress()
    {
        return address;
    }

    public boolean isArray()
    {
        return isArray;
    }
}
