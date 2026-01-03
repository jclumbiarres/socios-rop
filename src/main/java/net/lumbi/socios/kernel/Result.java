package net.lumbi.socios.kernel;

import java.util.function.Function;

public sealed interface Result<T, E> {
    // Isomorphic a Rust: Ok(T)
    record Success<T, E>(T value) implements Result<T, E> {
    }

    // Isomorphic a Rust: Err(E)
    record Failure<T, E>(E error) implements Result<T, E> {
    }

    // Constructores estáticos para syntactic sugar
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    // ROP: Map (transforma el valor si es Success, ignora si es Failure)
    @SuppressWarnings("unchecked")
    default <R> Result<R, E> map(Function<T, R> mapper) {
        if (this instanceof Success<T, E> s) {
            return success(mapper.apply(s.value()));
        }
        return (Result<R, E>) this; // Cast seguro por ser sealed
    }

    // ROP: FlatMap (bind) - La clave del Railway
    @SuppressWarnings("unchecked")
    default <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
        if (this instanceof Success<T, E> s) {
            return mapper.apply(s.value());
        }
        return (Result<R, E>) this;
    }

    // Un "fold" o "match" para sacar el valor final
    default <R> R fold(Function<T, R> onSuccess, Function<E, R> onFailure) {
        return switch (this) {
            case Success<T, E> s -> onSuccess.apply(s.value());
            case Failure<T, E> f -> onFailure.apply(f.error());
        };
    }
}
