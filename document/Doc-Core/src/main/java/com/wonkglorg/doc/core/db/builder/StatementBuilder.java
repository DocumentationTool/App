package com.wonkglorg.doc.core.db.builder;


import com.wonkglorg.doc.core.db.builder.statement.Batch;
import com.wonkglorg.doc.core.db.builder.statement.Query;
import com.wonkglorg.doc.core.db.builder.statement.Script;
import com.wonkglorg.doc.core.db.builder.statement.Update;
import org.intellij.lang.annotations.Language;

import java.util.Collection;

public class StatementBuilder{
	
	private StatementBuilder() {
		//utility class
	}
	
	public static Query query(@Language("SQL") String sql) {
		return new Query(sql);
	}
	
	public static <T> Batch<T> batch(@Language("SQL") String sql, Collection<T> values) {
		return new Batch<>(sql, values);
	}
	
	public static Update update(@Language("SQL") String sql) {
		return new Update(sql);
	}
	
	public static Script script(@Language("SQL") String sql) {
		return new Script(sql);
	}
}
