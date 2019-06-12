package sut.momtsaber.clikecompiler.codegen;

public abstract class Definition
{
    private final String id;

    public Definition(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
}
