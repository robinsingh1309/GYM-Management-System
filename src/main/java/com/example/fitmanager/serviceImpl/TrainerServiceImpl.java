package com.example.fitmanager.serviceImpl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
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

import com.example.fitmanager.dto.AdminTrainerResponse;
import com.example.fitmanager.dto.AdminTrainerResponse.LinkedUserResponse;
import com.example.fitmanager.dto.TrainerCreateRequest;
import com.example.fitmanager.dto.TrainerFilter;
import com.example.fitmanager.dto.TrainerResponse;
import com.example.fitmanager.dto.TrainerUpdateRequest;
import com.example.fitmanager.entity.Role;
import com.example.fitmanager.entity.Trainer;
import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.entity.User;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
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


    // Constructors
    // -------------------------------------------------------

    @Autowired
    public TrainerServiceImpl(final TrainerRepository trainerRepository, //
            final UserRepository userRepository) {

        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
    }


    // Methods
    // -------------------------------------------------------

    @Override
    @Transactional
    public TrainerResponse createTrainer(final TrainerCreateRequest request, //
            final boolean includeLinkedUser) {

        final String name = normalizeName(request.getName());
        final String email = normalizeEmail(request.getEmail());
        final String phoneNumber = request.getPhoneNumber();
        final LocalDate joiningDate = request.getJoiningDate();
        final Integer experienceYears = request.getExperienceYears();
        final Set<TrainerSpecialization> specializations = normalizeSpecializations(request.getSpecializations());

        validateProfile(name, email, phoneNumber, joiningDate, experienceYears);
        validateUniqueContacts(email, phoneNumber, null);

        final boolean active = !joiningDate.isAfter(LocalDate.now());
        final Trainer trainer = new Trainer(name, email, phoneNumber, joiningDate, //
                experienceYears, specializations, active);

        if (Objects.nonNull(request.getUserId())) {
            trainer.setUser(getEligibleUser(request.getUserId()));
        }

        return toResponse(trainerRepository.save(trainer), includeLinkedUser);
    }

    @Override
    @Transactional
    public TrainerResponse updateTrainer(final Long id, final TrainerUpdateRequest request, //
            final boolean includeLinkedUser) {

        final Trainer trainer = getTrainer(id);
        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be updated");
        }

        final String name = normalizeName(request.getName());
        final String email = normalizeEmail(request.getEmail());
        final String phoneNumber = request.getPhoneNumber();
        final LocalDate joiningDate = request.getJoiningDate();
        final Integer experienceYears = request.getExperienceYears();
        final Set<TrainerSpecialization> specializations = normalizeSpecializations(request.getSpecializations());

        validateProfile(name, email, phoneNumber, joiningDate, experienceYears);
        if (Boolean.TRUE.equals(trainer.getActive()) && joiningDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Active Trainer cannot have a future joining date");
        }
        validateUniqueContacts(email, phoneNumber, id);

        trainer.setName(name);
        trainer.setEmail(email);
        trainer.setPhoneNumber(phoneNumber);
        trainer.setJoiningDate(joiningDate);
        trainer.setExperienceYears(experienceYears);
        trainer.setSpecializations(specializations);

        return toResponse(trainerRepository.save(trainer), includeLinkedUser);
    }

    @Override
    @Transactional
    public void activateTrainer(final Long id) {

        final Trainer trainer = getTrainer(id);
        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be activated");
        }
        if (Boolean.TRUE.equals(trainer.getActive())) {
            throw new BadRequestException("Trainer is already active");
        }
        if (trainer.getJoiningDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Trainer cannot be activated before the joining date");
        }

        trainer.setActive(true);
        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void deactivateTrainer(final Long id) {

        final Trainer trainer = getTrainer(id);
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

        final Trainer trainer = getTrainer(id);
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

        final Trainer trainer = getTrainer(id);
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

        final Trainer trainer = getTrainer(trainerId);
        if (Boolean.TRUE.equals(trainer.getDeleted())) {
            throw new BadRequestException("Deleted Trainer cannot be linked to a User");
        }
        if (Objects.nonNull(trainer.getUser())) {
            throw new BadRequestException("Trainer is already linked to a User");
        }

        trainer.setUser(getEligibleUser(userId));
        trainerRepository.save(trainer);
    }

    @Override
    @Transactional
    public void unlinkUser(final Long trainerId) {

        final Trainer trainer = getTrainer(trainerId);
        if (Objects.isNull(trainer.getUser())) {
            throw new BadRequestException("Trainer is not linked to a User");
        }

        trainer.setUser(null);
        trainerRepository.save(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(final Long id, final boolean includeLinkedUser) {

        final Trainer trainer = getTrainer(id);
        if (Boolean.TRUE.equals(trainer.getDeleted()) && !includeLinkedUser) {
            throw new ResourceNotFoundException("Trainer not found with id: " + id);
        }

        return toResponse(trainer, includeLinkedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrainerResponse> getTrainers(final TrainerFilter filter, //
            final int page, final int size, final String sort, final boolean includeLinkedUser) {

        validateFilter(filter, includeLinkedUser);
        final Pageable validatedPageable = validatePageable(page, size, sort);

        return trainerRepository.findAll(TrainerSpecifications.withFilter(filter), validatedPageable) //
                .map(trainer -> toResponse(trainer, includeLinkedUser));
    }

    @Override
    public java.util.List<TrainerSpecialization> getSpecializations() {
        return java.util.List.of(TrainerSpecialization.values());
    }


    // Helper Methods
    // -------------------------------------------------------

    private Trainer getTrainer(final Long id) {

        return trainerRepository.findById(id) //
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));
    }

    private void validateFilter(final TrainerFilter filter, final boolean isAdmin) {

        if (!isAdmin && (filter.getStatus() == com.example.fitmanager.entity.TrainerStatus.DELETED //
                || Objects.nonNull(filter.getHasLinkedUser()) //
                || Objects.nonNull(filter.getUserId()) //
                || Objects.nonNull(filter.getEmail()) //
                || Objects.nonNull(filter.getPhoneNumber()))) {
            throw new AccessDeniedException("Trainer filter is restricted to ADMIN users");
        }

        if (Objects.nonNull(filter.getName())) {
            final String normalizedName = normalizeName(filter.getName());
            if (normalizedName.isBlank()) {
                throw new BadRequestException("Trainer name filter cannot be blank");
            }
            filter.setName(normalizedName);
        }
        if (Objects.nonNull(filter.getSpecializations()) && filter.getSpecializations().isEmpty()) {
            throw new BadRequestException("Trainer specializations filter cannot be empty");
        }
        if (Objects.nonNull(filter.getJoiningDateFrom()) && Objects.nonNull(filter.getJoiningDateTo()) //
                && filter.getJoiningDateFrom().isAfter(filter.getJoiningDateTo())) {
            throw new BadRequestException("Joining date range is invalid");
        }
        validateExperienceRange(filter);
        if (Boolean.FALSE.equals(filter.getHasLinkedUser()) && Objects.nonNull(filter.getUserId())) {
            throw new BadRequestException("hasLinkedUser=false cannot be combined with userId");
        }
        if (Objects.nonNull(filter.getEmail())) {
            final String email = normalizeEmail(filter.getEmail());
            if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new BadRequestException("Trainer email filter is invalid");
            }
            filter.setEmail(email);
        }
        if (Objects.nonNull(filter.getPhoneNumber()) //
                && !PHONE_PATTERN.matcher(filter.getPhoneNumber()).matches()) {
            throw new BadRequestException("Trainer phone filter must contain exactly 10 digits");
        }
    }

    private void validateExperienceRange(final TrainerFilter filter) {

        final Integer minimum = filter.getMinExperienceYears();
        final Integer maximum = filter.getMaxExperienceYears();
        if ((Objects.nonNull(minimum) && (minimum < 0 || minimum > 60)) //
                || (Objects.nonNull(maximum) && (maximum < 0 || maximum > 60))) {
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

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

        final boolean emailExists = Objects.isNull(trainerId) //
                ? trainerRepository.existsByEmail(email) //
                : trainerRepository.existsByEmailAndIdNot(email, trainerId);
        if (emailExists) {
            throw new BadRequestException("Trainer email already exists: " + email);
        }

        final boolean phoneExists = Objects.isNull(trainerId) //
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

    private String normalizeName(final String name) {

        if (Objects.isNull(name)) {
            throw new BadRequestException("Trainer name is required");
        }
        return name.strip().replaceAll("(?U)\\s+", " ");
    }

    private String normalizeEmail(final String email) {

        if (Objects.isNull(email)) {
            throw new BadRequestException("Trainer email is required");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private Set<TrainerSpecialization> normalizeSpecializations( //
            final Set<TrainerSpecialization> specializations) {

        return Objects.isNull(specializations) ? new HashSet<>() : new HashSet<>(specializations);
    }

    private TrainerResponse toResponse(final Trainer trainer, final boolean includeLinkedUser) {

        if (!includeLinkedUser) {
            return new TrainerResponse(trainer.getId(), trainer.getName(), trainer.getEmail(), //
                    trainer.getPhoneNumber(), trainer.getJoiningDate(), trainer.getExperienceYears(), //
                    trainer.getSpecializations(), trainer.getActive(), trainer.getDeleted(), //
                    trainer.getStatus(), trainer.getCreatedAt(), trainer.getUpdatedAt());
        }

        final User user = trainer.getUser();
        final LinkedUserResponse linkedUser = Objects.isNull(user) //
                ? null //
                : new LinkedUserResponse(user.getId(), user.getEmail(), user.getRole(), user.getActive());

        return new AdminTrainerResponse(trainer.getId(), trainer.getName(), trainer.getEmail(), //
                trainer.getPhoneNumber(), trainer.getJoiningDate(), trainer.getExperienceYears(), //
                trainer.getSpecializations(), trainer.getActive(), trainer.getDeleted(), //
                trainer.getStatus(), trainer.getCreatedAt(), trainer.getUpdatedAt(), linkedUser);
    }
}
