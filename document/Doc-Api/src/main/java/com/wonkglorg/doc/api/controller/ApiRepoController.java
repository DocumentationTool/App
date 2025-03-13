package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.json.JsonRepo;
import com.wonkglorg.doc.api.service.RepoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_REPO;

@RestController
@RequestMapping(API_REPO)
public class ApiRepoController {

    private final RepoService repoService;

    public ApiRepoController(@Lazy RepoService repoService) {
        this.repoService = repoService;
    }

    @Operation(
            summary = "Gets all existing repos",
            description = "Returns a list of repos and their properties"
    )
    @GetMapping("repository/get")
    public ResponseEntity<RestResponse<List<JsonRepo>>> getRepos() {
        return RestResponse.success(JsonRepo.from(repoService.getProperties())).toResponse();
    }

}
