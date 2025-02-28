package com.wonkglorg.doc.core.db.daos;

import com.wonkglorg.doc.core.objects.Resource;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ResourceMappers{
	
	public static class ResourceRowMapper implements RowMapper<Resource>{
		
		public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		private final String path;
		private final String createdAt;
		private final String createdBy;
		private final String lastModifiedAt;
		private final String lastModifiedBy;
		private final String data;
		private final String commitId;
		
		public ResourceRowMapper() {
			path = "resource_path";
			createdAt = "created_at";
			createdBy = "created_by";
			lastModifiedAt = "last_modified_at";
			lastModifiedBy = "last_modified_by";
			data = "data";
			commitId = "commit_id";
		}
		
		public ResourceRowMapper(String path, String createdAt, String createdBy, String lastModifiedAt, String lastModifiedBy, String commitId,String data) {
			super();
			this.path = path;
			this.createdAt = createdAt;
			this.createdBy = createdBy;
			this.lastModifiedAt = lastModifiedAt;
			this.lastModifiedBy = lastModifiedBy;
			this.data = data;
			this.commitId = commitId;
		}
		
		@Override
		public Resource map(ResultSet rs, StatementContext ctx) throws SQLException {
			Path resourcePath = getOrCustom(rs, path, Path::of, null);
			var cA = getOrCustom(rs, createdAt, s -> LocalDateTime.parse(s, formatter), null);
			String cB = getOrDefault(rs, createdBy, null);
			var lA = getOrCustom(rs, lastModifiedAt, s -> LocalDateTime.parse(s, formatter), null);
			String lB = getOrDefault(rs, lastModifiedBy, null);
			String dataReturn = getOrDefault(rs, data, null);
			String commitReturn = getOrDefault(rs,commitId,null);
			return new Resource(resourcePath, cA, cB, lA, lB,commitReturn ,dataReturn);
		}
		
		@SuppressWarnings("unchecked")
		private <T> T getOrDefault(ResultSet rs, String name, T defaultValue) {
			try{
				return (T) rs.getObject(name, defaultValue.getClass());
			} catch(Exception e){
				return defaultValue;
			}
		}
		
		private <T> T getOrCustom(ResultSet rs, String name, Function<String, T> valueMapper, T defaultValue) {
			try{
				return valueMapper.apply(rs.getString(name));
			} catch(Exception e){
				return defaultValue;
			}
		}
		
	}
}
