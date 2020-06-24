package com.solace.test.integration.semp.v2;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.solace.test.integration.semp.v2.action.ApiClient;
import com.solace.test.integration.semp.v2.config.api.AllApi;

import java.util.logging.Logger;

public class SempV2Api {
	private final com.solace.test.integration.semp.v2.config.api.AllApi configApi;
	private final com.solace.test.integration.semp.v2.action.api.AllApi actionApi;
	private final com.solace.test.integration.semp.v2.monitor.api.AllApi monitorApi;

	private static final Logger LOG = Logger.getLogger(SempV2Api.class.getName());

	public SempV2Api(String mgmtHost, String mgmtUsername, String mgmtPassword) {
		LOG.info(String.format("Creating Action API Clients for %s", mgmtHost));
		ApiClient actionApiClient = new ApiClient();
		actionApiClient.getJSON().getContext(null).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		actionApiClient.setBasePath(String.format("%s/SEMP/v2/action", mgmtHost));
		actionApiClient.setUsername(mgmtUsername);
		actionApiClient.setPassword(mgmtPassword);

		LOG.info(String.format("Creating Config API Clients for %s", mgmtHost));
		com.solace.test.integration.semp.v2.config.ApiClient configApiClient = new com.solace.test.integration.semp.v2.config.ApiClient();
		configApiClient.getJSON().getContext(null).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		configApiClient.setBasePath(String.format("%s/SEMP/v2/config", mgmtHost));
		configApiClient.setUsername(mgmtUsername);
		configApiClient.setPassword(mgmtPassword);

		LOG.info(String.format("Creating Monitor API Clients for %s", mgmtHost));
		com.solace.test.integration.semp.v2.monitor.ApiClient monitorApiClient = new com.solace.test.integration.semp.v2.monitor.ApiClient();
		monitorApiClient.getJSON().getContext(null).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		monitorApiClient.setBasePath(String.format("%s/SEMP/v2/monitor", mgmtHost));
		monitorApiClient.setUsername(mgmtUsername);
		monitorApiClient.setPassword(mgmtPassword);

		this.actionApi = new com.solace.test.integration.semp.v2.action.api.AllApi(actionApiClient);
		this.configApi = new com.solace.test.integration.semp.v2.config.api.AllApi(configApiClient);
		this.monitorApi = new com.solace.test.integration.semp.v2.monitor.api.AllApi(monitorApiClient);
	}

	public AllApi config() {
		return configApi;
	}

	public com.solace.test.integration.semp.v2.action.api.AllApi action() {
		return actionApi;
	}

	public com.solace.test.integration.semp.v2.monitor.api.AllApi monitor() {
		return monitorApi;
	}
}
