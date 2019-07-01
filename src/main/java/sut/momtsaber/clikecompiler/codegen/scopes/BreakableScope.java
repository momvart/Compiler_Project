package sut.momtsaber.clikecompiler.codegen.scopes;

public class BreakableScope extends Scope
{
    private int breakAddress;

    public BreakableScope(Scope parent, int startLine)
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
