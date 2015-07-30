/*
 * Nicholas Saney
 * 
 * Created: July 12, 2015
 * 
 * ArrayIterator.java
 * ArrayIterator class definition
 */

package chairosoft.psv;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T>
{
    private final T[] array;
    private int i = 0;
    public ArrayIterator(T[] _array) { this.array = _array; }
    
    @Override public boolean hasNext() { return this.i < this.array.length; }
    @Override public T next() { return this.array[i++]; }
    @Override public void remove() { throw new UnsupportedOperationException(); }
}