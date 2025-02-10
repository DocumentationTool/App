package com.wonkglorg.docapi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("api.cross-origin-port")
public class PortProperties {

	/**
	 * Defines the port over which spring is being started
	 */
	private int spring = 8080;
	/**
	 * Defines the port over which angular frontend is being started
	 */
	private int angular = 4200;

	public int getSpring() {
		return spring;
	}

	public void setSpring(int spring) {
		this.spring = spring;
	}

	public int getAngular() {
		return angular;
	}

	public void setAngular(int angular) {
		this.angular = angular;
	}
}
