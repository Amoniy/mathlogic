package hw4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    private static Integer[] verticesIndexInTopSort;
    private static Integer[] reversedVertices;
    private static int[][] comparability;
    private static int[][] plus;
    private static int[][] multiply;
    private static int[][] implicate;
    private static int vertices;
    private static ArrayList<Integer>[] graph;
    private static ArrayList<Integer>[] reversedGraph;
    private static ArrayList<Integer> ans;

    private static void dfs(int v, boolean[] used) {
        used[v] = true;
        for (int i = 0; i < graph[v].size(); i++) {
            int to = graph[v].get(i);
            if (!used[to]) {
                dfs(to, used);
            }
        }
        ans.add(v);
    }

    private static void topological_sort(boolean[] used) {
        for (int i = 0; i < vertices; i++) {
            if (!used[i]) {
                dfs(i, used);
            }
        }
        Collections.reverse(ans);
    }

    private static void dfsForComparability(int initialVertex, int v, boolean[] used) {
        used[v] = true;
        for (int i = 0; i < graph[v].size(); i++) {
            int to = graph[v].get(i);
            comparability[initialVertex][to] = -1;
            comparability[to][initialVertex] = 1;
            if (!used[to]) {
                dfsForComparability(initialVertex, to, used);
            }
        }
    }

    private static boolean plus(int a, int b) {
        ArrayList<Integer> biggerVertices = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            if (comparability[i][a] == 1 && comparability[i][b] == 1) {
                biggerVertices.add(i);
            }
        }
        if(comparability[a][b] == 1){
            biggerVertices.add(a);
        }
        if(comparability[a][b] == -1){
            biggerVertices.add(b);
        }

        if (biggerVertices.size() == 0) {
            return false;
        }
        int min = -1;
        for (int i = 0; i < biggerVertices.size(); i++) {
            boolean flag = true;
            for (int j = 0; j < biggerVertices.size(); j++) { // путь от i до j
                if (i == j) {
                    continue;
                }
                if (comparability[biggerVertices.get(i)][biggerVertices.get(j)] != -1) {
                    flag = false;
                    break;
                }
            }
            if (flag) { // из i достижимы все вершины
                min = biggerVertices.get(i);
            }
        }
        if (min == -1) {
            return false;
        }
        plus[a][b] = min;
        plus[b][a] = min;
        return true;
    }

    private static boolean mult(int a, int b) {
        ArrayList<Integer> smallerVertices = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            if (comparability[i][a] == -1 && comparability[i][b] == -1) {
                smallerVertices.add(i);
            }
        }
        if(comparability[a][b] == -1){
            smallerVertices.add(a);
        }
        if(comparability[a][b] == 1){
            smallerVertices.add(b);
        }

        if (smallerVertices.size() == 0) {
            return false;
        }
        int max = -1;
        for (int i = 0; i < smallerVertices.size(); i++) {
            boolean flag = true;
            for (int j = 0; j < smallerVertices.size(); j++) { // путь от i до j
                if (i == j) {
                    continue;
                }
                if (comparability[smallerVertices.get(i)][smallerVertices.get(j)] != 1) {
                    flag = false;
                    break;
                }
            }
            if (flag) { // из i достижимы все вершины
                max = smallerVertices.get(i);
            }
        }
        if (max == -1) {
            return false;
        }
        multiply[a][b] = max;
        multiply[b][a] = max;
        return true;
    }

    private static boolean isDistr(int a, int b, int c) {
        return (multiply[c][plus[a][b]] == plus[multiply[c][a]][multiply[c][b]]);
    }

    private static boolean impl(int a, int b) {
        ArrayList<Integer> smallerVertices = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            int k = multiply[i][a];
            if (comparability[k][b] == -1 || k == b) {
                smallerVertices.add(i);
            }
        }

        if (smallerVertices.size() == 0) {
            return false;
        }
        int max = -1;
        for (int i = 0; i < smallerVertices.size(); i++) {
            boolean flag = true;
            for (int j = 0; j < smallerVertices.size(); j++) { // путь от i до j
                if (i == j) {
                    continue;
                }
                if (comparability[smallerVertices.get(i)][smallerVertices.get(j)] != 1) {
                    flag = false;
                    break;
                }
            }
            if (flag) { // из i достижимы все вершины
                max = smallerVertices.get(i);
            }
        }
        if (max == -1) {
            return false;
        }
        implicate[a][b] = max;
        return true;
    }

    private static int zero = -1;
    private static int one = -1;

    private static boolean isAlgebr(int a) {
        return (plus[a][implicate[a][zero]] == one);
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        FileWriter writer = new FileWriter("output.txt");
        String spaces = "[ ]+";
        vertices = Integer.parseInt(reader.readLine());
        graph = new ArrayList[vertices];
        reversedGraph = new ArrayList[vertices];
        for (int i = 0; i < vertices; i++) {
            graph[i] = new ArrayList<>();
            reversedGraph[i] = new ArrayList<>();
        }
        for (int i = 0; i < vertices; i++) {
            String[] currentPaths = reader.readLine().split(spaces);
            for (int j = 0; j < currentPaths.length; j++) {
                int to = Integer.parseInt(currentPaths[j]);
                if (to == i + 1) {
                    continue;
                }
                graph[i].add(to - 1); // нумерация все же с нуля
                reversedGraph[to - 1].add(i);
            }
        }
        boolean[] used = new boolean[vertices];
        ans = new ArrayList<>();
        topological_sort(used);
        comparability = new int[vertices][vertices];
        verticesIndexInTopSort = new Integer[vertices];
        for (int i = 0; i < vertices; i++) {
            verticesIndexInTopSort[ans.get(i)] = i; // vIITS - номер итой вершины в топсорте
        }
        for (int i = 0; i < vertices; i++) {
            boolean[] newUsed = new boolean[vertices];
            dfsForComparability(i, i, newUsed);
        }
        // 0 - not comparable; a[i][j] = 1 => i > j; -1 => i < j
        // ans хранит порядок вершин в топ сорте. все опять-таки нумеруется с нуля

        //
        reversedVertices = Arrays.copyOf(verticesIndexInTopSort, vertices);
        List<Integer> a = Arrays.asList(reversedVertices);
        Collections.reverse(a);
        reversedVertices = (Integer[]) a.toArray();
        //

        plus = new int[vertices][vertices];
        multiply = new int[vertices][vertices];
        implicate = new int[vertices][vertices];

        for (int i = 0; i < vertices; i++) {
            plus[i][i] = i;
            multiply[i][i] = i;
        }

        for (int i = 0; i < vertices; i++) {
            for (int j = i + 1; j < vertices; j++) { // а может и не от i + 1 пока хз
                if (!plus(i, j)) {
                    writer.write(String.format("Операция '+' не определена: %d+%d\n", (i + 1), (j + 1)));
                    writer.close();
                    return;
                }
            }
        }

        for (int i = 0; i < vertices; i++) {
            for (int j = i + 1; j < vertices; j++) {
                if (!mult(i, j)) {
                    writer.write(String.format("Операция '*' не определена: %d*%d\n", (i + 1), (j + 1)));
                    writer.close();
                    return;
                }
            }
        }

        for (int i = 0; i < vertices; i++) {
            for (int j = i + 1; j < vertices; j++) {
                for (int k = 0; k < vertices; k++) {
                    if (!isDistr(i, j, k)) {
                        writer.write(String.format("Нарушается дистрибутивность: %d*(%d+%d)\n", (k + 1), (i + 1), (j + 1)));
                        writer.close();
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) { // тк нельзя разворачивать
                if (!impl(i, j)) {
                    writer.write(String.format("Операция '->' не определена: %d->%d\n", (i + 1), (j + 1)));
                    writer.close();
                    return;
                }
            }
        }

        for (int i = 0; i < vertices; i++) {
            boolean flag = true;
            for (int j = 0; j < vertices; j++) { // путь от i до j
                if (i == j) {
                    continue;
                }
                if (comparability[i][j] != -1) {
                    flag = false;
                    break;
                }
            }
            if (flag) { // из i достижимы все вершины
                zero = i;
            }
        }

        for (int i = 0; i < vertices; i++) {
            boolean flag = true;
            for (int j = 0; j < vertices; j++) { // путь от i до j
                if (i == j) {
                    continue;
                }
                if (comparability[i][j] != 1) {
                    flag = false;
                    break;
                }
            }
            if (flag) { // i достижима из всех вершин
                one = i;
            }
        }

        for (int i = 0; i < vertices; i++) {
            if (!isAlgebr(i)) {
                writer.write(String.format("Не булева алгебра: %d+~%d\n", (i + 1), (i + 1)));
                writer.close();
                return;
            }
        }

        writer.write("Булева алгебра\n");
        writer.close();
    }
}
