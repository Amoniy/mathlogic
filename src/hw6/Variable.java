package hw6;

public class Variable implements Expression {

    @Override
    public Expression getSingle() {
        return null;
    }

    @Override
    public Expression getLeft() {
        return null;
    }

    @Override
    public Expression getRight() {
        return null;
    }

    int number;
    String name;

    public Variable(String var, int num) {
        this.name = var;
        this.number = num;
    }

    @Override
    public String toString() {
        return name;
    }

}
