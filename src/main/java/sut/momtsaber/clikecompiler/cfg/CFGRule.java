package sut.momtsaber.clikecompiler.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CFGRule implements List<CFGSymbol>
{
    public static CFGRule createEpsilon()
    {
        return new CFGRule(new ArrayList<>());
    }

    public static CFGRule concat(CFGRule left, CFGRule right)
    {
        return new CFGRule(Stream.concat(left.stream(), right.stream()));
    }

    private ArrayList<CFGSymbol> symbols;

    public CFGRule(List<CFGSymbol> symbols)
    {
        this.symbols = new ArrayList<>(symbols);
    }

    public CFGRule(Stream<CFGSymbol> symbols)
    {
        this.symbols = symbols.collect(Collectors.toCollection(ArrayList::new));
    }

    public CFGRule(CFGSymbol... symbols)
    {
        this.symbols = new ArrayList<>(Arrays.asList(symbols));
    }

    public boolean isEpsilon()
    {
        return symbols.isEmpty();
    }

    public CFGRule subRule(int fromIndex, int toIndex)
    {
        return new CFGRule(subList(fromIndex, toIndex));
    }

    public CFGRule subRule(int fromIndex)
    {
        return subRule(fromIndex, size());
    }

    public CFGRule concat(CFGRule right)
    {
        return concat(this, right);
    }

    @Override
    public CFGRule clone() {return new CFGRule(symbols);}

    @Override
    public String toString()
    {
        return isEpsilon() ? "EPS" :
                String.join(" ", (Iterable<String>)() -> stream().map(Object::toString).iterator());
    }

    //region Delegated
    @Override
    public int size() {return symbols.size();}

    @Override
    public boolean isEmpty() {return symbols.isEmpty();}

    @Override
    public boolean contains(Object o) {return symbols.contains(o);}

    @Override
    public int indexOf(Object o) {return symbols.indexOf(o);}

    @Override
    public int lastIndexOf(Object o) {return symbols.lastIndexOf(o);}

    @Override
    public Object[] toArray() {return symbols.toArray();}

    @Override
    public <T> T[] toArray(T[] a) {return symbols.toArray(a);}

    @Override
    public CFGSymbol get(int index) {return symbols.get(index);}

    @Override
    public CFGSymbol set(int index, CFGSymbol element) {return symbols.set(index, element);}

    @Override
    public boolean add(CFGSymbol cfgSymbol) {return symbols.add(cfgSymbol);}

    @Override
    public void add(int index, CFGSymbol element) {symbols.add(index, element);}

    @Override
    public CFGSymbol remove(int index) {return symbols.remove(index);}

    @Override
    public boolean remove(Object o) {return symbols.remove(o);}

    @Override
    public void clear() {symbols.clear();}

    @Override
    public boolean addAll(Collection<? extends CFGSymbol> c) {return symbols.addAll(c);}

    @Override
    public boolean addAll(int index, Collection<? extends CFGSymbol> c) {return symbols.addAll(index, c);}

    @Override
    public boolean removeAll(Collection<?> c) {return symbols.removeAll(c);}

    @Override
    public boolean retainAll(Collection<?> c) {return symbols.retainAll(c);}

    @Override
    public ListIterator<CFGSymbol> listIterator(int index) {return symbols.listIterator(index);}

    @Override
    public ListIterator<CFGSymbol> listIterator() {return symbols.listIterator();}

    @Override
    public Iterator<CFGSymbol> iterator() {return symbols.iterator();}

    @Override
    public List<CFGSymbol> subList(int fromIndex, int toIndex) {return symbols.subList(fromIndex, toIndex);}

    @Override
    public void forEach(Consumer<? super CFGSymbol> action) {symbols.forEach(action);}

    @Override
    public Spliterator<CFGSymbol> spliterator() {return symbols.spliterator();}

    @Override
    public boolean removeIf(Predicate<? super CFGSymbol> filter) {return symbols.removeIf(filter);}

    @Override
    public void replaceAll(UnaryOperator<CFGSymbol> operator) {symbols.replaceAll(operator);}

    @Override
    public void sort(Comparator<? super CFGSymbol> c) {symbols.sort(c);}

    @Override
    public boolean equals(Object o) {return symbols.equals(o);}

    @Override
    public int hashCode() {return symbols.hashCode();}

    @Override
    public boolean containsAll(Collection<?> c) {return symbols.containsAll(c);}

    @Override
    public Stream<CFGSymbol> stream() {return symbols.stream();}

    @Override
    public Stream<CFGSymbol> parallelStream() {return symbols.parallelStream();}

    //endregion
}
