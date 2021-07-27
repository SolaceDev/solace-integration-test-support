package com.solace.test.integration.semp.v2.test;

import java.util.Optional;

public enum SolaceEnv {
	MGMT_HOST("SOLACE_MGMT_HOST", "http://localhost:8080"),
	MGMT_USERNAME("SOLACE_MGMT_USERNAME", "admin"),
	MGMT_PASSWORD("SOLACE_MGMT_PASSWORD", "admin");

	private final String name;
	private final String defaultValue;

	SolaceEnv(String name, String defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String get() {
		return get(defaultValue);
	}

	public String get(String defaultValue) {
		return Optional.ofNullable(System.getenv(getName())).orElse(defaultValue);
	}

	public String getName() {
		return name;
	}
}
