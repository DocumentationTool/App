package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;
import static com.wonkglorg.doc.api.DocApiApplication.DEV_USER;
import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_ROLE;

@RestController
@RequestMapping(API_ROLE)
public class ApiRoleController {


    private final RepoService repoService;

    public ApiRoleController(RepoService repoService) {
        this.repoService = repoService;
    }

    @GetMapping("/get")
    public ResponseEntity<RestResponse<List<Role>>> getRoles(@RequestParam("repoId") RepoId repoId, @RequestParam("userId") UserId userId) {

        if (DEV_MODE) {
            return RestResponse.success(DEV_USER.getRoles()).toResponse();
        }

        //return RestResponse.success(List.of(Role.values())).toResponse();
        return repoService.getUserPermissions();
    }

    @PostMapping("/add")
    public ResponseEntity<RestResponse<Void>> addRole(@RequestParam String userId, @RequestParam Role role) {
        //return RestResponse.success("Role added").toResponse();
        return null;
    }

    @PostMapping("/remove")
    public ResponseEntity<RestResponse<Void>> removeRole(@RequestParam String userId, @RequestParam Role role) {
        //return RestResponse.success("Role removed").toResponse();
        return null;
    }

}
