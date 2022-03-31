package lesson4;

import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Префиксное дерево для строк
 */
public class Trie extends AbstractSet<String> implements Set<String> {

    private static class Node {
        SortedMap<Character, Node> children = new TreeMap<>();
    }

    private final Node root = new Node();

    private int size = 0;

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root.children.clear();
        size = 0;
    }

    private String withZero(String initial) {
        return initial + (char) 0;
    }

    @Nullable
    private Node findNode(String element) {
        Node current = root;
        for (char character : element.toCharArray()) {
            if (current == null) return null;
            current = current.children.get(character);
        }
        return current;
    }

    @Override
    public boolean contains(Object o) {
        String element = (String) o;
        return findNode(withZero(element)) != null;
    }

    @Override
    public boolean add(String element) {
        Node current = root;
        boolean modified = false;
        for (char character : withZero(element).toCharArray()) {
            Node child = current.children.get(character);
            if (child != null) {
                current = child;
            } else {
                modified = true;
                Node newChild = new Node();
                current.children.put(character, newChild);
                current = newChild;
            }
        }
        if (modified) {
            size++;
        }
        return modified;
    }

    @Override
    public boolean remove(Object o) {
        String element = (String) o;
        Node current = findNode(element);
        if (current == null) return false;
        if (current.children.remove((char) 0) != null) {
            size--;
            return true;
        }
        return false;
    }

    /**
     * Итератор для префиксного дерева
     * <p>
     * Спецификация: {@link Iterator} (Ctrl+Click по Iterator)
     * <p>
     * Сложная
     */
    @NotNull
    @Override
    public Iterator<String> iterator() {
        return new PrefixTreeIterator();
    }

    private class PrefixTreeIterator implements Iterator<String> {
        private final Deque<Iterator<Map.Entry<Character, Node>>> stack;
        private final StringBuilder builder = new StringBuilder();
        private Iterator<Map.Entry<Character, Node>> current;
        private String next;

        private PrefixTreeIterator() {
            stack = new ArrayDeque<>();
            current = root.children.entrySet().iterator();
            setNext();
            next = builder.isEmpty() ? null : builder.substring(0, builder.length() - 1);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public String next() {
            String nextToGive = next;
            next = null;
            current = stack.pop();
            while (stack.size() > 0 && !current.hasNext()) {
                current = stack.pop();
                builder.deleteCharAt(builder.length() - 1);
            }
            builder.deleteCharAt(builder.length() - 1);
            setNext();
            next = builder.isEmpty() ? null : builder.substring(0, builder.length() - 1);
            return nextToGive;
        }

        private void setNext() {
            while (current.hasNext()) {
                Map.Entry<Character, Node> next = current.next();
                stack.push(current);
                builder.append(next.getKey());
                current = next.getValue().children.entrySet().iterator();
            }
        }

        /**
         * Удаление предыдущего элемента
         * <p>
         * Функция удаляет из множества элемент, возвращённый крайним вызовом функции next().
         * <p>
         * Бросает IllegalStateException, если функция была вызвана до первого вызова next() или же была вызвана
         * более одного раза после любого вызова next().
         * <p>
         * Спецификация: {@link Iterator#remove()} (Ctrl+Click по remove)
         * <p>
         * Сложная
         */
        //Сложность O(1)
        @Override
        public void remove() {
//            if (prev == null) {
//                throw new IllegalStateException();
//            }
//            if (root == prevParent) {
//                BinarySearchTree.this.remove(prev.value);
//            } else {
//                BinarySearchTree.this.remove(prevParent, prev.value);
//            }
//            prev = null;
        }
    }

    public static void main(String[] args) {
        Trie trie = new Trie();
        trie.add("D");
        trie.add("Dog");
        Iterator<String> iterator = trie.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        //System.out.println(trie.contains("Artem"));
    }

}