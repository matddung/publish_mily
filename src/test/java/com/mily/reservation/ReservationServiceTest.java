package com.mily.reservation;

import com.mily.user.LawyerUser;
import com.mily.user.MilyUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository);
    }

    @Test
    @DisplayName("예약 가능하면 예약을 저장한다")
    void createReservationIfAvailable_savesWhenSlotIsAvailable() {
        MilyUser user = MilyUser.builder().id(10L).build();
        LawyerUser lawyer = LawyerUser.builder().id(20L).build();
        LocalDateTime time = LocalDateTime.of(2026, 3, 1, 10, 0);

        when(reservationRepository.existsByLawyerUserIdAndReservationTime(20L, time)).thenReturn(false);

        reservationService.createReservationIfAvailable(user, lawyer, time);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());

        Reservation saved = captor.getValue();
        assertThat(saved.getMilyUser()).isEqualTo(user);
        assertThat(saved.getLawyerUser()).isEqualTo(lawyer);
        assertThat(saved.getReservationTime()).isEqualTo(time);
    }

    @Test
    @DisplayName("이미 예약된 시간이라면 저장하지 않고 예외를 던진다")
    void createReservationIfAvailable_throwsWhenAlreadyExists() {
        MilyUser user = MilyUser.builder().id(10L).build();
        LawyerUser lawyer = LawyerUser.builder().id(20L).build();
        LocalDateTime time = LocalDateTime.of(2026, 3, 1, 10, 0);

        when(reservationRepository.existsByLawyerUserIdAndReservationTime(20L, time)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservationIfAvailable(user, lawyer, time))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 예약된 시간");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("동시성으로 DB unique 제약 위반이 발생하면 IllegalStateException으로 변환한다")
    void createReservationIfAvailable_translatesDataIntegrityViolation() {
        MilyUser user = MilyUser.builder().id(10L).build();
        LawyerUser lawyer = LawyerUser.builder().id(20L).build();
        LocalDateTime time = LocalDateTime.of(2026, 3, 1, 10, 0);

        when(reservationRepository.existsByLawyerUserIdAndReservationTime(20L, time)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> reservationService.createReservationIfAvailable(user, lawyer, time))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 예약된 시간");
    }

    @Test
    @DisplayName("예약 거절 시 delete를 호출한다")
    void refuseReservation_deletesReservation() {
        Reservation reservation = Reservation.builder().id(1L).build();

        reservationService.refuseReservation(reservation);

        verify(reservationRepository).delete(reservation);
    }
}