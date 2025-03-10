package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonRoles;
import com.wonkglorg.doc.api.service.RepoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_ROLE;

@RestController
@RequestMapping(API_ROLE)
public class ApiRoleController {

    private final RepoService repoService;

    public ApiRoleController(@Lazy RepoService repoService) {
        this.repoService = repoService;
    }

    //@formatter:off
	@Operation(
			summary = "Get a user's Roles",
			description = "Returns a list of Roles the user has. If a repository is given, only returns roles for that repository. If none is given, returns roles for all repositories."
	)
	@GetMapping("/get")
	public ResponseEntity<RestResponse<Map<String,JsonRoles>>> getRoles(
			@Parameter(description = "The repoId to search in. If none is given, returns the result for all currently loaded repos.")
			@RequestParam(value = "repoId",required = false) String repoId,
			@Parameter(description = "The userId to search roles for, can be null to find all existing roles.")
			@RequestParam(value = "userId",required = false) String userId) {
		return null;
	}
	//@formatter:on
}
