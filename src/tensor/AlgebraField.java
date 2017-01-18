package tensor;

public interface AlgebraField<T extends AlgebraField> {
    T sum(T y);

    T diff(T y);

    T prod(T y);

    T div(T y);

    T sumIdentity();

    T prodIdentity();

    T symmetric();

    T reciprocal();

    T copy();

}
