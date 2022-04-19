package lesson6;

import kotlin.NotImplementedError;
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
    public static Set<Graph.Vertex> largestIndependentVertexSet(Graph graph) {
        Optional<Graph.Vertex> first = graph.getVertices().stream().findFirst();
        if (first.isEmpty()) {
            return new HashSet<>();
        }
        Set<Graph.Vertex> res = new HashSet<>();
        Set<Pair<Graph.Vertex, Boolean>> visitedSet = new HashSet<>();
        for (Graph.Vertex vertex : graph.getVertices()) {
            Set<Graph.Vertex> inclusive = recursiveLIVS(graph, visitedSet, vertex, null, true);
            Set<Graph.Vertex> exclusives = recursiveLIVS(graph, visitedSet, vertex, null, false);
            Set<Graph.Vertex> biggest = inclusive.size() >= exclusives.size() ? inclusive : exclusives;
            res.addAll(biggest);
        }
        return res;
    }

    private static Set<Graph.Vertex> recursiveLIVS(Graph graph, Set<Pair<Graph.Vertex, Boolean>> visitedSet, Graph.Vertex vertex, Graph.Vertex from, boolean include) {
        Set<Graph.Vertex> neighbors = graph.getNeighbors(vertex);
        if (visitedSet.contains(new Pair<>(vertex, include))) {
            return Collections.emptySet();
        }
        if (from != null) {
            neighbors.remove(from);
        }
        visitedSet.add(new Pair<>(vertex, include));
        Set<Graph.Vertex> res = new HashSet<>();
        if (neighbors.isEmpty()) {
            if (include) {
                res.add(vertex);
            }
            return res;
        }
        if (include) {
            res.add(vertex);
            for (Graph.Vertex neighbour : neighbors) {
                res.addAll(recursiveLIVS(graph, visitedSet, neighbour, vertex, false));
            }
            return res;
        }
        for (Graph.Vertex neighbour : neighbors) {
            Set<Graph.Vertex> inclusive = recursiveLIVS(graph, visitedSet, neighbour, vertex, true);
            Set<Graph.Vertex> exclusives = recursiveLIVS(graph, visitedSet, neighbour, vertex, false);
            Set<Graph.Vertex> biggest = inclusive.size() >= exclusives.size() ? inclusive : exclusives;
            res.addAll(biggest);
        }
        return res;
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

    public static void main(String[] args) {
        GraphBuilder builder = new GraphBuilder();
        Graph.Vertex d = builder.addVertex("D");
        Graph.Vertex e = builder.addVertex("E");
        Graph.Vertex f = builder.addVertex("F");
        Graph.Vertex a = builder.addVertex("A");
        Graph.Vertex b = builder.addVertex("B");
        Graph.Vertex c = builder.addVertex("C");
        builder.addConnection(a, b, 0);
        builder.addConnection(b, c, 0);
        builder.addConnection(c, d, 0);
        builder.addConnection(d, a, 0);
        builder.addConnection(d, e, 0);
        builder.addConnection(e, f, 0);
        builder.addConnection(f, d, 0);

        System.out.println("Res = " + findEulerLoop(builder.build()));
    }
}
