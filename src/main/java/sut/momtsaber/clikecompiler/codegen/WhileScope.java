package sut.momtsaber.clikecompiler.codegen;

public class WhileScope extends Scope
{
    private int breakAddress;
    public WhileScope(Scope parent, int startLine)
    {
        super(parent, startLine);
    }

    public int getBreakAddress()
    {
        return breakAddress;
    }

    public void setBreakAddress(int breakAddress)
    {
        this.breakAddress = breakAddress;
    }
}
