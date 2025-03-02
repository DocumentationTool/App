package com.wonkglorg.doc.core.db.builder.statement;

import com.wonkglorg.doc.core.db.builder.Parameterized;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Batch<T> extends Parameterized<Batch<?>, Function<T, Object>, Void, PreparedStatement>{
	private final Collection<T> batch;
	
	public Batch(String sql, Collection<T> batch) {
		super(sql);
		this.batch = batch;
	}
	
	@Override
	public Void execute(Connection conn) throws SQLException {
		if(batch.isEmpty()){
			return null;
		}
		
		try(PreparedStatement stmt = build(conn)){
			List<String> paramOrder = getParameterOrder(sql);
			for(T item : batch){
				for(int i = 0; i < paramOrder.size(); i++){
					String paramName = paramOrder.get(i);
					Function<T, Object> extractor = parameterMapping.get(paramName);
					if(extractor == null){
						throw new IllegalArgumentException("Missing extractor for parameter: " + paramName);
					}
					stmt.setObject(i + 1, extractor.apply(item));
				}
				stmt.addBatch();
			}
			stmt.executeBatch();
		}
		return null;
	}
	
}
