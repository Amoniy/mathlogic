package hw5;

public class Negate extends Operation {

    public Negate(Expression single) {
        this.single = single;
    }

    @Override
    public String getOperation() {
        return "!";
    }

}
