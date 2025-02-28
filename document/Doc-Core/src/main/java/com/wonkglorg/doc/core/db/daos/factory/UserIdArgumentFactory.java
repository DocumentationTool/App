package com.wonkglorg.doc.core.db.daos.factory;

import com.wonkglorg.doc.core.objects.UserId;
import org.jdbi.v3.core.argument.Argument;

public class UserIdArgumentFactory extends AbstractArgumentFactory<UserId> {

    public UserIdArgumentFactory() {
        super(UserId.class);
    }

    @Override
    protected Argument toArgument(Object value) {
        return stringArgument(value);
    }
}
