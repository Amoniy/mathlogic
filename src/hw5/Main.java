package hw5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


public class Main {

    public static void main(String[] args) throws Exception {
        TreeGenerator a = new TreeGenerator();
        String toParse = new BufferedReader(new InputStreamReader(System.in)).readLine();
        Parser parser = new Parser();
        Expression expression = parser.parse(toParse);
        List<TreeGenerator.Node> trees = a.generateTrees(parser.getLastVarsCount());
        if (a.check(expression, trees, parser.getLastVarsCount(), parser)) {
            System.out.println("Формула общезначима");
        }
    }
}
