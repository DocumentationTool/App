package com.wonkglorg.docapi.db;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataDB extends SqliteDatabase {

	private final List<String> createTables = List.of(


	);



	public DataDB(Path sourcePath, Path destinationPath) {
		super(sourcePath, destinationPath);
	}

	public DataDB(Path openInPath) {
		super(openInPath);
	}


	private void initialize(){

	}



}
