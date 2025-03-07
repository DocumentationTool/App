package com.wonkglorg.doc.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

/**
 * Custom error controller that overrides Spring Boot's default error handling behavior.
 * <p>
 * This controller prevents redirection to the `/error` endpoint and instead returns a structured
 * JSON response containing relevant error details.
 * </p>
 */
@Controller
public class CustomErrorController implements ErrorController{
	private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);
	private final ErrorAttributes errorAttributes;
	
	/**
	 * Constructs a new {@code CustomErrorController} with the provided {@code ErrorAttributes}.
	 *
	 * @param errorAttributes An instance of {@link ErrorAttributes} used to extract error details.
	 */
	public CustomErrorController(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}
	
	/**
	 * Handles errors and provides a JSON response instead of redirecting to the default `/error`
	 * page.
	 * <p>
	 * This method retrieves error attributes from the request, formats them into a structured JSON
	 * response,
	 * and returns the appropriate HTTP status code.
	 * </p>
	 *
	 * @param request The incoming HTTP request containing error details.
	 * @return A {@link ResponseEntity} containing a JSON-formatted error response.
	 */
	@RequestMapping("/error")
	public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
		
		var errorDetails = errorAttributes.getErrorAttributes(new ServletWebRequest(request), ErrorAttributeOptions.defaults());
		
		// Determine the appropriate HTTP status code (default to 500 if not provided)
		HttpStatus status = HttpStatus.valueOf((Integer) errorDetails.getOrDefault("status", 500));
		
		Map<String, Object> response = Map.of("message",
				errorDetails.get("error"),
				"status",
				status.value(),
				"timestamp",
				errorDetails.get("timestamp"),
				"path",
				errorDetails.get("path"));
		
		log.error("Error occurred: {}", response);
		
		return new ResponseEntity<>(response, status);
	}
}
