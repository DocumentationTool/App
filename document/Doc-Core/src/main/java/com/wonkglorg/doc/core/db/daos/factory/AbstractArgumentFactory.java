package com.wonkglorg.doc.core.db.daos.factory;

import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.argument.ArgumentFactory.Preparable;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.lang.reflect.Type;
import java.sql.Types;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractArgumentFactory<T> implements ArgumentFactory, Preparable {

    private final Class<T> expectedType;

    protected AbstractArgumentFactory(Class<T> type) {
        this.expectedType = type;
    }

    @Override
    public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
        if (type != expectedType) return Optional.empty();
        return Optional.of(this::toArgument);
    }

    protected abstract Argument toArgument(Object value);

    protected static Argument stringArgument(Object value) {
        if (value == null) {
            return (position, statement, ctx) -> statement.setNull(position, Types.NVARCHAR);
        }
        return (position, statement, ctx) -> statement.setString(position, value.toString());
    }
}
