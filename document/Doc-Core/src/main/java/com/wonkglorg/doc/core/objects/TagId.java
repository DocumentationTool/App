package com.wonkglorg.doc.core.objects;

public record TagId(String id) implements Identifyable{
	public static TagId of(String id) {
		return new TagId(id);
	}
	
	@Override
	public String toString() {
		return id;
	}
}
