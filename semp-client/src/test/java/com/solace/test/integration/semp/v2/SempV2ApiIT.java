package com.solace.test.integration.semp.v2;

import com.solace.test.integration.testcontainer.PubSubPlusContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class SempV2ApiIT {
	@Container
	private static final PubSubPlusContainer CONTAINER = new PubSubPlusContainer();
	private final SempV2Api sempV2Api = new SempV2Api(CONTAINER.getOrigin(PubSubPlusContainer.Port.SEMP),
			CONTAINER.getAdminUsername(), CONTAINER.getAdminPassword());

	@Test
	public void testGetPlatform() throws Exception {
		assertEquals("VMR", sempV2Api.action().getAboutApi().getData().getPlatform());
		assertEquals("VMR", sempV2Api.config().getAboutApi().getData().getPlatform());
		assertEquals("VMR", sempV2Api.monitor().getAboutApi().getData().getPlatform());
	}
}
