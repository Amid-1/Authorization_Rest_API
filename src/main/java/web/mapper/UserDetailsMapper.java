package web.mapper;

import org.springframework.stereotype.Component;
import web.dto.UserDetailsDto;
import web.model.UserDetails;

@Component
public class UserDetailsMapper {

    public UserDetailsDto toDto(UserDetails ent) {
        if (ent == null) return null;
        UserDetailsDto dto = new UserDetailsDto();
        dto.setUserId(ent.getUser().getId());
        dto.setFirstName(ent.getFirstName());
        dto.setLastName(ent.getLastName());
        dto.setMiddleName(ent.getMiddleName());
        dto.setEmail(ent.getEmail());
        dto.setDateOfBirth(ent.getDateOfBirth());
        dto.setPhoneNumber(ent.getPhoneNumber());
        dto.setPhotoUrl(ent.getPhotoUrl());
        return dto;
    }

    public void updateEntity(UserDetailsDto dto, UserDetails ent) {
        ent.setFirstName(dto.getFirstName());
        ent.setLastName(dto.getLastName());
        ent.setMiddleName(dto.getMiddleName());
        ent.setEmail(dto.getEmail());
        ent.setDateOfBirth(dto.getDateOfBirth());
        ent.setPhoneNumber(dto.getPhoneNumber());
        ent.setPhotoUrl(dto.getPhotoUrl());
    }
}