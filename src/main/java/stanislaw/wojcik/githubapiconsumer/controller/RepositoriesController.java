package stanislaw.wojcik.githubapiconsumer.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stanislaw.wojcik.githubapiconsumer.entity.Repository;
import stanislaw.wojcik.githubapiconsumer.exception.InvalidHeaderException;
import stanislaw.wojcik.githubapiconsumer.service.RepositoriesService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RepositoriesController {

    private final RepositoriesService service;

    @GetMapping("/repositories/{username}")
    public ResponseEntity<List<Repository>> getUserRepositories(
            @RequestHeader(HttpHeaders.ACCEPT) final String acceptHeader, @PathVariable final String username) {
        checkHeader(acceptHeader);
        return new ResponseEntity<>(service.getRepositories(username), HttpStatus.OK);
    }

    private void checkHeader(final String header) {
        if (header.isBlank() || !header.equals("application/json")) {
            throw new InvalidHeaderException("Invalid headers provided.");
        }
    }
}
