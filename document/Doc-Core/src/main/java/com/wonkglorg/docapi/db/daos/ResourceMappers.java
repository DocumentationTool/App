package com.wonkglorg.docapi.db.daos;

import com.wonkglorg.docapi.db.objects.Resource;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ResourceMappers {

    public static class ResourceRowMapper implements RowMapper<Resource> {

        public static final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private final String path;
        private final String createdAt;
        private final String createdBy;
        private final String lastModifiedAt;
        private final String lastModifiedBy;
        private final String data;

        public ResourceRowMapper() {
            path = "resourcePath";
            createdAt = "created_at";
            createdBy = "created_by";
            lastModifiedAt = "last_modified_at";
            lastModifiedBy = "last_modified_by";
            data = "data";
        }

        public ResourceRowMapper(String path, String createdAt, String createdBy,
                                 String lastModifiedAt,
                                 String lastModifiedBy, String data) {
            super();
            this.path = path;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
            this.lastModifiedAt = lastModifiedAt;
            this.lastModifiedBy = lastModifiedBy;
            this.data = data;
        }

        @Override
        public Resource map(ResultSet rs, StatementContext ctx) throws SQLException {
            Path resourcePath = getOrCustom(rs,path,Path::of,null);
            var cA = LocalDateTime.parse(rs.getString(createdAt), formatter);
            String cB = rs.getString(createdBy);
            var lA = LocalDateTime.parse(rs.getString(lastModifiedAt), formatter);
            String lB = rs.getString(lastModifiedBy);
            String dataReturn = null;
            try {
                dataReturn = rs.getString(data);
            } catch
            return new Resource(resourcePath, cA, cB, lA, lB, dataReturn);
        }

        @SuppressWarnings("unchecked")
        private <T> T getOrDefault(ResultSet rs, String name, T defaultValue) {
            try {
                return (T) rs.getObject(name, defaultValue.getClass());
            } catch (SQLException e) {
                return defaultValue;
            }
        }

        private <T> T getOrCustom(ResultSet rs, String name, Function<String, T> valueMapper, T defaultValue) {
            try {
                return valueMapper.apply(rs.getString(name));
            } catch (SQLException e) {
                return defaultValue;
            }
        }

    }
}
