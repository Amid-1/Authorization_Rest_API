package web.service;

import web.dto.UserDetailsDto;

public interface UserDetailsService {
    UserDetailsDto getDetails(Long userId);
    UserDetailsDto createOrUpdate(Long userId, UserDetailsDto dto);
    void delete(Long userId);
}