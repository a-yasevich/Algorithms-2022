package lesson6;

import kotlin.NotImplementedError;
import kotlin.Pair;
import lesson6.impl.GraphBuilder;

import java.util.*;

@SuppressWarnings("unused")
public class JavaGraphTasks {
    /**
     * Эйлеров цикл.
     * Средняя
     * <p>
     * Дан граф (получатель). Найти по нему любой Эйлеров цикл.
     * Если в графе нет Эйлеровых циклов, вернуть пустой список.
     * Соседние дуги в списке-результате должны быть инцидентны друг другу,
     * а первая дуга в списке инцидентна последней.
     * Длина списка, если он не пуст, должна быть равна количеству дуг в графе.
     * Веса дуг никак не учитываются.
     * <p>
     * Пример:
     * <p>
     * G -- H
     * |    |
     * A -- B -- C -- D
     * |    |    |    |
     * E    F -- I    |
     * |              |
     * J ------------ K
     * <p>
     * Вариант ответа: A, E, J, K, D, C, H, G, B, C, I, F, B, A
     * <p>
     * Справка: Эйлеров цикл -- это цикл, проходящий через все рёбра
     * связного графа ровно по одному разу
     */
    //Трудоёмкость O(V + E)
    //Рерурсоёмкость O(E)
    public static List<Graph.Edge> findEulerLoop(Graph graph) {
        for (Graph.Vertex vertex : graph.getVertices()) {
            Set<Graph.Vertex> neighbours = graph.getNeighbors(vertex);
            if (neighbours.size() == 0 || neighbours.size() % 2 != 0) {
                return Collections.emptyList();
            }
        }
        Optional<Graph.Vertex> start = graph.getVertices().stream().findFirst();
        if (start.isEmpty()) {
            return Collections.emptyList();
        }
        List<Graph.Edge> loop = new ArrayList<>();
        Set<Graph.Edge> traversedEdges = new HashSet<>();
        eulerLoopDFS(start.get(), null, traversedEdges, loop, graph);
        return loop;
    }

    private static Set<Graph.Vertex> neighboursOnUntraversedEdges(Graph.Vertex vertex, Set<Graph.Edge> traversedEdges, Graph graph) {
        Set<Graph.Vertex> neighbours = new HashSet<>();
        for (Graph.Vertex neighbour : graph.getNeighbors(vertex)) {
            Graph.Edge edgeToNeighbour = graph.getConnection(vertex, neighbour);
            if (!traversedEdges.contains(edgeToNeighbour)) {
                neighbours.add(neighbour);
            }
        }
        return neighbours;
    }

    private static void eulerLoopDFS(Graph.Vertex vertex, Graph.Vertex wentFrom, Set<Graph.Edge> traversedEdges, List<Graph.Edge> loop, Graph graph) {
        if (wentFrom != null) {
            traversedEdges.add(graph.getConnection(wentFrom, vertex));
        }
        Set<Graph.Vertex> neighbours;
        while (!(neighbours = neighboursOnUntraversedEdges(vertex, traversedEdges, graph)).isEmpty()) {
            Graph.Vertex neighbour = neighbours.stream().findFirst().get();
            eulerLoopDFS(neighbour, vertex, traversedEdges, loop, graph);
        }
        if (wentFrom != null) {
            loop.add(graph.getConnection(wentFrom, vertex));
        }
    }

    /**
     * Минимальное остовное дерево.
     * Средняя
     * <p>
     * Дан связный граф (получатель). Найти по нему минимальное остовное дерево.
     * Если есть несколько минимальных остовных деревьев с одинаковым числом дуг,
     * вернуть любое из них. Веса дуг не учитывать.
     * <p>
     * Пример:
     * <p>
     * G -- H
     * |    |
     * A -- B -- C -- D
     * |    |    |    |
     * E    F -- I    |
     * |              |
     * J ------------ K
     * <p>
     * Ответ:
     * <p>
     * G    H
     * |    |
     * A -- B -- C -- D
     * |    |    |
     * E    F    I
     * |
     * J ------------ K
     */
    public static Graph minimumSpanningTree(Graph graph) {
        throw new NotImplementedError();
    }

    /**
     * Максимальное независимое множество вершин в графе без циклов.
     * Сложная
     * <p>
     * Дан граф без циклов (получатель), например
     * <p>
     * G -- H -- J
     * |
     * A -- B -- D
     * |         |
     * C -- F    I
     * |
     * E
     * <p>
     * Найти в нём самое большое независимое множество вершин и вернуть его.
     * Никакая пара вершин в независимом множестве не должна быть связана ребром.
     * <p>
     * Если самых больших множеств несколько, приоритет имеет то из них,
     * в котором вершины расположены раньше во множестве this.vertices (начиная с первых).
     * <p>
     * В данном случае ответ (A, E, F, D, G, J)
     * <p>
     * Если на входе граф с циклами, бросить IllegalArgumentException
     */
    //Трудоёмкость O(V + E)
    //Ресурсоёмкость O(V)
    public static Set<Graph.Vertex> largestIndependentVertexSet(Graph graph) {
        Set<Graph.Vertex> vertices = graph.getVertices();
        if (vertices.isEmpty()) {
            return Collections.emptySet();
        }
        //Вершина -> Пара(Размер независимого множества вершин включающего данную вершину и размер н.м.в. не вкл. данную вершину)
        Map<Graph.Vertex, Pair<Integer, Integer>> d = new HashMap<>();
        Set<Graph.Vertex> livs = new HashSet<>();
        for (Graph.Vertex vertex : vertices) {
            livs.addAll(livsDynamic(graph, d));
        }
        return livs;
    }

    //Трудоёмоксть O(V + E)
    //Ресурсоёмкость O(V)
    private static Set<Graph.Vertex> livsDynamic(Graph graph, Map<Graph.Vertex, Pair<Integer, Integer>> d) {
        Optional<Graph.Vertex> first = graph.getVertices().stream().filter(vertex -> !d.containsKey(vertex)).findFirst();
        if (first.isEmpty()) {
            return Collections.emptySet();
        }
        List<Graph.Vertex> treeNodes = treeNodesBFS(first.get(), d, graph);
        for (int i = treeNodes.size() - 1; i >= 0; i--) {
            Graph.Vertex vertex = treeNodes.get(i);
            int includingCurrent = 1;
            int excludingCurrent = 0;
            for (Graph.Vertex neighbour : graph.getNeighbors(vertex)) {
                Pair<Integer, Integer> pair = d.get(neighbour);
                if (pair == null) {
                    //Это родитель данной вершины
                    continue;
                }
                includingCurrent += pair.getSecond();
                excludingCurrent += Math.max(pair.getFirst(), pair.getSecond());
            }
            d.put(vertex, new Pair<>(includingCurrent, excludingCurrent));
        }
        Set<Graph.Vertex> answer = new HashSet<>();
        makeAnswerDFS(first.get(), null, false, d, answer, graph);
        return answer;
    }

    //Трудоёмоксть O(V + E)
    //Ресурсоёмкость O(V)
    private static List<Graph.Vertex> treeNodesBFS(Graph.Vertex first, Map<Graph.Vertex, Pair<Integer, Integer>> d, Graph graph) {
        Queue<Graph.Vertex> queue = new ArrayDeque<>();
        List<Graph.Vertex> treeNodes = new ArrayList<>();
        queue.add(first);
        treeNodes.add(first);
        d.put(first, null);
        while (!queue.isEmpty()) {
            Graph.Vertex vertex = queue.remove();
            Set<Graph.Vertex> neighbours = graph.getNeighbors(vertex);
            neighbours.removeAll(d.keySet());
            treeNodes.addAll(neighbours);
            queue.addAll(neighbours);
            d.put(vertex, null);
        }
        return treeNodes;
    }

    //O(V + E)
    private static void makeAnswerDFS(Graph.Vertex vertex, Graph.Vertex parent, boolean parentIncluded,
                                      Map<Graph.Vertex, Pair<Integer, Integer>> d, Set<Graph.Vertex> ans, Graph graph) {
        boolean includeCurrent = !parentIncluded && (d.get(vertex).getFirst() >= d.get(vertex).getSecond());
        if (includeCurrent) {
            ans.add(vertex);
        }
        Set<Graph.Vertex> neighbours = graph.getNeighbors(vertex);
        neighbours.remove(parent);
        for (Graph.Vertex neighbour : neighbours) {
            makeAnswerDFS(neighbour, vertex, includeCurrent, d, ans, graph);
        }
    }

    /**
     * Наидлиннейший простой путь.
     * Сложная
     * <p>
     * Дан граф (получатель). Найти в нём простой путь, включающий максимальное количество рёбер.
     * Простым считается путь, вершины в котором не повторяются.
     * Если таких путей несколько, вернуть любой из них.
     * <p>
     * Пример:
     * <p>
     * G -- H
     * |    |
     * A -- B -- C -- D
     * |    |    |    |
     * E    F -- I    |
     * |              |
     * J ------------ K
     * <p>
     * Ответ: A, E, J, K, D, C, H, G, B, F, I
     */
    public static Path longestSimplePath(Graph graph) {
        throw new NotImplementedError();
    }


    /**
     * Балда
     * Сложная
     * <p>
     * Задача хоть и не использует граф напрямую, но решение базируется на тех же алгоритмах -
     * поэтому задача присутствует в этом разделе
     * <p>
     * В файле с именем inputName задана матрица из букв в следующем формате
     * (отдельные буквы в ряду разделены пробелами):
     * <p>
     * И Т Ы Н
     * К Р А Н
     * А К В А
     * <p>
     * В аргументе words содержится множество слов для поиска, например,
     * ТРАВА, КРАН, АКВА, НАРТЫ, РАК.
     * <p>
     * Попытаться найти каждое из слов в матрице букв, используя правила игры БАЛДА,
     * и вернуть множество найденных слов. В данном случае:
     * ТРАВА, КРАН, АКВА, НАРТЫ
     * <p>
     * И т Ы Н     И т ы Н
     * К р а Н     К р а н
     * А К в а     А К В А
     * <p>
     * Все слова и буквы -- русские или английские, прописные.
     * В файле буквы разделены пробелами, строки -- переносами строк.
     * Остальные символы ни в файле, ни в словах не допускаются.
     */
    static public Set<String> baldaSearcher(String inputName, Set<String> words) {
        throw new NotImplementedError();
    }

}
