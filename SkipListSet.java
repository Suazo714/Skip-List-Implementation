import java.util.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {
    private class SkipListSetItem {
        T value;
        List<SkipListSetItem> nextItems;
        List<SkipListSetItem> prevItems;

        SkipListSetItem(T value, int height) {
            this.value = value;
            this.nextItems = new ArrayList<>(Collections.nCopies(height, (SkipListSetItem) null));
            this.prevItems = new ArrayList<>(Collections.nCopies(height, (SkipListSetItem) null));
        }
    }

    private class SkipListSetIterator implements Iterator<T> {
        SkipListSetItem currentItem;

        SkipListSetIterator() {
            currentItem = head;
        }

        public boolean hasNext() {
            return currentItem.nextItems.get(0) != null;
        }

        public T next() {
            currentItem = currentItem.nextItems.get(0);
            return currentItem.value;
        }

        public void remove() {
            SkipListSet.this.remove(currentItem.value);
        }
    }

    // Constants and instance variables for SkipListSet
    private SkipListSetItem head;
    private SkipListSetItem tail;
    private Random rand;

    private int maxHeight;
    private int size;

    // Construct empty SkipListSet
    public SkipListSet() {
        this.head = new SkipListSetItem(null, 1);
        this.tail = new SkipListSetItem(null, 1);
        this.head.nextItems.set(0, tail);
        this.tail.prevItems.set(0, head);

        this.rand = new Random();
        this.maxHeight = 1;
        this.size = 0;
    }

    // Other methods for SortedSet, Set, Iterable and Collection interfaces
    public Comparator<? super T> comparator() {
        return null;
    }

    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException();
    }

    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException();
    }

    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException();
    }

    public T first() {
        if(size == 0)
            throw new NoSuchElementException();
        return head.nextItems.get(0).value;
    }

    public T last() {
        if(size == 0)
            throw new NoSuchElementException();
        return tail.prevItems.get(0).value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        return getSkipListSetItem(o) != null;
    }

    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    public Object[] toArray() {
        Object[] array = new Object[size];
        int idx = 0;
        for(T value: this) {
            array[idx++] = value;
        }
        return array;
    }

    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    private int randomHeight() {
        int height = 1;
        while (height < maxHeight && rand.nextBoolean()) {
            height++;
        }
        return height;
    }

    public boolean add(T t) {
        if(contains(t))
            return false;

        int height = randomHeight();
        if(height > maxHeight) {
            maxHeight = height;
            while(head.nextItems.size() < maxHeight) {
                head.nextItems.add(null);
                tail.prevItems.add(null);
            }
        }

        SkipListSetItem newItem = new SkipListSetItem(t, height);
        SkipListSetItem current = head;

        for(int i = maxHeight - 1; i >= 0; i--) {
            while (current.nextItems.get(i) == null && current.nextItems.get(i).value.compareTo(t) < 0) {
                current = current.nextItems.get(i);
            }
            if (i < height) {
                newItem.nextItems.set(i, current.nextItems.get(i));
                if (current.nextItems.get(i) != null) {
                    current.nextItems.get(i).prevItems.set(i, newItem);
                }
                current.nextItems.set(i, newItem);
                newItem.prevItems.set(i, current);
            }
        }

        size++;
        return true;
    }

    public boolean remove(Object o) {
        SkipListSetItem itemToRemove = getSkipListSetItem(o);
        if(itemToRemove == null)
            return false;

        for(int i = 0; i < itemToRemove.nextItems.size(); i++) {
            if(itemToRemove.prevItems.get(i) != null) {
                itemToRemove.prevItems.get(i).nextItems.set(i, itemToRemove.nextItems.get(i));
            }
            if(itemToRemove.nextItems.get(i) != null) {
                itemToRemove.nextItems.get(i).prevItems.set(i, itemToRemove.prevItems.get(i));
            }
        }

        size--;
        return true;
    }

    private SkipListSetItem getSkipListSetItem(Object o) {
        if(!(o instanceof Comparable)) {
            return null;
        }
    
        @SuppressWarnings("unchecked")
        Comparable<T> valueToFind = (Comparable<T>) o;
    
        SkipListSetItem current = head;
        for(int i = maxHeight - 1; i >= 0; i--) {
            while(current.nextItems.get(i) != null && (current.nextItems.get(i).value == null || current.nextItems.get(i).value.compareTo((T) valueToFind) < 0)) {
                current = current.nextItems.get(i);
            }
        }
    
        if (current.nextItems.get(0) != null && (current.nextItems.get(0).value == null ? valueToFind == null : current.nextItems.get(0).value.equals(valueToFind))) {
            return current.nextItems.get(0);
        }
        return null;
    }
    

    public boolean containsAll(Collection<?> c) {
        for(Object o: c) {
            if(!contains(o))
                return false;
        }
        return true;
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for(T t: c) {
            changed = add(t);
        }
        return changed;
    }

    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for(Object o: c) {
            changed |= remove(o);
        }
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for(T t: this) {
            if(!c.contains(t)) {
                remove(t);
                changed = true;
            }
        }
        return changed;
    }

    public void clear() {
        head.nextItems.set(0, tail);
        tail.prevItems.set(0, head);
        size = 0;
    }

    public boolean equals(Object o) {
        if(o == this)
            return true;
        if(!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        if(c.size() != size())
            return false;
        return containsAll(c);
    }

    public int hashCode() {
        int hashCode = 0;
        for(T t: this) {
            hashCode += t.hashCode();
        }
        return hashCode;
    }

    public void reBalance() {
    }
}
