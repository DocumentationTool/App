package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.objects.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTags {
    public Map<String, String> tags = new HashMap<>();

    public JsonTags(List<Tag> tags) {
        for (Tag tag : tags) {
            this.tags.put(tag.tagId(), tag.tagName());
        }
    }


}
