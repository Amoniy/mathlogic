package hw6;

public class Implicate extends Operation {

    public Implicate(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getOperation() {
        return "->";
    }

}
