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
        private Iterator<Map.Entry<Character, Node>> removeFrom;

        private PrefixTreeIterator() {
            stack = new ArrayDeque<>();
            Iterator<Map.Entry<Character, Node>> rootIterator = root.children.entrySet().iterator(); //first letters iterator
            if (rootIterator.hasNext()) {
                stack.push(rootIterator);
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public String next() {
            setNext();
            String next = builder.substring(0, builder.length() - 1);
            if (builder.isEmpty()) {
                throw new NoSuchElementException();
            }
            resetToTheStartOfNextWord();
            return next;
        }

        private void setNext() {
            Iterator<Map.Entry<Character, Node>> cur = stack.pop();
            removeFrom = cur;
            Iterator<Map.Entry<Character, Node>> fork = cur;

            while (cur.hasNext()) {
                Map.Entry<Character, Node> next = cur.next();
                if (cur.hasNext()) {
                    fork = cur;
                }
                builder.append(next.getKey());
                stack.push(cur);
                cur = next.getValue().children.entrySet().iterator();
            }
            removeFrom = fork;
        }

        private void resetToTheStartOfNextWord() {
            //deleting Nth letter to select another
            builder.deleteCharAt(builder.length() - 1);
            while (stack.size() > 0 && !stack.peek().hasNext()) {
                //no another letter n => deleting letter (n - 1) to select another (n - 1)th letter
                if (stack.size() == 1 && !stack.peek().hasNext()) {
                    //no another 1st letter
                    stack.pop();
                    return;
                }
                stack.pop();
                builder.deleteCharAt(builder.length() - 1);
            }
        }

        @Override
        public void remove() {
            if (removeFrom == null) {
                throw new IllegalStateException();
            }
            removeFrom.remove();
            size--;
        }
    }
}