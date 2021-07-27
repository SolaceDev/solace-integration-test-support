package com.solace.test.integration.semp.v2;

import com.solace.test.integration.semp.v2.test.SolaceEnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SempV2ApiIT {
	private final SempV2Api sempV2Api = new SempV2Api(SolaceEnv.MGMT_HOST.get(), SolaceEnv.MGMT_USERNAME.get(),
			SolaceEnv.MGMT_PASSWORD.get());

	@Test
	public void testGetPlatform() throws Exception {
		assertEquals("VMR", sempV2Api.action().getAboutApi().getData().getPlatform());
		assertEquals("VMR", sempV2Api.config().getAboutApi().getData().getPlatform());
		assertEquals("VMR", sempV2Api.monitor().getAboutApi().getData().getPlatform());
	}
}
