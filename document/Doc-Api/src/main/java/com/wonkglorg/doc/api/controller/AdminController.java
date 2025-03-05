package com.wonkglorg.doc.api.controller;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.ADMIN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ADMIN)
@RestController
public class AdminController{
	private static final Logger log = LoggerFactory.getLogger(AdminController.class);

	//admin specific controller logic here
	
}
