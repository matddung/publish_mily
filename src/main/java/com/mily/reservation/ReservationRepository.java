package com.mily.reservation;

import com.mily.user.LawyerUser;
import com.mily.user.MilyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByLawyerUserAndReservationTimeBetween(LawyerUser lawyerUser, LocalDateTime startTime, LocalDateTime endTime);

    List<Reservation> findByLawyerUserIdAndReservationTime(Long lawyerUserId, LocalDateTime dateTime);

    boolean existsByLawyerUserIdAndReservationTime(Long lawyerUserId, LocalDateTime reservationTime);

    Optional<Reservation> findByReservationTime(LocalDateTime reservationTime);

    List<Reservation> findByMilyUserId(long id);

    List<Reservation> findByLawyerUserId(long id);

    List<Reservation> findByMilyUser(MilyUser isLoginedUser);
}