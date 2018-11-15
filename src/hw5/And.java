package hw5;

public class And extends Operation {

    public And(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getOperation() {
        return "&";
    }

}
