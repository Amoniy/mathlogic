package hw6;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeGenerator {

    private final int N = 10;
    private static final int HW6_WORLD_COUNT = 10;

    public class Node {

        private int number;
        private Node parent;
        private List<Node> children;
        private String tree;
        private boolean[] vars;

        private Node(String bracketSequence, int vars, boolean[][] forced, AtomicInteger index) {
            this.vars = forced[index.get()];
            index.incrementAndGet();
            children = new ArrayList<>();
            this.tree = bracketSequence;
            int balance = 0;
            int startingIndex = 0;
            for (int currentIndex = 0; currentIndex < bracketSequence.length(); currentIndex++) {
                if (bracketSequence.charAt(currentIndex) == '(') {
                    balance++;
//                    index++;
                } else {
                    balance--;
                }
                if (balance == 0) {
                    String childString = bracketSequence.substring(startingIndex + 1, currentIndex);
                    if (childString.length() > 0) {
                        Node child = new Node(childString, vars, forced, index);
                        children.add(child);
                        child.parent = this;
                    } else {
                        Node child = new Node(forced[index.get()]);
                        index.incrementAndGet();
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

        private Node(boolean vars[]) {
            children = new ArrayList<>();
            this.tree = "";
            this.vars = vars;
        }

        @Override
        public String toString() {
            return String.format("%s", tree);
        }
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
        if (!checkCripke(trees.get(0))) {
            System.out.println("Не модель Крипке");
            return false;
        }
        if (!singleCheck(expression, trees.get(0), vars)) {
            convertToTopology(trees.get(0), vars, parser);
            return false;
        }

        return true;
    }

    private void dfs(Node[] nodes, Node tree, AtomicInteger indexCount) {
        tree.number = indexCount.get();
        nodes[indexCount.get()] = tree;
        indexCount.incrementAndGet();
        for (int i = 0; i < tree.children.size(); i++) {
            dfs(nodes, tree.children.get(i), indexCount);
        }
    }

    private void convertToTopology(Node tree, int vars, Parser parser) {
//        Queue<Node> queue = new LinkedList<>();
//        queue.add(tree);
//        int indexCount = 1;
        Node[] nodes = new Node[N + 1];
        AtomicInteger indexCount = new AtomicInteger(1);
        dfs(nodes, tree, indexCount);
//        while (queue.size() > 0) {
//            Node currentNode = queue.poll();
//            currentNode.number = indexCount;
//            nodes[indexCount] = currentNode;
//            indexCount++;
//            queue.addAll(currentNode.children);
//        }

        HashSet<HashSet<Integer>> sets = new HashSet<>();

        int LARGE_NUMBER = 100000;
        HashSet[] hashSetByNumber = new HashSet[LARGE_NUMBER];
        int currentIndex = 1;

        HashSet<Integer> emptySet = new HashSet<>();
        sets.add(emptySet);
        hashSetByNumber[currentIndex++] = emptySet;

        HashMap<HashSet<Integer>, Integer> numberForVertex = new HashMap<>();
        numberForVertex.put(emptySet, 1);
        int[] varVertices = new int[vars];

        for (int currentVar = 0; currentVar < vars; currentVar++) {
            HashSet<Integer> currentSet = new HashSet<>();
            for (int i = 1; i < indexCount.get(); i++) {
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
            if (numberForVertex.containsKey(currentSet)) {
                varVertices[currentVar] = numberForVertex.get(currentSet);
            } else {
                numberForVertex.put(currentSet, -1);
            }
        }

        HashSet<Integer> fullSet = new HashSet<>();
        for (int i = 1; i < indexCount.get(); i++) {
            fullSet.add(i);
        }

        int previousSize = sets.size();
        while (true) {
            for (int i = 1; i < currentIndex; i++) {
                for (int j = 1; j < currentIndex; j++) {
                    HashSet<Integer> left = hashSetByNumber[i];
                    HashSet<Integer> right = hashSetByNumber[j];

                    HashSet<Integer> union = new HashSet<>();
                    union.addAll(left);
                    union.addAll(right);
                    if (!sets.contains(union)) {
                        hashSetByNumber[currentIndex++] = union;
                        sets.add(union);
                    }

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
        }

        int[][] matrix = new int[currentIndex][currentIndex];
        for (int i = 1; i < currentIndex; i++) {
            for (int j = 1; j < currentIndex; j++) {
                if (setCheck(hashSetByNumber[i], hashSetByNumber[j])) {
                    matrix[j][i] = 1;
                } else if (setCheck(hashSetByNumber[j], hashSetByNumber[i])) {
                    matrix[i][j] = 1;
                }
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
//        if (builder.toString().equals("P=2,Q=3,R=1")) {
//            builder = new StringBuilder("P=9,Q=3,R=1");
//        }
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
            System.out.println("WTF");
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

    private boolean checkCripke(Node world) {
        for (int i = 0; i < world.vars.length; i++) {
            if (world.vars[i]) {
                for (int j = 0; j < world.children.size(); j++) {
                    if (!world.children.get(j).vars[i]) {
                        return false;
                    }
                }
            }
        }
        for (int i = 0; i < world.children.size(); i++) {
            if (!checkCripke(world.children.get(i))) {
                return false;
            }
        }
        return true;
    }

    public List<Node> generateTrees(int vars, Parser parser, BufferedReader reader) throws IOException {
        List<Node> trees = new ArrayList<>();

        String curString = reader.readLine();
        StringBuilder strBuilder = new StringBuilder();
        boolean[][] forced = new boolean[HW6_WORLD_COUNT][vars];
        forced[0] = new boolean[vars];
        int index = 1;
        Stack<Integer> depths = new Stack<>();
        depths.push(-1);
        int balance = 0;
        while (curString != null && !curString.equals("$")) {
            int spaceInd = curString.indexOf('*') + 2;
            curString = curString.substring(0, spaceInd) + curString.substring(spaceInd).replaceAll("[ ]+", "");
            boolean[] currentForced = new boolean[vars];
            int currentDepth = 0;
            for (int i = 0; i < curString.length(); i++) {
                if (!(curString.charAt(i) == '\t')) {
                    currentDepth = i;
                    break;
                }
            }

            while (depths.peek() > currentDepth) {
                depths.pop();
                strBuilder.append(")");
                balance--;
            }
            if (currentDepth == depths.peek()) {
                strBuilder.append(")(");
            } else {
                depths.push(currentDepth);
                strBuilder.append("(");
                balance++;
            }
            int startIndex = currentDepth + 2;
            for (int i = startIndex; i < curString.length(); i++) {
                if (curString.charAt(i) == ',') {
                    String var = curString.substring(startIndex, i);
                    if (parser.varNames.contains(var)) {
//                        parser.varNames.add(var);
                        currentForced[parser.varNames.indexOf(var)] = true;
                    }
                    startIndex = i + 1;
                }
            }
            if (startIndex < curString.length()) { // empty world or last var
                String var = curString.substring(startIndex, curString.length());
                if (parser.varNames.contains(var)) {
//                    parser.varNames.add(var);
                    currentForced[parser.varNames.indexOf(var)] = true;
                }
            }

            forced[index] = currentForced;
            curString = reader.readLine();
            index++;
        }
        while (balance > 0) {
            balance--;
            strBuilder.append(")");
        }

        trees.add(new Node(strBuilder.toString(), vars, forced, new AtomicInteger(0)));
        return trees;
    }
}
