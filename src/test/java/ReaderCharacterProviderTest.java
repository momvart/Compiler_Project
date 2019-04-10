import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

import sut.momtsaber.clikecompiler.lexicalanalysis.characterproviders.ReaderCharacterProvider;

public class ReaderCharacterProviderTest
{
    @Test
    public void testString()
    {
        ReaderCharacterProvider provider = new ReaderCharacterProvider("salam");
        assertTrue(provider.hasNext());
        assertEquals('s', provider.next());
        assertEquals('a', provider.next());
        assertTrue(provider.hasNext());
        assertEquals('l', provider.next());
        assertEquals('a', provider.next());
        assertEquals('m', provider.next());
        assertFalse(provider.hasNext());
    }

    @Test
    public void testStream()
    {
        ReaderCharacterProvider provider = new ReaderCharacterProvider(new ByteArrayInputStream("salam".getBytes()));
        assertTrue(provider.hasNext());
        assertEquals('s', provider.next());
        assertEquals('a', provider.next());
        assertTrue(provider.hasNext());
        assertEquals('l', provider.next());
        assertEquals('a', provider.next());
        assertEquals('m', provider.next());
        assertFalse(provider.hasNext());
    }
}
