package com.wonkglorg.docapi.db.daos;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UserFunctions {


    @SqlUpdate("""
            INSERT INTO Users(user_id, password_hash, created_by, last_modified_by)  VALUES(:userId,:password,:created_by,:created_by);
            """)
    int addUserFunction(@Bind("userId") String userId, @Bind("password") String password, @Bind("created_by") String createdBy);


}
