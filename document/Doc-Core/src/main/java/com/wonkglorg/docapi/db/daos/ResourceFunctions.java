package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.db.objects.Resource;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.*;

import java.nio.file.Path;
import java.util.List;

public interface ResourceFunctions {

    /**
     * Deletes a specific resource and all its related data in ResourceData, Tags and Permissions
     *
     * @param resourcePath the path to the resource
     */
    @SqlUpdate("DELETE FROM Resources WHERE resourcePath = :resourcePath")
    int delete(@Bind("resourcePath") Path resourcePath);

    /**
     * Deletes the indexed data for this resource
     *
     * @param resourcePath the path to the resource
     */
    @SqlUpdate("DELETE FROM FileData WHERE resourcePath = :resourcePath")
    void deleteData(@Bind("resourcePath") Path resourcePath);

    @SqlQuery("Select * From Resources")
    @UseRowMapper(ResourceMappers.ResourceRowMapper.class)
    List<Resource> findAll();

    @SqlQuery("SELECT * FROM Resources WHERE resourcePath = :resourcePath")
    @UseRowMapper(ResourceMappers.ResourceRowMapper.class)
    Resource findByPath(@Bind("resourcePath") Path resourcePath);


    @SqlUpdate(
            "INSERT INTO Resources(resourcePath,created_at,created_by,last_modified_at,last_modified_by)"
                    + " VALUES(:resourcePath,:createdAt,:createdBy,:lastModifiedAt,:lastModifiedBy);")
    int insert(@BindMethods Resource resource); //use bind methods instead of bindBean for records as records are not beans

    @SqlScript("""
            INSERT INTO Resources(resourcePath,created_at,created_by,last_modified_at,last_modified_by) VALUES(:resourcePath,:createdAt,:createdBy,:lastModifiedAt,:lastModifiedBy);
            INSERT INTO FileData(resourcePath,data) VALUES(:resourcePath,:data);
            """)
    void insert(@BindBean Resource resource, @Bind("data") String data);

    @SqlUpdate("UPDATE Resources SET resourcePath = :newPath WHERE resourcePath = :oldPath")
    int updatePath(@Bind("oldPath") Path oldPath, @Bind("newPath") Path newPath);


    @SqlBatch("INSERT INTO FileData(resourcePath, data) VALUES(:resourePath,:data)")
    void insertBatch(@BindBean List<Resource> resources);

    @SqlUpdate("""
            UPDATE FileData
            SET data = :data
            WHERE resourcePath = :resourcePath;
            """)
    int updateResource(@Bind("resourcePath") Path resourcePath, @Bind("data") String data);


}

