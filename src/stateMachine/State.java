package stateMachine;

public interface State<I> {
    State<I> next(I x);
}
