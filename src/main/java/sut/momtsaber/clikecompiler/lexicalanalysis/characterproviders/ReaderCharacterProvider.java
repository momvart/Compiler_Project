package sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class ReaderCharacterProvider extends CharacterProvider implements Closeable
{
    private Reader reader;
    private Character buffer;

    public ReaderCharacterProvider(Reader reader)
    {
        this.reader = reader;
    }

    public ReaderCharacterProvider(String text)
    {
        this(new StringReader(text));
    }

    public ReaderCharacterProvider(InputStream stream)
    {
        this(new InputStreamReader(stream));
    }

    @Override
    public void close() throws IOException
    {
        this.reader.close();
    }

    @Override
    public boolean hasNext()
    {
        if (buffer != null)
            return true;
        try
        {
            int readVal = reader.read();
            if (readVal == -1)
                return false;
            buffer = (char)readVal;
            return true;
        }
        catch (IOException e) { return false; }
    }

    @Override
    public char checkedNext()
    {
        char retVal = buffer;
        buffer = null;
        return retVal;
    }
}
