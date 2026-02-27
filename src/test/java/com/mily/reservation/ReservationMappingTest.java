package com.mily.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMappingTest {

    @Test
    @DisplayName("Reservation 엔티티에 lawyerUserId + reservationTime unique 제약이 선언되어 있다")
    void reservation_hasUniqueConstraintOnLawyerAndTime() {
        Table table = Reservation.class.getAnnotation(Table.class);

        assertThat(table).isNotNull();
        UniqueConstraint[] constraints = table.uniqueConstraints();

        assertThat(constraints).isNotEmpty();

        List<String> expected = Arrays.asList("lawyerUserId", "reservationTime");

        assertThat(Arrays.stream(constraints)
                .map(UniqueConstraint::columnNames)
                .anyMatch(columns -> Arrays.asList(columns).containsAll(expected) && columns.length == 2))
                .isTrue();
    }

    @Test
    @DisplayName("JoinColumn/Column 이름이 unique 제약 컬럼과 일치한다")
    void reservation_columnNames_matchConstraintColumns() throws NoSuchFieldException {
        Field lawyerUser = Reservation.class.getDeclaredField("lawyerUser");
        JoinColumn lawyerJoinColumn = lawyerUser.getAnnotation(JoinColumn.class);

        Field reservationTime = Reservation.class.getDeclaredField("reservationTime");
        Column reservationTimeColumn = reservationTime.getAnnotation(Column.class);

        assertThat(lawyerJoinColumn).isNotNull();
        assertThat(lawyerJoinColumn.name()).isEqualTo("lawyerUserId");

        assertThat(reservationTimeColumn).isNotNull();
        assertThat(reservationTimeColumn.name()).isEqualTo("reservationTime");
    }
}