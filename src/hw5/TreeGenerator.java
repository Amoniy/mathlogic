package hw5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TreeGenerator {

    private final int N = 6;

    private int[] generateNextBracket(int[] brackets) {
        int opens = 0;
        int closes = 0;
        for (int i = brackets.length - 1; i >= 0; i--) {
            if (brackets[i] == 1) {
                opens++;
                if (closes > opens) {
                    break;
                }
            } else {
                closes++;
            }
        }
        int statCloses = closes;
        int statOpens = opens;
        brackets[brackets.length - statCloses - statOpens] = -1;
        for (int i = brackets.length - statCloses - statOpens + 1; i < brackets.length; i++) {
            if (opens > 0) {
                brackets[i] = 1;
                opens--;
            } else {
                brackets[i] = -1;
            }
        }
        return brackets;
    }

    public class Node {

        private int number;
        private Node parent;
        private List<Node> children;
        private String tree;
        private boolean[] vars;

        private Node(String bracketSequence, int vars) {
            this.vars = new boolean[vars];
            children = new ArrayList<>();
            this.tree = bracketSequence;
            int balance = 0;
            int startingIndex = 0;
            for (int currentIndex = 0; currentIndex < bracketSequence.length(); currentIndex++) {
                if (bracketSequence.charAt(currentIndex) == '(') {
                    balance++;
                } else {
                    balance--;
                }
                if (balance == 0) {
                    String childString = bracketSequence.substring(startingIndex + 1, currentIndex);
                    // +1 чтобы обрезать крайние скобки самого узла (()) -> ()
                    if (childString.length() > 0) {
                        Node child = new Node(childString, vars);
                        children.add(child);
                        child.parent = this;
                    } else {
                        Node child = new Node(vars);
                        children.add(child);
                        child.parent = this;
                    }
                    startingIndex = currentIndex + 1;
                }
            }
        }

        private Node(int vars) {
            children = new ArrayList<>();
            this.tree = "";
            this.vars = new boolean[vars];
        }

        @Override
        public String toString() {
            return String.format("%s", tree);
        }
    }

    private boolean hasNext(Node tree, int vars) {
        for (int i = 0; i < tree.children.size(); i++) {
            if (hasNext(tree.children.get(i), vars)) {
                return true;
            }
        }
        for (int i = 0; i < tree.vars.length; i++) {
            if (!tree.vars[i]) { // есть хотя бы один 0 значит еще можем увеличить
                return true;
            }
        }
        return false;
    }

    private void siftDown(Node tree) {
        for (int i = 0; i < tree.vars.length; i++) {
            tree.vars[i] = tree.parent.vars[i];
        }
        for (int i = 0; i < tree.children.size(); i++) {
            siftDown(tree.children.get(i));
        }
    }

    private void generateNextTree(Node tree, int vars) {
        for (int i = 0; i < tree.children.size(); i++) {
            if (hasNext(tree.children.get(i), vars)) {
                generateNextTree(tree.children.get(i), vars);
                for (int j = 0; j < i; j++) {
                    siftDown(tree.children.get(j));
                }
                return;
            }
        }
        if (tree.parent == null) {
            for (int i = 0; i < tree.vars.length; i++) {
                if (tree.vars[i]) {
                    tree.vars[i] = false;
                } else {
                    tree.vars[i] = true;
                    break;
                }
            }
        } else { // тут нужно учитывать, что уже вынуждено в родителе
            for (int i = 0; i < tree.vars.length; i++) {
                if (tree.vars[i] && (!tree.parent.vars[i])) { // в родителе 0, у нас 1
                    tree.vars[i] = false;
                } else if (!tree.vars[i]) { // 0 точно можем менять
                    tree.vars[i] = true;
                    return;
                } else { // у нас 1, в родителе 1
                    tree.vars[i] = false;
                }
            }
        }
    }

    private boolean checkTree(Expression expression, Node tree, int vars) {
        if (!singleCheck(expression, tree, vars)) {
            return false;
        }
        while (true) {
            generateNextTree(tree, vars);
            if (!singleCheck(expression, tree, vars)) {
                singleCheck(expression, tree, vars);
                return false;
            }
            if (!hasNext(tree, vars)) {
                break;
            }
        }

        return true;
    }

    private boolean singleCheck(Expression expression, Node tree, int vars) {
        return isForced(expression, tree);
    }

    private List<Node> getAllSuccessors(Node world) {
        List<Node> successors = new ArrayList<>();
        successors.add(world);
        for (int i = 0; i < world.children.size(); i++) {
            successors.addAll(getAllSuccessors(world.children.get(i)));
        }
        return successors;
    }

    private boolean isForced(Expression expression, Node world) {
        if (expression.getClass() == Or.class) {
            return (isForced(expression.getLeft(), world) || isForced(expression.getRight(), world));
        }
        if (expression.getClass() == And.class) {
            return (isForced(expression.getLeft(), world) && isForced(expression.getRight(), world));
        }
        if (expression.getClass() == Negate.class) {
            List<Node> successors = getAllSuccessors(world);
            for (int i = 0; i < successors.size(); i++) {
                if (isForced(expression.getSingle(), successors.get(i))) {
                    return false;
                }
            }
            return true;
        }
        if (expression.getClass() == Implicate.class) {
            List<Node> successors = getAllSuccessors(world);
            for (int i = 0; i < successors.size(); i++) {
                if (isForced(expression.getLeft(), successors.get(i)) &&
                        (!isForced(expression.getRight(), successors.get(i)))) {
                    return false;
                }
            }
            return true;
        }

        return checkVar((Variable) expression, world);
    }

    private boolean checkVar(Variable var, Node world) { // переменная кодируется ее порядком в маске
        return world.vars[var.number];
    }

    public boolean check(Expression expression, List<Node> trees, int vars, Parser parser) {
        for (int i = 0; i < trees.size(); i++) {
            if (!checkTree(expression, trees.get(i), vars)) {
                convertToTopology(trees.get(i), vars, parser);
                return false;
            }
        }
        return true;
    }

    private void convertToTopology(Node tree, int vars, Parser parser) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(tree);
        int indexCount = 1;
        Node[] nodes = new Node[N + 1];
        while (queue.size() > 0) { // подсчет размера дерева +1 и заполнение nodes[]
            Node currentNode = queue.poll();
            currentNode.number = indexCount;
            nodes[indexCount] = currentNode;
            indexCount++;
            queue.addAll(currentNode.children);
        }

        HashSet<HashSet<Integer>> sets = new HashSet<>();

        int LARGE_NUMBER = 10000; // на самом деле должно быть вообще 2 в 6 степени
        HashSet[] hashSetByNumber = new HashSet[LARGE_NUMBER];
        int currentIndex = 1;

        HashSet<Integer> emptySet = new HashSet<>();
        sets.add(emptySet); // empty set
        hashSetByNumber[currentIndex++] = emptySet;

        HashMap<HashSet<Integer>, Integer> numberForVertex = new HashMap<>();
        numberForVertex.put(emptySet, 1);
        int[] varVertices = new int[vars];

        for (int currentVar = 0; currentVar < vars; currentVar++) { // перебор переменных
            HashSet<Integer> currentSet = new HashSet<>();
            for (int i = 1; i < indexCount; i++) { // перебор миров в которых она вынуждена
                Node currentNode = nodes[i];
                if (currentNode.vars[currentVar]) {
                    currentSet.add(currentNode.number);
                }
            }
            if (!sets.contains(currentSet)) {
                sets.add(currentSet);
                numberForVertex.put(currentSet, currentIndex);
                hashSetByNumber[currentIndex++] = currentSet;
            }
            if (numberForVertex.containsKey(currentSet)) { // такое множество вершин уже было заиспользовано
                varVertices[currentVar] = numberForVertex.get(currentSet);
            } else {
                numberForVertex.put(currentSet, -1); // невозможно
            }
        }

        HashSet<Integer> fullSet = new HashSet<>();
        for (int i = 1; i < indexCount; i++) {
            fullSet.add(i);
        }

        int previousSize = sets.size();
        while (true) {
            for (int i = 1; i < currentIndex; i++) {
                for (int j = 1; j < currentIndex; j++) {
                    HashSet<Integer> left = hashSetByNumber[i];
                    HashSet<Integer> right = hashSetByNumber[j];

//                    addOr(sets, hashSetByNumber, currentIndex);
                    HashSet<Integer> union = new HashSet<>();
                    union.addAll(left);
                    union.addAll(right);
                    if (!sets.contains(union)) {
                        hashSetByNumber[currentIndex++] = union;
                        sets.add(union);
                    }

//                    addAnd(sets, hashSetByNumber, currentIndex);
                    HashSet<Integer> intersection = new HashSet<>();
                    for (int k : union) {
                        if (left.contains(k) && right.contains(k)) {
                            intersection.add(k);
                        }
                    }
                    if (intersection.size() > 0) {
                        if (!sets.contains(intersection)) {
                            hashSetByNumber[currentIndex++] = intersection;
                            sets.add(intersection);
                        }
                    }

                    HashSet<Integer> c = new HashSet<>();
                    c.addAll(fullSet);
                    for (int k : left) {
                        c.remove(k);
                    }
                    c.addAll(right);
                    // TODO
                    HashSet<Integer> savingSet = new HashSet<>();
                    savingSet.addAll(c);
                    for (int k : c) {
                        if (allBad(k, tree, c)) {
                            savingSet.remove(k);
                        }
                    }
                    if (savingSet.size() > 0) {
                        if (!sets.contains(savingSet)) {
                            hashSetByNumber[currentIndex++] = savingSet;
                            sets.add(savingSet);
                        }
                    }
                }
            }

            if (previousSize == sets.size()) {
                break;
            }
            previousSize = sets.size();
        } // получили топологию

        int[][] matrix = new int[currentIndex][currentIndex];
        for (int i = 1; i < currentIndex; i++) {
            for (int j = 1; j < currentIndex; j++) {
                if (setCheck(hashSetByNumber[i], hashSetByNumber[j])) { // i содержит j
                    matrix[j][i] = 1;
                } else if (setCheck(hashSetByNumber[j], hashSetByNumber[i])) {
                    matrix[i][j] = 1;
                }
//                if (hashSetByNumber[i].contains(hashSetByNumber[j])) {
//                    matrix[i][j] = -1;
//                    matrix[j][i] = 1;
//                } else if (hashSetByNumber[j].contains(hashSetByNumber[i])) {
//                    matrix[i][j] = 1;
//                    matrix[j][i] = -1;
//                }
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(currentIndex - 1);
        System.out.println(builder.toString());
        for (int i = 1; i < currentIndex; i++) {
            builder = new StringBuilder();
            for (int j = 1; j < currentIndex; j++) {
                if (matrix[i][j] == 1) {
                    builder.append(j);
                    builder.append(" ");
                }
            }
            System.out.println(builder.toString().trim());
        }
        builder = new StringBuilder();
        builder.append(String.format("%s=%d", parser.varNames.get(0), varVertices[0]));
        for (int i = 1; i < vars; i++) {
            builder.append(String.format(",%s=%d", parser.varNames.get(i), varVertices[i]));
        }
        System.out.println(builder.toString());
    }

    private boolean setCheck(HashSet<Integer> left, HashSet<Integer> right) {
        int leftInitialSize = left.size();
        HashSet<Integer> savingSet = new HashSet<>();
        savingSet.addAll(left);
        savingSet.addAll(right);
        if (leftInitialSize == savingSet.size()) {
            return true;
        } else return false;
    }

    private boolean allBad(int k, Node tree, HashSet<Integer> c) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(tree);
        Node neededNode = null;
        while (queue.size() > 0) {
            Node currentNode = queue.poll();
            if (currentNode.number == k) {
                neededNode = currentNode;
                break;
            }
            for (int i = 0; i < currentNode.children.size(); i++) {
                queue.add(currentNode.children.get(i));
            }
        }
        if (neededNode == null) {
            System.out.println("WTF"); // really can't happen
            return true;
        }
        queue.clear();
        queue.add(neededNode);
        while (queue.size() > 0) {
            Node currentNode = queue.poll();
            if (!c.contains(currentNode.number)) {
                return true;
            }
            for (int i = 0; i < currentNode.children.size(); i++) {
                queue.add(currentNode.children.get(i));
            }
        }

        return false;
    }

    public List<Node> generateTrees(int vars) {
        List<Node> trees = new ArrayList<>();
        for (int n = 0; n < N; n++) {
            StringBuilder builder = new StringBuilder();
            int[] brackets = new int[2 * n];
            for (int i = 0; i < n; i++) {
                brackets[i] = 1;
                builder.append("(");
            }
            for (int i = n; i < 2 * n; i++) {
                brackets[i] = -1;
                builder.append(")");
            }
            trees.add(new Node(String.format("%s", builder.toString()), vars));
            builder = new StringBuilder();

            long[] catalan = {1, 1, 2, 5, 14, 42, /*132, 429, 1430, 4862, 16796, 58786, 208012*/};
            for (int i = 0; i < catalan[n] - 1; i++) {
                brackets = generateNextBracket(brackets);
                for (int j = 0; j < 2 * n; j++) {
                    if (brackets[j] == 1) {
                        builder.append("(");
                    } else {
                        builder.append(")");
                    }
                }
                trees.add(new Node(String.format("%s", builder.toString()), vars));
                builder = new StringBuilder();
            }
        }
        return trees;
    }
}
