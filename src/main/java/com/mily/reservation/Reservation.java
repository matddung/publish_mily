package com.mily.reservation;

import com.mily.user.LawyerUser;
import com.mily.user.MilyUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"lawyerUserId", "reservationTime"})})
@Getter
@Setter
@Component
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@SuperBuilder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "reservationTime")
    private LocalDateTime reservationTime;

    @ManyToOne
    @JoinColumn(name = "milyUserId")
    private MilyUser milyUser;

    @ManyToOne
    @JoinColumn(name = "lawyerUserId")
    private LawyerUser lawyerUser;
}