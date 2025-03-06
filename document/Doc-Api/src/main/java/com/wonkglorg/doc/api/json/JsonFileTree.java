package com.wonkglorg.doc.api.json;

import java.util.ArrayList;
import java.util.List;

//todo:jmd build up json tree to return
public class JsonFileTree {
    private final List<JsonRepos<Object>> repo = new ArrayList<>();



    public class JsonFilePath{
        public String path;
        public Object obj;
    }

    
}
