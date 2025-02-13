package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.db.objects.Resource;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public interface ResourceDAO {

	@SqlUpdate("DELETE FROM Resources WHERE resourcePath = :resourcePath")
	void delete(@Bind("resourcePath") String resourcePath);

	@SqlQuery("Select * From Resources")
	List<Resource> findAll();

	@SqlQuery("SELECT * FROM Resources WHERE resourcePath = :resourcePath")
	Resource findByPath(@Bind("resourcePath") String resourcePath);

	@SqlUpdate(
			"INSERT INTO Resources(resourcePath,created_at,created_by,last_modified_at,last_modified_by)"
					+ " VALUES(:resourcePath,:createdAt,:createdBy,:lastModifiedAt,:lastModifiedBy)")
	void insert(@BindBean Resource resource);

	@SqlUpdate("INSERT INTO FileData(resourcePath,data) VALUES(:path,:data)")
	void insertData(@Bind("path")Path path,@Bind("data") String data);

}

