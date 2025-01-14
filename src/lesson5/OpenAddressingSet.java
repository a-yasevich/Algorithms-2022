package lesson5;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OpenAddressingSet<T> extends AbstractSet<T> {

    private final int bits;

    private final int capacity;

    private final Object[] storage;
    private final boolean[] deletedMark;

    private int size = 0;

    private int startingIndex(Object element) {
        return element.hashCode() & (0x7FFFFFFF >> (31 - bits));
    }

    public OpenAddressingSet(int bits) {
        if (bits < 2 || bits > 31) {
            throw new IllegalArgumentException();
        }
        this.bits = bits;
        capacity = 1 << bits;
        storage = new Object[capacity];
        deletedMark = new boolean[capacity];
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Проверка, входит ли данный элемент в таблицу
     */
    @Override
    public boolean contains(Object o) {
        int index = startingIndex(o);
        Object current = storage[index];
        while (current != null) {
            if (current.equals(o) && !deletedMark[index]) {
                return true;
            }
            index = (index + 1) % capacity;
            current = storage[index];
            if (index == startingIndex(o)) {
                return false;
            }
        }
        return false;
    }

    /**
     * Добавление элемента в таблицу.
     * <p>
     * Не делает ничего и возвращает false, если такой же элемент уже есть в таблице.
     * В противном случае вставляет элемент в таблицу и возвращает true.
     * <p>
     * Бросает исключение (IllegalStateException) в случае переполнения таблицы.
     * Обычно Set не предполагает ограничения на размер и подобных контрактов,
     * но в данном случае это было введено для упрощения кода.
     */
    @Override
    public boolean add(T t) {
        int startingIndex = startingIndex(t);
        int index = startingIndex;
        int deletedIndex = -1;
        Object current = storage[index];
        while (current != null) {
            if (current.equals(t) && !deletedMark[index]) {
                return false;
            }
            if (deletedMark[index]) {
                deletedIndex = index;
            }
            index = (index + 1) % capacity;
            if (index == startingIndex) {
                throw new IllegalStateException("Table is full");
            }
            current = storage[index];
        }
        if (deletedIndex != -1) {
            index = deletedIndex;
        }
        storage[index] = t;
        deletedMark[index] = false;
        size++;
        return true;
    }

    /**
     * Удаление элемента из таблицы
     * <p>
     * Если элемент есть в таблица, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     * <p>
     * Спецификация: {@link Set#remove(Object)} (Ctrl+Click по remove)
     * <p>
     * Средняя
     */

    //Трудоёмкость remove, add и contains зависит от коэфициента заполнения таблицы A = (size / capacity)
    // и примерно равна (1 / (1 - A))
    @Override
    public boolean remove(Object o) {
        int startingIndex = startingIndex(o);
        int index = startingIndex;
        Object current = storage[index];
        while (current != null) {
            if (current.equals(o) && !deletedMark[index]) {
                deletedMark[index] = true;
                size--;
                return true;
            }
            index = (index + 1) % capacity;
            if (index == startingIndex) {
                return false;
            }
            current = storage[index];
        }
        return false;
    }

    /**
     * Создание итератора для обхода таблицы
     * <p>
     * Не забываем, что итератор должен поддерживать функции next(), hasNext(),
     * и опционально функцию remove()
     * <p>
     * Спецификация: {@link Iterator} (Ctrl+Click по Iterator)
     * <p>
     * Средняя (сложная, если поддержан и remove тоже)
     */
    //Рерурсоёмкость O(1)
    //Трудоёмкость - трудоёмкость операции next()
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new OpenAddressingSetIterator<>();
    }

    private class OpenAddressingSetIterator<T> implements Iterator<T> {
        Object next;
        int nextPossibleIndex = 0;
        int lastReturnedIndex = -1;

        public OpenAddressingSetIterator() {
            next = setNext();
        }

        //Трудоёмоксть O(1)
        @Override
        public boolean hasNext() {
            return next != null;
        }

        //Трудоёмкость зависит от коэфициента заполнения таблицы A = (size / capacity) и в среднем равна (1 / A)
        @Override
        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            Object nextToGive = next;
            lastReturnedIndex = nextPossibleIndex - 1;
            next = setNext();
            return (T) nextToGive;
        }

        //Сложность O(1)
        @Override
        public void remove() {
            System.out.println(lastReturnedIndex);
            if (lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }
            deletedMark[lastReturnedIndex] = true;
            size--;
            lastReturnedIndex = -1;
        }

        private Object setNext() {
            while ((nextPossibleIndex < capacity)
                    && (storage[nextPossibleIndex] == null || deletedMark[nextPossibleIndex])) {
                nextPossibleIndex++;
            }
            int nextIndex = nextPossibleIndex++;
            return nextIndex < capacity ? storage[nextIndex] : null;
        }
    }
}
