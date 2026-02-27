package com.mily.reservation;

import com.mily.standard.util.Ut;
import com.mily.user.LawyerUser;
import com.mily.user.MilyUser;
import com.mily.user.MilyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequestMapping("/reservation")
@RequiredArgsConstructor
@Controller
public class ReservationController {
    private final ReservationService reservationService;
    private final MilyUserService milyUserService;

    @PostMapping("")
    public String saveReservation(@ModelAttribute Reservation reservation, RedirectAttributes redirectAttributes, Long lawyerUserId) {
        MilyUser milyUser = milyUserService.getCurrentUser();

        if(!milyUser.getRole().equals("member")) {
            throw new Ut.DataNotFoundException("권한이 없습니다.");
        }

        LawyerUser lawyerUser = milyUserService.getLawyer(lawyerUserId).getLawyerUser();

        try {
            reservationService.createReservationIfAvailable(milyUser, lawyerUser, reservation.getReservationTime());
            redirectAttributes.addFlashAttribute("message", "예약이 성공적으로 저장되었습니다.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 저장 중 오류가 발생했습니다 : " + ex.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/{reservationId}/refuse")
    public String refuseReservation(@PathVariable Long reservationId, RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.getReservation(reservationId);
            reservationService.refuseReservation(reservation);
            redirectAttributes.addFlashAttribute("message", "예약이 성공적으로 거절되었습니다.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 거절 중 오류가 발생했습니다: " + ex.getMessage());
        }
        return "redirect:/";
    }

    // 변호사 ID와 날짜에 따른 시간 선택 페이지
    @GetMapping("/available_times")
    public String getAvailableTimes(@RequestParam Long lawyerUserId, @RequestParam LocalDate date, Model model) {
        try {
            LawyerUser lawyerUser = milyUserService.getLawyer(lawyerUserId).getLawyerUser();
            List<LocalDateTime> availableTimes = reservationService.getAvailableTimes(lawyerUserId, date);
            model.addAttribute("availableTimes", availableTimes);
            model.addAttribute("lawyerUserId", lawyerUserId);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "예약 가능한 시간 조회 중 오류가 발생했습니다: " + ex.getMessage());
        }
        return "available_times";
    }

    // 변호사 ID에 따른 날짜 선택 페이지
    @GetMapping("/select_date")
    public String selectDate(@RequestParam("lawyerUserId") Long lawyerUserId, Model model) {
        MilyUser lawyerUser = milyUserService.findById(lawyerUserId).get();
        MilyUser isLoginedUser = milyUserService.getCurrentUser();

        if (isLoginedUser == null || !isLoginedUser.role.equals("member")) {
            return "redirect:/";
        }

        List<String> dates = new ArrayList<>();
        List<String> daysOfWeek = new ArrayList<>();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(7);

        model.addAttribute("start", start);
        model.addAttribute("end", end);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");

        dates.add(start.format(formatter));
        daysOfWeek.add("오늘");

        for (int i = 1; i < 7; i++) {
            dates.add(start.plusDays(i).format(formatter));
            String dayOfWeek = start.plusDays(i).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
            daysOfWeek.add(dayOfWeek.substring(0, 1));
        }

        model.addAttribute("user", lawyerUser);
        model.addAttribute("lawyerUserId", lawyerUserId);
        model.addAttribute("dates", dates);
        model.addAttribute("day", daysOfWeek);

        return "select_date";
    }

    @GetMapping("/getAvailableTimes")
    public ResponseEntity<List<LocalDateTime>> allAvailableTimes(Long lawyerUserId, String id) {
        LocalDate now = LocalDate.now();
        MilyUser lawyerUser = milyUserService.findById(lawyerUserId).get();

        char lastIndex = id.charAt(id.length() -1);
        int index = Character.getNumericValue(lastIndex);

        LocalDate date = now.plusDays(index);

        List<LocalDateTime> availableTimes = reservationService.getAvailableTimes(lawyerUser.getLawyerUser().getId(), date);

        return ResponseEntity.ok().body(availableTimes);
    }

    @GetMapping("/result")
    public String getResult (@RequestParam Long lawyerUserId, @RequestParam String consultation, @RequestParam String selectedDate, @RequestParam String selectedTime, Model model) {

        MilyUser isLoginedUser = milyUserService.getCurrentUser();
        MilyUser lawyerUser = milyUserService.findById(lawyerUserId).get();

        List<LocalDate> localDates = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        String dayOfWeek;

        LocalDate start = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        localDates.add(start);
        dates.add(start.format(formatter));

        for (int i = 1; i < 7; i++) {
            dates.add(start.plusDays(i).format(formatter));
            localDates.add(start.plusDays(i));
        }

        int index = dates.indexOf(selectedDate);
        LocalDate selectDate = localDates.get(index);

        LocalTime parsedTime = LocalTime.parse(selectedTime);
        LocalDateTime reservationTime = selectDate.atTime(parsedTime);

        dayOfWeek = selectDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN).substring(0, 1);

        try {
            reservationService.createReservationIfAvailable(isLoginedUser, lawyerUser.getLawyerUser(), reservationTime);
        } catch (IllegalStateException ex) {
            return "redirect:/user/lawyers";
        }

        model.addAttribute("lawyer", lawyerUser);
        model.addAttribute("user", isLoginedUser);
        model.addAttribute("selectedTime", reservationTime);
        model.addAttribute("day", dayOfWeek);
        model.addAttribute("time", selectedTime);

        return "reservation_success";
    }
}