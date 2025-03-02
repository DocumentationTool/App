package com.wonkglorg.doc.core.db.builder.statement;

import com.wonkglorg.doc.core.db.builder.Parameterized;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents an sql update, which can be parameterized used for single (insert, update, delete) statements
 */
public class Update extends Parameterized<Update, Object, Integer, PreparedStatement>{
	
	public Update(String sql, boolean transaction) {
		super(sql, transaction);
	}
	
	public Update(String sql) {
		super(sql);
	}
	
	@Override
	public Integer execute(Connection conn) throws SQLException {
		if(transaction){
			conn.setAutoCommit(false);
		}
		try(PreparedStatement stmt = build(conn)){
			return stmt.executeUpdate();
		} finally{
			if(transaction){
				conn.commit();
				conn.setAutoCommit(true);
			}
		}
	}
}
