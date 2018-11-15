package hw6;

public class Or extends Operation {

    public Or(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getOperation() {
        return "|";
    }

}
