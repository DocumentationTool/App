package com.wonkglorg.doc.core.db.dbs;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * @author Wonkglorg
 */
@SuppressWarnings("unused")
public class SqliteDatabase<T extends DataSource> extends Database<T>{
	private static final Pattern pattern = Pattern.compile(":(\\w+)");
	
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
	 *
	 * @param dataSource the data source
	 */
	public SqliteDatabase(T dataSource) {
		super(SQLITE, dataSource);
	}
	
	@Override
	public void close() {
	}
	
	public Connection getConnection() {
		try{
			return dataSource.getConnection();
		} catch(SQLException e){
			throw new RuntimeException(e);
		}
	}
	
}

