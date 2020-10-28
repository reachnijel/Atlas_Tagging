package com.manulife.edl.atlas.tags.tagObject;

import java.util.Vector;

public class Tag {
    private String tagName;
    private String guid;
    private String tagDesc;
    private Vector<String> attributes;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTagDesc() {
        return tagDesc;
    }

    public void setTagDesc(String tagDesc) {
        this.tagDesc = tagDesc;
    }

    public Vector<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Vector<String> attributes) {
        this.attributes = attributes;
    }
}
