package lesson3;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// attention: Comparable is supported but Comparator is not
public class BinarySearchTree<T extends Comparable<T>> extends AbstractSet<T> implements CheckableSortedSet<T> {

    private static class Node<T> {
        final T value;
        Node<T> left = null;
        Node<T> right = null;

        Node(T value) {
            this.value = value;
        }

    }

    private Node<T> root = null;
    private final List<SubTree<T>> subTrees = new ArrayList<>();
    int size = 0;

    //Сложность O(1)
    @Override
    public int size() {
        return size;
    }

    private Node<T> find(T value) {
        if (root == null) return null;
        return find(root, value);
    }

    private Node<T> find(Node<T> start, T value) {
        int comparison = value.compareTo(start.value);
        if (comparison == 0) {
            return start;
        } else if (comparison < 0) {
            if (start.left == null) return start;
            return find(start.left, value);
        } else {
            if (start.right == null) return start;
            return find(start.right, value);
        }
    }

    //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        T t = (T) o;
        Node<T> closest = find(t);
        return closest != null && t.compareTo(closest.value) == 0;
    }

    /**
     * Добавление элемента в дерево
     * <p>
     * Если элемента нет в множестве, функция добавляет его в дерево и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * <p>
     * Спецификация: {@link Set#add(Object)} (Ctrl+Click по add)
     * <p>
     * Пример
     */
    //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
    @Override
    public boolean add(T t) {
        Node<T> closest = find(t);
        int comparison = closest == null ? -1 : t.compareTo(closest.value);
        if (comparison == 0) {
            return false;
        }
        Node<T> newNode = new Node<>(t);
        if (closest == null) {
            root = newNode;
        } else if (comparison < 0) {
            assert closest.left == null;
            closest.left = newNode;
        } else {
            assert closest.right == null;
            closest.right = newNode;
        }
        size++;
        for (SubTree<T> subTree : subTrees) {
            if (subTree.isValueValid(t)) {
                subTree.size++;
            }
        }
        return true;
    }

    /**
     * Удаление элемента из дерева
     * <p>
     * Если элемент есть в множестве, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     * <p>
     * Спецификация: {@link Set#remove(Object)} (Ctrl+Click по remove)
     * <p>
     * Средняя
     */
    //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
    @Override
    public boolean remove(Object o) {
        if (root == null) {
            return false;
        }
        T value = (T) o;
        int sizeBefore = size;
        root = remove(root, value);
        return size == sizeBefore - 1;
    }

    private Node<T> remove(Node<T> start, T value) {
        if (start == null) {
            return null;
        }
        int comparison = value.compareTo(start.value);
        if (comparison < 0) {
            start.left = remove(start.left, value);
        } else if (comparison > 0) {
            start.right = remove(start.right, value);
        } else {
            size--;
            for (SubTree<T> subTree : subTrees) {
                if (subTree.isValueValid(value)) {
                    subTree.size--;
                }
            }
            start = innerRemove(start);
        }
        return start;
    }

    private Node<T> innerRemove(Node<T> node) {
        if (node.left == null) {
            return node.right;
        }
        if (node.right == null) {
            return node.left;
        }
        Node<T> oldCopy = node;

        node = min(oldCopy.right);
        node.right = deleteMin(oldCopy.right);
        node.left = oldCopy.left;
        return node;
    }

    private Node<T> deleteMin(Node<T> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMin(node.left);
        return node;
    }

    private Node<T> min(Node<T> node) {
        if (node == null) {
            return null;
        }
        if (node.left == null) {
            return node;
        }
        return min(node.left);
    }

    @Nullable
    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
    //Ресурсоёмкость O(n)
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new BinarySearchTreeIterator();
    }

    public class BinarySearchTreeIterator implements Iterator<T> {
        private final Deque<Node<T>> stack;
        private Node<T> prev;
        private Node<T> prevParent;

        private BinarySearchTreeIterator() {
            stack = new ArrayDeque<>();
            for (Node<T> curr = root; curr != null; curr = curr.left) {
                stack.push(curr);
            }
        }

        /**
         * Проверка наличия следующего элемента
         * <p>
         * Функция возвращает true, если итерация по множеству ещё не окончена (то есть, если вызов next() вернёт
         * следующий элемент множества, а не бросит исключение); иначе возвращает false.
         * <p>
         * Спецификация: {@link Iterator#hasNext()} (Ctrl+Click по hasNext)
         * <p>
         * Средняя
         */
        //Сложность O(1)
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /**
         * Получение следующего элемента
         * <p>
         * Функция возвращает следующий элемент множества.
         * Так как BinarySearchTree реализует интерфейс SortedSet, последовательные
         * вызовы next() должны возвращать элементы в порядке возрастания.
         * <p>
         * Бросает NoSuchElementException, если все элементы уже были возвращены.
         * <p>
         * Спецификация: {@link Iterator#next()} (Ctrl+Click по next)
         * <p>
         * Средняя
         */
        //Сложность O(log(n))
        @Override
        public T next() {
            if (stack.isEmpty()) {
                throw new NoSuchElementException();
            }

            Node<T> popped = stack.pop();
            prev = popped;
            prevParent = stack.isEmpty() ? root : stack.peek();
            for (Node<T> curr = popped.right; curr != null; curr = curr.left) {
                stack.push(curr);
            }
            return popped.value;
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
            if (prev == null) {
                throw new IllegalStateException();
            }
            if (root == prevParent) {
                BinarySearchTree.this.remove(prev.value);
            } else {
                BinarySearchTree.this.remove(prevParent, prev.value);
            }
            prev = null;
        }
    }

    /**
     * Подмножество всех элементов в диапазоне [fromElement, toElement)
     * <p>
     * Функция возвращает множество, содержащее в себе все элементы дерева, которые
     * больше или равны fromElement и строго меньше toElement.
     * При равенстве fromElement и toElement возвращается пустое множество.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     * <p>
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     * <p>
     * Спецификация: {@link SortedSet#subSet(Object, Object)} (Ctrl+Click по subSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     * <p>
     * Очень сложная (в том случае, если спецификация реализуется в полном объёме)
     */
    //Сложность O(1)
    @NotNull
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        SubTree<T> subTree = new SubTree<>(fromElement, toElement, this);
        subTrees.add(subTree);
        return subTree;
    }

    /**
     * Подмножество всех элементов строго меньше заданного
     * <p>
     * Функция возвращает множество, содержащее в себе все элементы дерева строго меньше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     * <p>
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     * <p>
     * Спецификация: {@link SortedSet#headSet(Object)} (Ctrl+Click по headSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     * <p>
     * Сложная
     */
    //Сложность O(1)
    @NotNull
    @Override
    public SortedSet<T> headSet(T toElement) {
        SubTree<T> subTree = new SubTree<>(null, toElement, this);
        subTrees.add(subTree);
        return subTree;
    }

    /**
     * Подмножество всех элементов нестрого больше заданного
     * <p>
     * Функция возвращает множество, содержащее в себе все элементы дерева нестрого больше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     * <p>
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     * <p>
     * Спецификация: {@link SortedSet#tailSet(Object)} (Ctrl+Click по tailSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     * <p>
     * Сложная
     */
    //Сложность O(1)
    @NotNull
    @Override
    public SortedSet<T> tailSet(T fromElement) {
        SubTree<T> subTree = new SubTree<>(fromElement, null, this);
        subTrees.add(subTree);
        return subTree;
    }

    //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
    @Override
    public T first() {
        if (root == null) throw new NoSuchElementException();
        Node<T> current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current.value;
    }

    //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
    @Override
    public T last() {
        if (root == null) throw new NoSuchElementException();
        Node<T> current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.value;
    }

    public int height() {
        return height(root);
    }

    private int height(Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public boolean checkInvariant() {
        return root == null || checkInvariant(root);
    }

    private boolean checkInvariant(Node<T> node) {
        Node<T> left = node.left;
        if (left != null && (left.value.compareTo(node.value) >= 0 || !checkInvariant(left))) return false;
        Node<T> right = node.right;
        return right == null || right.value.compareTo(node.value) > 0 && checkInvariant(right);
    }

    private static class SubTree<T extends Comparable<T>> extends BinarySearchTree<T> {
        private final T fromElement;
        private final T toElement;
        private final BinarySearchTree<T> parentTree;

        public SubTree(T fromElement, T toElement, BinarySearchTree<T> parentTree) {
            this.fromElement = fromElement;
            this.toElement = toElement;
            this.parentTree = parentTree;
        }

        //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
        @Override
        public boolean contains(Object o) {
            T t = (T) o;
            if (!isValueValid(t)) {
                return false;
            }
            return parentTree.contains(o);
        }

        //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
        @Override
        public boolean add(T t) {
            if (!isValueValid(t)) {
                throw new IllegalArgumentException();
            }
            return parentTree.add(t);
        }

        //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
        @Override
        public boolean remove(Object o) {
            T t = (T) o;
            if (!isValueValid(t)) {
                throw new IllegalArgumentException();
            }
            return parentTree.remove(o);
        }

        //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
        @Override
        public T first() {
            Node<T> closest = findFirst(parentTree.root);
            if (closest == null) {
                throw new NoSuchElementException();
            }
            return closest.value;
        }

        //Сложность O(log(n)) - в среднем, O(n) - в худшем случае
        @Override
        public T last() {
            Node<T> closest = findLast(parentTree.root);
            if (closest == null) {
                throw new NoSuchElementException();
            }
            return closest.value;
        }

        private Node<T> findFirst(Node<T> start) {
            if (start == null) {
                return null;
            }
            int comparison = fromElement.compareTo(start.value);
            if (comparison == 0) {
                return start;
            }
            Node<T> ok = !isValueValid(start.value) ? null : start;
            if (comparison < 0) {
                if (start.left == null) return ok;
                Node<T> smaller = findFirst(start.left);
                return smaller == null ? ok : smaller;

            } else {
                if (start.right == null) return ok;
                Node<T> bigger = findFirst(start.right);
                return ok == null ? bigger : ok;
            }

        }

        private Node<T> findLast(Node<T> start) {
            if (start == null) {
                return null;
            }
            int comparison = toElement.compareTo(start.value);
            Node<T> ok = !isValueValid(start.value) ? null : start;
            if (comparison <= 0) {
                if (start.left == null) return ok;
                Node<T> smaller = findLast(start.left);
                return ok == null ? smaller : ok;

            } else {
                if (start.right == null) return ok;
                Node<T> bigger = findLast(start.right);
                return bigger == null ? ok : bigger;
            }

        }

        private boolean isValueValid(T value) {
            return (fromElement == null || value.compareTo(fromElement) >= 0)
                    && (toElement == null || value.compareTo(toElement) < 0);
        }
    }

}