package com.wonkglorg.doc.api.json;

import com.wonkglorg.doc.core.objects.Tag;

public class JsonTag {
    public String tagId;
    public String tagName;

    public JsonTag(Tag tag) {
        this.tagId = tag.tagId().id();
        this.tagName = tag.tagName();
    }
}
