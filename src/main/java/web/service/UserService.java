package web.service;

import web.dto.UserCreateDto;
import web.dto.UserDto;
import web.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    UserDto createUser(UserCreateDto dto);
    UserDto updateUser(Long id, UserUpdateDto dto);
    void deleteUser(Long id);
}