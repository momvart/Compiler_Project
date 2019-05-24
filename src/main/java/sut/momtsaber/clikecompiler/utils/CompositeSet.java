package sut.momtsaber.clikecompiler.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeSet<E> implements Set<E>
{
    private HashSet<E> internal = new HashSet<>();

    private Set<Set<E>> children = new LinkedHashSet<>();

    private Stream<Set<E>> streamOfAll()
    {
        return Stream.concat(Stream.of(internal), children.stream());
    }

    @Override
    public int size()
    {
        return streamOfAll()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .size();
    }

    @Override
    public boolean isEmpty()
    {
        return streamOfAll().allMatch(Set::isEmpty);
    }

    @Override
    public boolean contains(Object o)
    {
        Set<CompositeSet<E>> open = new HashSet<>();
        open.add(this);
        return streamOfAll().anyMatch(set ->
        {
            if (set instanceof CompositeSet)
            {
                return ((CompositeSet<E>)set).containsInternal(o, open);
            }
            else
                return set.contains(o);
        });
    }

    private boolean containsInternal(Object o, Set<CompositeSet<E>> openSets)
    {
        openSets.add(this);
        boolean result = streamOfAll().anyMatch(set ->
        {
            if (set instanceof CompositeSet)
            {
                if (!openSets.contains(set))
                    return ((CompositeSet<E>)set).containsInternal(o, openSets);
                return false;
            }
            else
                return set.contains(o);
        });
        openSets.remove(this);
        return result;
    }

    public HashSet<E> toFlattenedSet()
    {
        HashSet<E> retVal = new HashSet<>();
        return (HashSet<E>)toFlattenedSet(retVal);
    }

    public Set<E> toFlattenedSet(Set<E> out)
    {
        Set<CompositeSet<E>> open = new HashSet<>();
        open.add(this);
        streamOfAll().forEach(set -> {
            if (set instanceof CompositeSet)
                ((CompositeSet<E>)set).addToFlattenedSet(out, open);
            else
                out.addAll(set);
        });
        return out;
    }

    private void addToFlattenedSet(Set<E> out, Set<CompositeSet<E>> openSets)
    {
        openSets.add(this);
        streamOfAll().forEach(set ->
        {
            if (set instanceof CompositeSet)
            {
                if (!openSets.contains(set))
                    ((CompositeSet<E>)set).addToFlattenedSet(out, openSets);
            }
            else
                out.addAll(set);
        });
        openSets.remove(this);
    }

    @Override
    public Iterator<E> iterator()
    {
        HashSet<E> temp = new HashSet<>();
        return toFlattenedSet(temp).iterator();
    }

    @Override
    public boolean add(E e)
    {
        return internal.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        if (c instanceof Set)
        {
            addChild((Set<E>)c);
            return true;
        }
        else
            return internal.addAll(c);
    }

    public void addChild(Set<E> set) { children.add(set); }

    @Override
    public Object[] toArray()
    {
        return toFlattenedSet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return toFlattenedSet().toArray(a);
    }

    @Override
    public boolean remove(Object o)
    {
        Set<CompositeSet<E>> open = new HashSet<>();
        open.add(this);
        streamOfAll().forEach(set -> {
            if (set instanceof CompositeSet)
                ((CompositeSet<E>)set).removeInternal(o, open);
            else
                set.remove(o);
        });
        return true;
    }

    private void removeInternal(Object o, Set<CompositeSet<E>> openSets)
    {
        openSets.add(this);
        streamOfAll().forEach(set ->
        {
            if (set instanceof CompositeSet)
            {
                if (!openSets.contains(set))
                    ((CompositeSet<E>)set).removeInternal(o, openSets);
            }
            else
                set.remove(o);
        });
        openSets.remove(this);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        streamOfAll().forEach(Set::clear);
    }
}
