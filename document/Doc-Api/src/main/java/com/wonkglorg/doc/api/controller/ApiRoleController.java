package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.core.permissions.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_ROLE;

@RestController
@RequestMapping(API_ROLE)
public class ApiRoleController {

    @GetMapping("/get")
    public ResponseEntity<RestResponse<List<Role>>> getRoles(@RequestParam String userId) {
        //return RestResponse.success(List.of(Role.values())).toResponse();
        return null;
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
