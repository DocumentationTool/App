package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.wonkglorg.doc.api.DocApiApplication.DEV_MODE;
import static com.wonkglorg.doc.api.DocApiApplication.DEV_USER;
import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.API_USER;

/**
 * Handles all api user specific requests
 */
@Controller
@RequestMapping(API_USER)
public class ApiUserController {
    //todo, setup specific api key to bypass role permissions (when the app itself needs to make
    // changes / be setup for new admins)
    private static final Logger log = LoggerFactory.getLogger(ApiUserController.class);

    private final RepoService repoManager;

    public ApiUserController(RepoService repoManager) {
        this.repoManager = repoManager;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/add")
    public ResponseEntity<String> addUser(@RequestParam("repo") RepoId repo, @RequestParam("userID") UserId userId,
                                          @RequestParam("password") String password) {
        log.info("Received PUT request to add user to repo = '{}'with ID='{}'. Password provided: {}", repo, userId,
                password != null ? "*".repeat(password.length()) : "MISSING");
        if (DEV_MODE) {
            return new ResponseEntity<>(DEV_USER.toString(), HttpStatus.OK);
        }
        repoManager.getRepo(repo).getDatabase().addUser(userId, password, "example");

        return new ResponseEntity<>("", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/remove")
    public ResponseEntity<Profile> deleteUser(@RequestParam("userID") String userId) {
        log.info("Received PUT request to delete user with userID='{}'", userId);
        return ResponseEntity.ok(null);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("get/{id}")
    public ResponseEntity<UserProfile> getUser(@PathVariable("id") String id) {
        log.info("Received GET request to retrieve user with userID='{}'", id);
        if (DEV_MODE) {
            log.info("DEVMODE: getting user profile.");
            return new ResponseEntity<>(DEV_USER, HttpStatus.OK);
        }
        return ResponseEntity.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<Profile> updateUser(@RequestBody UserProfile userId) {
        log.info("Received PUT request to update user profile for userID='{}'", userId.getId());
        return ResponseEntity.ok(null);
    }


}
