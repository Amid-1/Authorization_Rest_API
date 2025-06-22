package web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import web.dto.UserDetailsDto;
import web.service.UserDetailsService;

@RestController
@RequestMapping("/api/users/{userId}/details")
public class UserDetailsController {

    private final UserDetailsService detailsService;

    public UserDetailsController(UserDetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @GetMapping
    public UserDetailsDto get(@PathVariable Long userId) {
        return detailsService.getDetails(userId);
    }

    @PostMapping
    public ResponseEntity<UserDetailsDto> create(
            @PathVariable Long userId,
            @RequestBody @Valid UserDetailsDto dto
    ) {
        UserDetailsDto created = detailsService.createOrUpdate(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public UserDetailsDto update(
            @PathVariable Long userId,
            @RequestBody @Valid UserDetailsDto dto
    ) {
        return detailsService.createOrUpdate(userId, dto);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        detailsService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
