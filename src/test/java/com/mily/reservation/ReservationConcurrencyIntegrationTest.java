package com.mily.reservation;

import com.mily.user.LawyerUser;
import com.mily.user.LawyerUserRepository;
import com.mily.user.MilyUser;
import com.mily.user.MilyUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
class ReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MilyUserRepository milyUserRepository;

    @Autowired
    private LawyerUserRepository lawyerUserRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("동일한 예약 슬롯에 동시에 요청하면 하나만 성공하고 나머지는 실패한다")
    void createReservationIfAvailable_allowsOnlyOneReservationUnderConcurrency() throws InterruptedException {
        MilyUser member = milyUserRepository.saveAndFlush(MilyUser.builder()
                .userLoginId("member-concurrency")
                .userPassword("pw")
                .userName("member")
                .userEmail("member-concurrency@test.com")
                .userPhoneNumber("010-0000-0001")
                .userDateOfBirth("1990-01-01")
                .role("member")
                .build());

        MilyUser lawyerBase = milyUserRepository.saveAndFlush(MilyUser.builder()
                .userLoginId("lawyer-concurrency")
                .userPassword("pw")
                .userName("lawyer")
                .userEmail("lawyer-concurrency@test.com")
                .userPhoneNumber("010-0000-0002")
                .userDateOfBirth("1991-01-01")
                .role("lawyer")
                .build());

        jdbcTemplate.update(
                "insert into lawyer_user (mily_user_id, major, introduce, office_address, license_number, area, profile_img_file_path) values (?, ?, ?, ?, ?, ?, ?)",
                lawyerBase.getId(), "civil", "intro", "seoul", "L-12345", "SEOUL", null
        );

        Long memberId = member.getId();
        Long lawyerId = lawyerBase.getId();

        LocalDateTime targetTime = LocalDateTime.of(2026, 6, 1, 10, 0);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        List<Throwable> unexpectedErrors = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    MilyUser managedMember = milyUserRepository.findById(memberId).orElseThrow();
                    LawyerUser managedLawyer = lawyerUserRepository.findById(lawyerId).orElseThrow();
                    reservationService.createReservationIfAvailable(managedMember, managedLawyer, targetTime);
                    successCount.incrementAndGet();
                } catch (IllegalStateException expected) {
                    failCount.incrementAndGet();
                } catch (Throwable throwable) {
                    synchronized (unexpectedErrors) {
                        unexpectedErrors.add(throwable);
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        List<Reservation> saved = reservationRepository.findByLawyerUserIdAndReservationTime(lawyerId, targetTime);

        String unexpectedErrorSummary = unexpectedErrors.isEmpty()
                ? "none"
                : unexpectedErrors.stream()
                .map(throwable -> throwable.getClass().getSimpleName() + ": " + throwable.getMessage())
                .collect(Collectors.joining(" | "));
        System.out.println("[ReservationConcurrencyIntegrationTest] final-result "
                + "threadCount=" + threadCount
                + ", successCount=" + successCount.get()
                + ", failCount=" + failCount.get()
                + ", savedCount=" + saved.size()
                + ", unexpectedErrors=" + unexpectedErrorSummary);

        assertThat(unexpectedErrors).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(saved).hasSize(1);
    }
}