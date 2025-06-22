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

/**
 * Сервис для работы с подробной информацией пользователя.
 * Реализует создание, чтение, обновление и удаление UserDetails.
 */
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

    /**
     * Получает существующие детали пользователя из БД.
     *
     * @param userId ID пользователя
     * @return DTO с деталями пользователя
     * @throws EntityNotFoundException если детали для данного userId не найдены
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailsDto getDetails(Long userId) {
        UserDetails det = detailsRepo.findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Details for user " + userId + " not found")
                );
        return detailsMapper.toDto(det);
    }

    /**
     * Создаёт или обновляет детали пользователя.
     * Если записи нет, создаётся новая сущность.
     *
     * @param userId ID пользователя
     * @param dto    DTO с данными для сохранения
     * @return DTO сохранённой сущности
     * @throws EntityNotFoundException если пользователя с таким ID нет
     */
    @Override
    @Transactional
    public UserDetailsDto createOrUpdate(Long userId, UserDetailsDto dto) {
        // Проверяем, что пользователь существует
        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found: " + userId)
                );

        // Либо загружаем существующие детали, либо создаём новую запись
        UserDetails det = detailsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserDetails newDet = new UserDetails();
                    newDet.setUser(user);
                    return newDet;
                });

        // Маппим поля из DTO в сущность
        detailsMapper.updateEntity(dto, det);
        UserDetails saved = detailsRepo.save(det);

        return detailsMapper.toDto(saved);
    }

    /**
     * Удаляет детали пользователя, если они есть.
     *
     * @param userId ID пользователя
     */
    @Override
    @Transactional
    public void delete(Long userId) {
        detailsRepo.findByUserId(userId)
                .ifPresent(detailsRepo::delete);
    }
}