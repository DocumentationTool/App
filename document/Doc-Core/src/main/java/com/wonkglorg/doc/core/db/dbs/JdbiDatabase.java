package com.wonkglorg.doc.core.db.dbs;

import com.wonkglorg.doc.core.interfaces.ThrowingConsumer;
import com.wonkglorg.doc.core.interfaces.ThrowingFunction;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;
import java.util.function.Function;

/**
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public class JdbiDatabase<T extends DataSource> extends Database<T>{
	protected Jdbi jdbi;
	
	/**
	 * * Creates a Sqlite database at the specified copyToPath.
	 * * The sourcePath indicates where in the project the database file can be found, it will
	 * then be
	 * copied to the destinationPath destination.
	 * * If there is no database file it will be created at the destinationPath location.
	 * <br>
	 * !!IMPORTANT!!
	 * <br>Use <br>
	 * <pre>
	 *     {@code
	 * <plugin>
	 * 	<groupId>org.apache.maven.plugins</groupId>
	 * 	<artifactId>maven-resources-plugin</artifactId>
	 * 	<version>3.3.1</version>
	 * 	<configuration>
	 * 		<nonFilteredFileExtensions>
	 * 			<nonFilteredFileExtension>db</nonFilteredFileExtension>
	 * 		</nonFilteredFileExtensions>
	 * 	</configuration>
	 * </plugin>
	 * }
	 * </pre>
	 * otherwise sqlite database files will be filtered and become corrupted.
	 */
	public JdbiDatabase(T dataSource) {
		super(SQLITE, dataSource);
		connect();
		jdbi().installPlugin(new SqlObjectPlugin());
	}

	/**
	 * Attaches a sql interface to this jdbi connection (all resources will be automatically closed
	 * after usage)
	 *
	 * @param clazz    the class should follow {@link Handle#attach(Class)} specified requirements
	 * @param consumer the consumer of the prepared class
	 * @return the expected value
	 */
	public <V, R, E extends Throwable> R attach(Class<V> clazz, ThrowingFunction<V, R, E> consumer) throws E {
		try (Handle handle = jdbi.open()) {
			return consumer.apply(handle.attach(clazz));
		}
	}
	
	@Override
	public void close() {
		//nothing needs to be closed here
	}
	
	public void connect() {
		if(jdbi != null){
			return;
		}
		
		jdbi = Jdbi.create(dataSource);
	}
	
	public Jdbi jdbi() {
		return jdbi;
	}
	
	/**
	 * Represents a query to the database that returns some result.
	 *
	 * @param sql The sql query to execute
	 * @param function The function to apply to the query
	 * @param <R>
	 * @return
	 */
	public <R> R query(String sql, Function<Query, R> function) throws RuntimeException {
		connect();
		try(Handle handle = jdbi.open(); Query query = handle.createQuery(sql)){
			return function.apply(query);
		} catch(Exception e){
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * Represents a query to the database that returns some result.
	 *
	 * @param sql The sql query to execute
	 * @param function The function to apply to the query
	 * @param onError function to call when an error occured
	 * @param <R>
	 * @return
	 */
	public <R> R query(String sql, Function<Query, R> function, Function<Exception, R> onError) {
		try{
			return query(sql, function);
		} catch(Exception e){
			return onError.apply(e);
		}
	}
	
	/**
	 * Attaches a sql interface to this jdbi connection with no expected return (all resources
	 * will be
	 * automatically closed after usage)
	 *
	 * @param clazz the class should follow {@link Handle#attach(Class)} specified requirements
	 * @param consumer the consumer of the prepared class
	 */
	public <V,E extends Throwable> void voidAttach(Class<V> clazz, ThrowingConsumer<V,E> consumer) throws RuntimeException, E {
		try(Handle handle = jdbi.open()){
			consumer.accept(handle.attach(clazz));
		}
	}

	/**
	 * Represents a query to the database that does not return a result.
	 *
	 * @param sql      The sql query to execute
	 * @param function The function to apply to the query
	 */
	public <E extends Throwable> void voidQuery(String sql, ThrowingConsumer<Query, E> function) throws E {
		connect();
		try (Handle handle = jdbi.open(); Query query = handle.createQuery(sql)) {
			function.accept(query);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	public void registerArgument(ArgumentFactory argumentFactory){
		jdbi().registerArgument(argumentFactory);
	}

	public void registerRowMapper(RowMapper<?> rowMapper){
		jdbi().registerRowMapper(rowMapper);
	}



}

