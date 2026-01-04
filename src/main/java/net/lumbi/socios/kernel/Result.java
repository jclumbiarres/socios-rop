package net.lumbi.socios.kernel;

import java.util.function.Function;

/**
 * A functional result type that represents either a successful value or an
 * error.
 * This interface is isomorphic to Rust's Result&lt;T, E&gt; type and enables
 * Railway-Oriented Programming (ROP) patterns in Java.
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error
 */
public sealed interface Result<T, E> {

    /**
     * Represents a successful result containing a value.
     * Isomorphic to Rust's Ok(T).
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error (not used in Success)
     */
    record Success<T, E>(T value) implements Result<T, E> {
    }

    /**
     * Represents a failed result containing an error.
     * Isomorphic to Rust's Err(E).
     *
     * @param <T> the type of the success value (not used in Failure)
     * @param <E> the type of the error
     */
    record Failure<T, E>(E error) implements Result<T, E> {
    }

    /**
     * Creates a successful result containing the given value.
     * 
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Result<Integer, String> result = Result.success(42);
     * // result is Success(42)
     * }</pre>
     *
     * @param <T>   the type of the success value
     * @param <E>   the type of the error
     * @param value the success value to wrap
     * @return a Success result containing the value
     */
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed result containing the given error.
     * 
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Result<Integer, String> result = Result.failure("Not found");
     * // result is Failure("Not found")
     * }</pre>
     *
     * @param <T>   the type of the success value
     * @param <E>   the type of the error
     * @param error the error to wrap
     * @return a Failure result containing the error
     */
    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    /**
     * Transforms the success value using the provided mapper function.
     * If this is a Success, applies the mapper to the value and returns a new
     * Success.
     * If this is a Failure, returns the Failure unchanged.
     * 
     * <p>
     * This operation is fundamental to Railway-Oriented Programming, allowing
     * transformation of values while preserving error propagation.
     * 
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Result<Integer, String> result = Result.success(5);
     * Result<Integer, String> doubled = result.map(x -> x * 2);
     * // doubled is Success(10)
     * 
     * Result<Integer, String> error = Result.failure("Error");
     * Result<Integer, String> unchanged = error.map(x -> x * 2);
     * // unchanged is still Failure("Error")
     * }</pre>
     *
     * @param <R>    the type of the new success value
     * @param mapper the function to transform the success value
     * @return a Result with the transformed value or the original Failure
     */
    @SuppressWarnings("unchecked")
    default <R> Result<R, E> map(Function<T, R> mapper) {
        if (this instanceof Success<T, E> s) {
            return success(mapper.apply(s.value()));
        }
        return (Result<R, E>) this; // Safe cast due to sealed interface
    }

    /**
     * Chains computations that may fail, applying the mapper only if this is a
     * Success.
     * This is the monadic bind operation (also known as flatMap or andThen).
     * 
     * <p>
     * If this is a Success, applies the mapper to extract the value and returns
     * the resulting Result. If this is a Failure, returns the Failure unchanged.
     * 
     * <p>
     * This is the key operation for Railway-Oriented Programming, enabling
     * sequential composition of operations that may fail.
     * 
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Result<String, String> parseAndValidate(String input) {
     *     return parseInteger(input)
     *             .flatMap(this::validatePositive)
     *             .flatMap(this::validateRange);
     * }
     * 
     * Result<Integer, String> parseInteger(String s) {
     *     try {
     *         return Result.success(Integer.parseInt(s));
     *     } catch (NumberFormatException e) {
     *         return Result.failure("Invalid number");
     *     }
     * }
     * 
     * Result<Integer, String> validatePositive(Integer n) {
     *     return n > 0
     *             ? Result.success(n)
     *             : Result.failure("Must be positive");
     * }
     * 
     * // Usage:
     * Result<String, String> result = parseAndValidate("42");
     * // result is Success(42) if all validations pass
     * // or Failure with first error message if any step fails
     * }</pre>
     *
     * @param <R>    the type of the new success value
     * @param mapper the function that produces a new Result
     * @return the Result produced by the mapper or the original Failure
     */
    @SuppressWarnings("unchecked")
    default <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
        if (this instanceof Success<T, E> s) {
            return mapper.apply(s.value());
        }
        return (Result<R, E>) this;
    }

    /**
     * Extracts a value from the Result by applying the appropriate function.
     * This is a catamorphism (fold) that collapses the Result into a single value.
     * 
     * <p>
     * Applies onSuccess to the value if this is a Success, or onFailure to
     * the error if this is a Failure.
     * 
     * <p>
     * Example:
     * 
     * <pre>{@code
     * Result<Integer, String> success = Result.success(42);
     * String message = success.fold(
     *         value -> "Got value: " + value,
     *         error -> "Error: " + error);
     * // message is "Got value: 42"
     * 
     * Result<Integer, String> failure = Result.failure("Not found");
     * String errorMsg = failure.fold(
     *         value -> "Got value: " + value,
     *         error -> "Error: " + error);
     * // errorMsg is "Error: Not found"
     * 
     * // Converting to HTTP status code:
     * int statusCode = result.fold(
     *         value -> 200,
     *         error -> 404);
     * }</pre>
     *
     * @param <R>       the type of the result value
     * @param onSuccess function to apply if this is a Success
     * @param onFailure function to apply if this is a Failure
     * @return the result of applying the appropriate function
     */
    default <R> R fold(Function<T, R> onSuccess, Function<E, R> onFailure) {
        return switch (this) {
            case Success<T, E> s -> onSuccess.apply(s.value());
            case Failure<T, E> f -> onFailure.apply(f.error());
        };
    }
}