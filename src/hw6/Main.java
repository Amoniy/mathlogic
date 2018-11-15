package hw6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


public class Main {

    public static void main(String[] args) throws Exception {
        TreeGenerator a = new TreeGenerator();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String toParse = bufferedReader.readLine();
        Parser parser = new Parser();
        Expression expression = parser.parse(toParse);
        List<TreeGenerator.Node> trees = a.generateTrees(parser.getLastVarsCount(), parser, bufferedReader);
        if (a.check(expression, trees, parser.getLastVarsCount(), parser)) {
            System.out.println("Не опровергает формулу");
        }
    }
}
