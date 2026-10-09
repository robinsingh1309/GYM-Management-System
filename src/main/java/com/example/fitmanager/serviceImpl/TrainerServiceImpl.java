package com.example.fitmanager.serviceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fitmanager.dto.TrainerCreateRequest;
import com.example.fitmanager.dto.TrainerFilter;
import com.example.fitmanager.dto.TrainerResponse;
import com.example.fitmanager.dto.TrainerUpdateRequest;
import com.example.fitmanager.entity.Role;
import com.example.fitmanager.entity.Trainer;
import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.entity.TrainerStatus;
import com.example.fitmanager.entity.User;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.mapper.TrainerMapper;
import com.example.fitmanager.repository.TrainerRepository;
import com.example.fitmanager.repository.TrainerSpecifications;
import com.example.fitmanager.repository.UserRepository;
import com.example.fitmanager.service.TrainerService;


@Service
public class TrainerServiceImpl implements TrainerService {

    // Fields

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .’'-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Set<String> ALLOWED_SORT_PROPERTIES = //
            Set.of("id", "name", "joiningDate", "experienceYears", "createdAt");

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainerMapper trainerMapper;


    // Constructors
    // -------------------------------------------------------

    @Autowired
    public TrainerServiceImpl(final TrainerRepository trainerRepository, //
            final UserRepository userRepository, final TrainerMapper trainerMapper) {

        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.trainerMapper = trainerMapper;
    }


    // Methods
    // -------------------------------------------------------

    @Override
    @Transactional
    public TrainerResponse createTrainer(final TrainerCreateRequest request, //
            final boolean includeLinkedUser) {

        final LocalDate todayDate = LocalDate.now();

        final Trainer trainer = trainerMapper.toEntity(request, false);

        final String trainerName = trainer.getName();
        final String trainerEmail = trainer.getEmail();
        final String trainerPhoneNumber = trainer.getPhoneNumber();
        final LocalDate trainerJoiningDate = trainer.getJoiningDate();
        
        final Integer trainerExperienceYears = trainer.getExperienceYears();


        this.validateProfile(trainerName, trainerEmail, trainerPhoneNumber, //
                trainerJoiningDate, trainerExperienceYears);

        this.validateUniqueContacts(trainerEmail, trainerPhoneNumber, null);

        trainer.setActive(!trainerJoiningDate.isAfter(todayDate));


        final Long userId = request.getUserId();
        if (Objects.nonNull(userId)) {
            final User user = this.getEligibleUser(userId);
            trainer.setUser(user);
        }

        return trainerMapper.toResponse(trainerRepository.save(trainer), includeLinkedUser);
    }

    @Override
    @Transactional
    public TrainerResponse updateTrainer(final Long id, final TrainerUpdateRequest request, //
            final boolean includeLinkedUser) {

        final LocalDate todayDate = LocalDate.now();

        final Trainer trainer = this.getTrainer(id);
        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be updated");
        }

        trainerMapper.updateEntity(trainer, request);

        final String trainerName = trainer.getName();
        final String trainerEmail = trainer.getEmail();
        final String trainerPhoneNumber = trainer.getPhoneNumber();
        final LocalDate trainerJoiningDate = trainer.getJoiningDate();
        final Integer trainerExperienceYears = trainer.getExperienceYears();

        this.validateProfile(trainerName, trainerEmail, trainerPhoneNumber, //
                trainerJoiningDate, trainerExperienceYears);

        if (Boolean.TRUE.equals(trainer.getActive()) && trainerJoiningDate.isAfter(todayDate)) {
            throw new BadRequestException("Active Trainer cannot have a future joining date");
        }

        this.validateUniqueContacts(trainerEmail, trainerPhoneNumber, id);

        return trainerMapper.toResponse(trainerRepository.save(trainer), includeLinkedUser);
    }

    @Override
    @Transactional
    public void activateTrainer(final Long id) {

        final Trainer trainer = this.getTrainer(id);

        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be activated");
        }

        if (Boolean.TRUE.equals(trainer.getActive())) {
            throw new BadRequestException("Trainer is already active");
        }

        final LocalDate todayDate = LocalDate.now();
        final LocalDate trainerJoiningDate = trainer.getJoiningDate();
        if (trainerJoiningDate.isAfter(todayDate)) {
            throw new BadRequestException("Trainer cannot be activated before the joining date");
        }

        trainer.setActive(true);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void deactivateTrainer(final Long id) {

        final Trainer trainer = this.getTrainer(id);

        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be deactivated");
        }

        if (!Boolean.TRUE.equals(trainer.getActive())) {
            throw new BadRequestException("Trainer is already inactive");
        }

        trainer.setActive(false);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void deleteTrainer(final Long id) {

        final Trainer trainer = this.getTrainer(id);

        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Trainer is already deleted");
        }

        if (Boolean.TRUE.equals(trainer.getActive())) {
            throw new BadRequestException("Active Trainer must be deactivated before deletion");
        }

        trainer.setActive(false);
        trainer.setDeleted(true);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void restoreTrainer(final Long id) {

        final Trainer trainer = this.getTrainer(id);

        if (!Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Only a deleted Trainer can be restored");
        }

        trainer.setDeleted(false);
        trainer.setActive(false);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void linkUser(final Long trainerId, final Long userId) {

        final Trainer trainer = this.getTrainer(trainerId);

        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be linked to a User");
        }

        final User user = trainer.getUser();
        if (Objects.nonNull(user)) {
            throw new BadRequestException("Trainer is already linked to a User");
        }

        final User linkUserToTrainer = this.getEligibleUser(userId);
        trainer.setUser(linkUserToTrainer);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void unlinkUser(final Long trainerId) {

        final Trainer trainer = this.getTrainer(trainerId);

        final User user = trainer.getUser();
        if (Objects.isNull(user)) {
            throw new BadRequestException("Trainer is not linked to a User");
        }

        trainer.setUser(null);

        trainerRepository.save(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(final Long id, final boolean includeLinkedUser) {

        final Trainer trainer = this.getTrainer(id);

        if (Boolean.TRUE.equals(trainer.getDeleted()) && !includeLinkedUser) {
            throw new ResourceNotFoundException("Trainer not found with id: " + id);
        }

        return trainerMapper.toResponse(trainer, includeLinkedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrainerResponse> getTrainers(final TrainerFilter filter, //
            final int page, final int size, final String sort, final boolean includeLinkedUser) {

        this.validateFilter(filter, includeLinkedUser);

        final Pageable validatedPageable = this.validatePageable(page, size, sort);

        return trainerRepository //
                .findAll( //
                        TrainerSpecifications.withFilter(filter), validatedPageable //
                ) //
                .map(trainer -> trainerMapper.toResponse(trainer, includeLinkedUser));
    }

    @Override
    public List<TrainerSpecialization> getSpecializations() {
        return List.of(TrainerSpecialization.values());
    }


    // Helper Methods
    // -------------------------------------------------------

    private Trainer getTrainer(final Long id) {

        return trainerRepository.findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Trainer not found with id: " + id));
    }

    private void validateFilter(final TrainerFilter filter, final boolean isAdmin) {

        if (!isAdmin && (filter.getStatus() == TrainerStatus.DELETED //
                || Objects.nonNull(filter.getHasLinkedUser()) //
                || Objects.nonNull(filter.getUserId()) //
                || Objects.nonNull(filter.getEmail()) //
                || Objects.nonNull(filter.getPhoneNumber())) //
        ) {
            throw new AccessDeniedException("Trainer filter is restricted to ADMIN users");
        }

        trainerMapper.normalizeFilter(filter);

        final String name = filter.getName();
        if (Objects.nonNull(name)) {
            if (name.isBlank()) {
                throw new BadRequestException("Trainer name filter cannot be blank");
            }
        }

        final Set<TrainerSpecialization> specializations = filter.getSpecializations();
        if (Objects.nonNull(specializations) && specializations.isEmpty()) {
            throw new BadRequestException("Trainer specializations filter cannot be empty");
        }

        final LocalDate joiningDateFrom = filter.getJoiningDateFrom();
        final LocalDate joiningDateTo = filter.getJoiningDateTo();

        if (Objects.nonNull(joiningDateFrom) && Objects.nonNull(joiningDateTo) //
                && joiningDateFrom.isAfter(joiningDateTo)) //
        {
            throw new BadRequestException("Joining date range is invalid");
        }

        this.validateExperienceRange(filter);

        if (Boolean.FALSE.equals(filter.getHasLinkedUser()) && Objects.nonNull(filter.getUserId())) {
            throw new BadRequestException("hasLinkedUser=false cannot be combined with userId");
        }

        final String email = filter.getEmail();
        if (Objects.nonNull(email)) {
            if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new BadRequestException("Trainer email filter is invalid");
            }
        }

        final String phoneNumber = filter.getPhoneNumber();
        if (Objects.nonNull(phoneNumber) && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new BadRequestException("Trainer phone filter must contain exactly 10 digits");
        }

    }

    private void validateExperienceRange(final TrainerFilter filter) {

        final Integer minimum = filter.getMinExperienceYears();
        final Integer maximum = filter.getMaxExperienceYears();

        if ((Objects.nonNull(minimum) && (minimum < 0 || minimum > 60)) //
                || (Objects.nonNull(maximum) && (maximum < 0 || maximum > 60)) //
        ) {
            throw new BadRequestException("Trainer experience filter must be between 0 and 60");
        }

        if (Objects.nonNull(minimum) && Objects.nonNull(maximum) && minimum > maximum) {
            throw new BadRequestException("Trainer experience range is invalid");
        }
    }

    private Pageable validatePageable(final int page, final int size, final String sortExpression) {

        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("Trainer page must be at least 0 and size must be between 1 and 100");
        }

        final String[] sortParts = sortExpression.split(",", -1);
        if (sortParts.length != 2 || sortParts[0].isBlank() || sortParts[1].isBlank()) {
            throw new BadRequestException("Trainer sort must use property,direction format");
        }

        final String property = sortParts[0];
        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new BadRequestException("Unsupported Trainer sort property: " + property);
        }

        final Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortParts[1]);
        } catch (final IllegalArgumentException exception) {
            throw new BadRequestException("Trainer sort direction must be ASC or DESC");
        }

        Sort sort = Sort.by(direction, property);
        if (!"id".equals(property)) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        return PageRequest.of(page, size, sort);
    }

    private User getEligibleUser(final Long userId) {

        final User user = userRepository.findById(userId) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("Only active Users can be linked to a Trainer");
        }

        if (user.getRole() != Role.STAFF) {
            throw new BadRequestException("Only STAFF Users can be linked to a Trainer");
        }

        if (trainerRepository.existsByUser_Id(userId)) {
            throw new BadRequestException("User is already linked to a Trainer");
        }

        return user;
    }

    private void validateUniqueContacts(final String email, final String phoneNumber, final Long trainerId) {

        final boolean emailExists = //
                Objects.isNull(trainerId) //
                        ? trainerRepository.existsByEmail(email) //
                        : trainerRepository.existsByEmailAndIdNot(email, trainerId);
        if (emailExists) {
            throw new BadRequestException("Trainer email already exists: " + email);
        }

        final boolean phoneExists = //
                Objects.isNull(trainerId) //
                        ? trainerRepository.existsByPhoneNumber(phoneNumber) //
                        : trainerRepository.existsByPhoneNumberAndIdNot(phoneNumber, trainerId);
        if (phoneExists) {
            throw new BadRequestException("Trainer phone number already exists: " + phoneNumber);
        }
    }

    private void validateProfile(final String name, final String email, //
            final String phoneNumber, final LocalDate joiningDate, final Integer experienceYears) {

        if (name.length() < 2 || name.length() > 100 || !NAME_PATTERN.matcher(name).matches()) {
            throw new BadRequestException("Trainer name must be 2 to 100 valid characters");
        }

        if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Trainer email is invalid");
        }

        if (Objects.isNull(phoneNumber) || !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new BadRequestException("Trainer phone number must contain exactly 10 digits");
        }

        if (Objects.isNull(joiningDate)) {
            throw new BadRequestException("Trainer joining date is required");
        }

        if (joiningDate.isAfter(LocalDate.now().plusYears(5))) {
            throw new BadRequestException("Trainer joining date cannot be more than five years in the future");
        }

        if (Objects.nonNull(experienceYears) && (experienceYears < 0 || experienceYears > 60)) {
            throw new BadRequestException("Trainer experience years must be between 0 and 60");
        }
    }

}
