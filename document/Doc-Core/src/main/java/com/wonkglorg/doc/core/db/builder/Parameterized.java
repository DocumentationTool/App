package com.wonkglorg.doc.core.db.builder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public abstract class Parameterized<T extends Parameterized<?, ?, ?, ?>, U, R, S extends Statement>{
	
	protected static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":(\\w+)");
	
	protected final Map<String, U> parameterMapping = new LinkedHashMap<>();
	
	protected final String sql;
	
	protected final boolean transaction;
	
	protected Parameterized(String sql, boolean transaction) {
		this.sql = sql;
		this.transaction = transaction;
	}
	
	protected Parameterized(String sql) {
		this(sql, true);
	}
	
	/**
	 * Add a parameter to the statement
	 *
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 * @return this
	 */
	public T param(String name, U value) {
		parameterMapping.put(name, value);
		return (T) this;
	}
	
	public T param(String name1, U value1, String name2, U value2) {
		parameterMapping.put(name1, value1);
		parameterMapping.put(name2, value2);
		return (T) this;
	}
	
	public T param(String name1, U value1, String name2, U value2, String name3, U value3) {
		parameterMapping.put(name1, value1);
		parameterMapping.put(name2, value2);
		parameterMapping.put(name3, value3);
		return (T) this;
	}
	
	//@formatter:off
	public T param(String name1, U value1, String name2, U value2, String name3, U value3, String name4, U value4) {
		parameterMapping.put(name1, value1);
		parameterMapping.put(name2, value2);
		parameterMapping.put(name3, value3);
		parameterMapping.put(name4, value4);
		return (T) this;
	}
	
	public T param(String name1, U value1, String name2, U value2, String name3, U value3, String name4, U value4, String name5, U value5) {
		parameterMapping.put(name1, value1);
		parameterMapping.put(name2, value2);
		parameterMapping.put(name3, value3);
		parameterMapping.put(name4, value4);
		parameterMapping.put(name5, value5);
		return (T)this;
		
	}
	//@formatter:on
	
	/**
	 * Add multiple parameters to the statement
	 *
	 * @param values the values to add
	 * @return this
	 */
	public T param(Map<String, U> values) {
		parameterMapping.putAll(values);
		return (T) this;
	}
	
	/**
	 * Build the statement
	 *
	 * @param conn the connection to use
	 * @return the prepared statement
	 * @throws SQLException if the statement could not be prepared
	 */
	public S build(Connection conn) throws SQLException {
		List<String> statements = getStatements();
		if(statements.size() == 1){
			return singleStatementBuild(statements.get(0), conn);
		}
		throw new IllegalArgumentException(
				"MultiLine statements are not supported for this class, either the Script class execute multiple statements or separate the statements into separate calls");
	}
	
	/**
	 * Evaluates single statements
	 *
	 * @param statement the statement to evaluate
	 * @param conn the connection to use
	 * @return the statement
	 * @throws SQLException
	 */
	protected S singleStatementBuild(String statement, Connection conn) throws SQLException {
		
		Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
		
		List<String> paramOrder = new ArrayList<>();
		StringBuilder sqlWithPlaceholders = new StringBuilder();
		
		while(matcher.find()){
			String paramName = matcher.group(1);
			paramOrder.add(paramName);
			matcher.appendReplacement(sqlWithPlaceholders, "?");
		}
		matcher.appendTail(sqlWithPlaceholders);
		
		return (S) conn.prepareStatement(sqlWithPlaceholders.toString());
		
	}
	
	public abstract R execute(Connection conn) throws SQLException;
	
	protected List<String> getParameterOrder(String sql) {
		Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
		List<String> paramOrder = new ArrayList<>();
		while(matcher.find()){
			paramOrder.add(matcher.group(1));
		}
		return paramOrder;
	}
	
	protected boolean isMultipleStatements() {
		
		return getStatements().size() != 1;
	}
	
	protected List<String> getStatements() {
		return Stream.of(sql.split(";")).map(s -> {
			String trim = s.trim();
			if(trim.startsWith("\n")){
				trim = trim.substring(1);
			}
			if(trim.endsWith("\n")){
				trim = trim.substring(0, trim.length() - 1);
			}
			return trim;
		}).toList();
		
	}
	
}
