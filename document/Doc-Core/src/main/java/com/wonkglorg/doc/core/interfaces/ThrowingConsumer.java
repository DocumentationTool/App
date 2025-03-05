package com.wonkglorg.doc.core.interfaces;

import java.util.Objects;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code ThrowingConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 * @since 1.8
 */
@SuppressWarnings("unused")
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
	void accept(T t) throws E;

	default ThrowingConsumer<T, E> andThen(ThrowingConsumer<? super T, E> after) {
		Objects.requireNonNull(after);
		return (T t) -> {
			accept(t);
			after.accept(t);
		};
	}
}
