package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.db.objects.Resource;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ResourceFunctions {

	/**
	 * Deletes a specific resource and all its related data in ResourceData, Tags and Permissions
	 * @param resourcePath the path to the resource
	 */
	@SqlUpdate("DELETE FROM Resources WHERE resourcePath = :resourcePath")
	void delete(@Bind("resourcePath") String resourcePath);

	/**
	 * Deletes the indexed data for this resource
	 * @param resourcePath the path to the resource
	 */
	@SqlUpdate("DELETE FROM ResourceData WHERE resourcePath = :resourcePath")
	void deleteData(@Bind("resourcePath") String resourcePath);

	@SqlQuery("Select * From Resources")
	List<Resource> findAll();

	@SqlQuery("SELECT * FROM Resources WHERE resourcePath = :resourcePath")
	Resource findByPath(@Bind("resourcePath") String resourcePath);


	@SqlUpdate(
			"INSERT INTO Resources(resourcePath,created_at,created_by,last_modified_at,last_modified_by)"
			+ " VALUES(:resourcePath,:createdAt,:createdBy,:lastModifiedAt,:lastModifiedBy);")
	int insert(@BindBean Resource resource);

	@SqlUpdate("""
			BEGIN TRANSACTION;
			INSERT INTO Resources(resourcePath,created_at,created_by,last_modified_at,last_modified_by) VALUES(:resourcePath,:createdAt,:createdBy,:lastModifiedAt,:lastModifiedBy);
			INSERT INTO FileData(resourcePath,data) VALUES(:resourcePath,:data);
			COMMIT;
			""")
	int insert(@BindBean Resource resource, @Bind("data") String data);
}

