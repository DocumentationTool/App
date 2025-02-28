package com.wonkglorg.doc.core.db.daos.factory;

import org.jdbi.v3.core.argument.Argument;

import java.nio.file.Path;

public class PathArgumentFactory extends AbstractArgumentFactory<Path> {

    public PathArgumentFactory() {
        super(Path.class);
    }

    @Override
    protected Argument toArgument(Object value) {
        return stringArgument(value);
    }

}
