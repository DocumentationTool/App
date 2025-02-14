package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.db.objects.Resource;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResourceMappers {

	public static class ResourceRowMapper implements RowMapper<Resource> {

		public static final DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		private final String path;
		private final String createdAt;
		private final String createdBy;
		private final String lastModifiedAt;
		private final String lastModifiedBy;

		public ResourceRowMapper() {
			path = "resourcePath";
			createdAt = "created_at";
			createdBy = "created_by";
			lastModifiedAt = "last_modified_at";
			lastModifiedBy = "last_modified_by";
		}

		public ResourceRowMapper(String path, String createdAt, String createdBy,
				String lastModifiedAt,
				String lastModifiedBy) {
			super();
			this.path = path;
			this.createdAt = createdAt;
			this.createdBy = createdBy;
			this.lastModifiedAt = lastModifiedAt;
			this.lastModifiedBy = lastModifiedBy;
		}

		@Override
		public Resource map(ResultSet rs, StatementContext ctx) throws SQLException {
			Path resourcePath = Path.of(rs.getString(path));
			var cA = LocalDateTime.parse(rs.getString(createdAt), formatter);
			String cB = rs.getString(createdBy);
			var lA = LocalDateTime.parse(rs.getString(lastModifiedAt), formatter);
			String lB = rs.getString(lastModifiedBy);

			return new Resource(resourcePath, cA, cB, lA, lB);
		}
	}
}
