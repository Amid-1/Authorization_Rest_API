package web.dto;

import java.util.List;

/**
 * DTO для ответа API.
 * Содержит только те поля, которые нужны клиенту:
 * - id
 * - username
 * - roleIds
 */
public class UserDto {
    private Long id;
    private String username;
    private List<Long> roleIds;

    public UserDto() {
    }

    public UserDto(Long id, String username, List<Long> roleIds) {
        this.id = id;
        this.username = username;
        this.roleIds = roleIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}