package com.wonkglorg.docapi;

import com.wonkglorg.doc.core.request.ResourceRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RequestTest{
	@Test
	void canResourceRequestProperlyHandleNulls() {
		ResourceRequest request = new ResourceRequest();
		
		Assertions.assertFalse(request.targetPath().isPresent());
		Assertions.assertFalse(request.targetPath().isAntPath());
	}
}
