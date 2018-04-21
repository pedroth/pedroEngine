package stateMachine;

@FunctionalInterface
public interface State<I> {
    State<I> next(I x);
}
