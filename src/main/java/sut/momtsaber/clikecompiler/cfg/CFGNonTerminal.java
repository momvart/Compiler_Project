package sut.momtsaber.clikecompiler.cfg;

public class CFGNonTerminal extends CFGSymbol
{
    private final int id;

    public CFGNonTerminal(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CFGNonTerminal)) return false;

        CFGNonTerminal that = (CFGNonTerminal)o;

        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}
