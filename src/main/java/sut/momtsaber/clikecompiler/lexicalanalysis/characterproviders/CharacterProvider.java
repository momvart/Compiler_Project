package sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders;

public abstract class CharacterProvider
{
    public abstract boolean hasNext();

    protected abstract char checkedNext();

    public final char next()
    {
        return !hasNext() ? '\0' : checkedNext();
    }
}
