package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;
import static com.wonkglorg.doc.api.DocApiApplication.DEV_USERS;
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
        if (repoService.isValidRepo(repoId)) {
            return RestResponse.<List<Role>>error("Invalid Repo").toResponse();
        }
        if (DEV_MODE) {
            Map<UserId, UserProfile> userProfileMap = DEV_USERS.get(repoId);
            if (userProfileMap == null) {
                return RestResponse.<List<Role>>error("No users in repo found").toResponse();
            }
            UserProfile userProfile = userProfileMap.get(userId);
            return RestResponse.success(userProfile.getRoles()).toResponse();
        }
        //return repoService.getUserPermissions(repoId,userId);
        return null;
    }

    @PostMapping("/add")
    public ResponseEntity<RestResponse<Void>> addRole(@RequestParam("repoID") RepoId repoId, @RequestParam UserId userId, @RequestParam Role role) {
        if (DEV_MODE) {
            DEV_USERS.putIfAbsent(repoId, new HashMap<>()).putIfAbsent(userId, new ArrayList<>()).add(role);
            return RestResponse.<Void>success("Added Role to user'", null).toResponse();
        }

        //return RestResponse.success("Role added").toResponse();
        return null;
    }

    @PostMapping("/remove")
    public ResponseEntity<RestResponse<Void>> removeRole(@RequestParam String userId, @RequestParam Role role) {
        //return RestResponse.success("Role removed").toResponse();
        return null;
    }

}
