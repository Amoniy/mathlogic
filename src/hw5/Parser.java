package hw5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Parser {

    private int index;
    private String input;
    private String variable;
    private char operation;
    public List<String> varNames;

    public Expression parse(String input) {
        varNames = new ArrayList<>();
        index = 0;
        operation = 0;
        variable = "";
        this.input = input.replaceAll("\\s", "");
        return tryImpl();
    }

    private Expression tryImpl() {
        Expression left = tryOr();
        while (true) {
            switch (operation) {
                case '>':
                    left = new Implicate(left, tryImpl());
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression tryOr() {
        Expression left = tryAnd();
        while (true) {
            switch (operation) {
                case '|':
                    left = new Or(left, tryAnd());
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression tryAnd() {
        Expression left = tryBrackets();
        while (true) {
            switch (operation) {
                case '&':
                    left = new And(left, tryBrackets());
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression tryBrackets() {
        singleParse();
        Expression answer;
        switch (operation) {
            case 'v':
                int num;
                if (varNames.contains(variable)) {
                    num = varNames.indexOf(variable);
                } else {
                    varNames.add(variable);
                    num = varNames.size() - 1;
                }
                answer = new Variable(variable, num);
                singleParse();
                break;
            case '!':
                answer = new Negate(tryBrackets());
                break;
            case '(':
                answer = tryImpl();
                singleParse();
                break;
            default: //я сюда никогда не зайду
                answer = new Variable("---", -1);
        }
        return answer;
    }

    private HashSet<Character> nonVars = new HashSet<>(Arrays.asList('-', '|', '&', '!', '(', ')'));

    private String parseVar() {
        StringBuilder var = new StringBuilder();
        while (index < input.length() && !nonVars.contains(input.charAt(index))) {
            var.append(input.charAt(index));
            index++;
        }
        return var.toString();
    }

    private void singleParse() {
        if (index >= input.length()) {
            return;
        }
        char curChar = input.charAt(index);
        switch (curChar) {
            case '-': {
                operation = '>';
                index += 2;
                return;
            }
            case '&': {
                operation = '&';
                index += 1;
                return;
            }
            case '|': {
                operation = '|';
                index += 1;
                return;
            }
            case '!': {
                operation = '!';
                index += 1;
                return;
            }
            case '(': {
                operation = '(';
                index += 1;
                return;
            }
            case ')': {
                operation = ')';
                index += 1;
                return;
            }
        }
        variable = parseVar();
        operation = 'v';
    }

    public int getLastVarsCount() {
        return varNames.size();
    }
}
