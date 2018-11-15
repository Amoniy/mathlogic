package hw6;

public class Operation implements Expression {

    @Override
    public Expression getLeft() {
        return left;
    }

    @Override
    public Expression getRight() {
        return right;
    }

    @Override
    public Expression getSingle() {
        return single;
    }

    public String operation;
    public Expression left;
    public Expression right;
    public Expression single; // negation

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        if(single!=null){
            return String.format("(!%s)", single.toString());
        }
        return String.format("(%s,%s,%s)", getOperation() ,left.toString(), right.toString());
    }
}
