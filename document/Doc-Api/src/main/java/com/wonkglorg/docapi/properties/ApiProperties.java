package com.wonkglorg.docapi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("doc.web.api")
public class ApiProperties {

	public class Ports {
		/**
		 * Defines the port over which spring is being started
		 */
		private int spring = 8080;
		/**
		 * Defines the port over which angular frontend is being started
		 */
		private int angular = 4200;

		public int getAngular() {
			return angular;
		}

		public int getSpring() {
			return spring;
		}

		public void setAngular(int angular) {
			this.angular = angular;
		}

		public void setSpring(int spring) {
			this.spring = spring;
		}
	}

	/**
	 * All whitelisted pages that can be accessed without user permissions
	 */
	private List<String> whitelist = new ArrayList<>();
	private Ports ports = new Ports();

	public Ports getPorts() {
		return ports;
	}
	
	public List<String> getWhitelist() {
		return whitelist;
	}

	public void setPorts(Ports ports) {
		this.ports = ports;
	}
	
	public void setWhitelist(List<String> whitelist) {
		this.whitelist = whitelist;
	}
}
