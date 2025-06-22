package web.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.dto.UserDetailsDto;
import web.mapper.UserDetailsMapper;
import web.model.UserDetails;
import web.model.User;
import web.repository.UserDetailsRepository;
import web.repository.UserRepository;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDetailsRepository detailsRepo;
    private final UserRepository userRepo;
    private final UserDetailsMapper detailsMapper;

    public UserDetailsServiceImpl(UserDetailsRepository detailsRepo,
                                  UserRepository userRepo,
                                  UserDetailsMapper detailsMapper) {
        this.detailsRepo   = detailsRepo;
        this.userRepo      = userRepo;
        this.detailsMapper = detailsMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsDto getDetails(Long userId) {
        UserDetails det = detailsRepo.findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Details for user " + userId + " not found"));
        return detailsMapper.toDto(det);
    }

    @Override
    @Transactional
    public UserDetailsDto createOrUpdate(Long userId, UserDetailsDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found: " + userId));

        UserDetails det = detailsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserDetails newDet = new UserDetails();
                    newDet.setUser(user);
                    return newDet;
                });

        detailsMapper.updateEntity(dto, det);
        UserDetails saved = detailsRepo.save(det);
        return detailsMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        detailsRepo.findByUserId(userId)
                .ifPresent(detailsRepo::delete);
    }
}