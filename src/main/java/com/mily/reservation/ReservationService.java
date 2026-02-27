package com.mily.reservation;

import com.mily.user.LawyerUser;
import com.mily.user.MilyUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;

    @Transactional
    public void createReservationIfAvailable(MilyUser milyUser, LawyerUser lawyerUser, LocalDateTime time) {
        boolean exists = reservationRepository.existsByLawyerUserIdAndReservationTime(lawyerUser.getId(), time);

        if (exists) {
            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        Reservation reservation = new Reservation();
        reservation.setMilyUser(milyUser);
        reservation.setLawyerUser(lawyerUser);
        reservation.setReservationTime(time);

        try {
            reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("이미 예약된 시간입니다.", ex);
        }
    }

    // 예약 거절
    @Transactional
    public void refuseReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    // 예약 가능한 시간 표시
    public List<LocalDateTime> getAvailableTimes(Long lawyerUserId, LocalDate reservationTime) {
        List<LocalDateTime> availableTimes = new ArrayList<>();
        LocalDateTime dateTime;
        for (int hour = 9; hour < 18; hour++) {
            if (hour != 12) {
                dateTime = reservationTime.atTime(hour, 0);
                List<Reservation> findAvailableTimes = reservationRepository.findByLawyerUserIdAndReservationTime(lawyerUserId, dateTime);
                if (findAvailableTimes.isEmpty()) {
                    availableTimes.add(dateTime);
                }
            }
        }

        return availableTimes;
    }

    public Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow();
    }

    public Optional<Reservation> findByReservationTime(LocalDateTime reservationTime) {
        return reservationRepository.findByReservationTime(reservationTime);
    }

    public List<Reservation> findByMilyUser(MilyUser milyUser) {
        return reservationRepository.findByMilyUserId(milyUser.getId());
    }

    public List<Reservation> findByLawyerUserId(long id) {
        return reservationRepository.findByLawyerUserId(id);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}