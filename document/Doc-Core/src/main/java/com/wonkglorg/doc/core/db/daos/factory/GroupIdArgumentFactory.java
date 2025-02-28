package com.wonkglorg.doc.core.db.daos.factory;

import com.wonkglorg.doc.core.objects.GroupId;
import org.jdbi.v3.core.argument.Argument;

public class GroupIdArgumentFactory extends AbstractArgumentFactory<GroupId> {
    public GroupIdArgumentFactory() {
        super(GroupId.class);
    }

    @Override
    protected Argument toArgument(Object value) {
        return stringArgument(value);
    }
}
