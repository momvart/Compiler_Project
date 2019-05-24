import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import sut.momtsaber.clikecompiler.utils.CompositeSet;

import static org.junit.Assert.*;

public class CompositeSetTest
{
    @Test
    public void testContainsSimple()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        assertTrue(set.contains(1));
    }

    @Test
    public void testContainsComposite1()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        assertTrue(set.contains(1));
        set.addAll(new HashSet<>(Arrays.asList(5, 3, 1)));
        assertTrue(set.contains(3));
        assertTrue(set.contains(1));
    }

    @Test
    public void testContainsComposite2()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        assertTrue(set.contains(1));
        CompositeSet<Integer> inner = new CompositeSet<>();
        inner.addAll(new HashSet<>(Arrays.asList(5, 3, 1)));
        inner.add(7);
        set.addAll(inner);
        assertTrue(set.contains(3));
        assertTrue(set.contains(1));
        assertTrue(set.contains(5));
        assertTrue(set.contains(7));
    }

    @Test
    public void testContainsCompositeCircular()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        assertTrue(set.contains(1));
        CompositeSet<Integer> inner = new CompositeSet<>();
        inner.addAll(set);
        inner.addAll(new HashSet<>(Arrays.asList(20, 30)));
        inner.add(70);
        set.addAll(inner);

        assertTrue(set.contains(70));
        assertTrue(set.contains(20));
    }

    @Test
    public void testToFlattenedSimple()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        assertEquals(2, set.toFlattenedSet().size());
    }

    @Test
    public void testToFlattenedComposite1()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        set.addAll(new HashSet<>(Arrays.asList(5, 3, 1)));
        assertEquals(4, set.toFlattenedSet().size());
    }

    @Test
    public void tesToFlattenedComposite2()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        CompositeSet<Integer> inner = new CompositeSet<>();
        inner.addAll(new HashSet<>(Arrays.asList(5, 3, 1)));
        inner.add(7);
        set.addAll(inner);
        assertEquals(5, set.toFlattenedSet().size());
    }

    @Test
    public void testToFlattenedCompositeCircular()
    {
        CompositeSet<Integer> set = new CompositeSet<>();
        set.add(1);
        set.add(2);
        CompositeSet<Integer> inner = new CompositeSet<>();
        inner.addAll(set);
        inner.addAll(new HashSet<>(Arrays.asList(20, 30)));
        inner.add(70);
        set.addAll(inner);
        assertEquals(5, set.toFlattenedSet().size());
    }
}
