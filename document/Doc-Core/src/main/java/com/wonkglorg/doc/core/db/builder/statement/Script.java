package com.wonkglorg.doc.core.db.builder.statement;


import com.wonkglorg.doc.core.db.builder.ParameterizedMulti;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Represents an sql script, which can be parameterized, used for multiple statements in 1 request, if a resultset is needed use {@link Query} instead, define parameters in sql queries as :paramName and add the parameter with {@link #param(String, Object)}
 *
 *
 * for String parameters surround them with '' in the sql query to make sure they are escaped correctly, this script does not validate or prevent sql injections or mismatched data, and relies on {@link Statement} directly
 */
public class Script extends ParameterizedMulti<Script, Object, Void, Statement>{
	
	public Script(String sql, boolean transaction) {
		super(sql, transaction);
	}
	
	public Script(String sql) {
		super(sql);
	}
	
	@Override
	protected Statement singleStatementBuild(String statement, Connection conn) throws SQLException {
		Statement sqlStatement = conn.createStatement();
		sqlStatement.addBatch(replaceSqlParameters(statement));
		return sqlStatement;
	}
	
	@Override
	protected Statement multiStatementBuild(List<String> statements, Connection conn) throws SQLException {
		
		Statement sqlStatement = conn.createStatement();
		for(String statement : statements){
			sqlStatement.addBatch(replaceSqlParameters(statement));
		}
		return sqlStatement;
	}
	
	protected String replaceSqlParameters(String sql) {
		Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
		StringBuilder sqlWithPlaceholders = new StringBuilder();
		
		while(matcher.find()){
			String paramName = matcher.group(1);
			
			if(!parameterMapping.containsKey(paramName)){
				throw new IllegalArgumentException("Missing parameter: " + paramName);
			}
			Object object = parameterMapping.get(paramName);
			matcher.appendReplacement(sqlWithPlaceholders, toDatabaseValue(object.toString()));
		}
		matcher.appendTail(sqlWithPlaceholders);
		return sqlWithPlaceholders.toString();
	}
	
	@Override
	public Void execute(Connection conn) throws SQLException {
		if(transaction){
			conn.setAutoCommit(false);
		}
		try(Statement stmt = build(conn)){
			stmt.executeBatch();
		} finally{
			if(transaction){
				conn.commit();
				conn.setAutoCommit(true);
			}
		}
		return null;
	}
	
	/**
	 * Convert the object to a database value
	 * @param object the object to convert
	 * @return the database value
	 */
	public String toDatabaseValue(Object object) {
		if(!(object instanceof Number)){
			return "'" + object + "'";
		}
		return object.toString();
	}
	
}
