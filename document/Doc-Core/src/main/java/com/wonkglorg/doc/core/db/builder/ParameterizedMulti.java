package com.wonkglorg.doc.core.db.builder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public abstract class ParameterizedMulti<T extends Parameterized<?, ?, ?, ?>, U, R, S extends Statement> extends Parameterized<T, U, R, S>{
	
	protected ParameterizedMulti(String sql, boolean transaction) {
		super(sql, transaction);
	}
	
	protected ParameterizedMulti(String sql) {
		super(sql);
	}
	
	@Override
	public S build(Connection conn) throws SQLException {
		List<String> statements = getStatements();
		if(statements.size() == 1){
			return singleStatementBuild(statements.get(0), conn);
		}
		if(statements.size() > 1){
			return multiStatementBuild(statements, conn);
		}
		throw new IllegalArgumentException("No statements to execute");
	}
	
	/**
	 * Evaluates multiple statements
	 *
	 * @param statements the statements to evaluate
	 * @param conn the connection to use
	 * @return the statements
	 * @throws SQLException
	 */
	protected abstract S multiStatementBuild(List<String> statements, Connection conn) throws SQLException;
	
}
