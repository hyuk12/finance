package com.finance.demo.shared.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 날짜 범위를 나타내는 값 객체
 * Day 2: Specification 패턴에서 사용할 날짜 범위
 */
public class DateRange {
    
    private final LocalDateTime start;
    private final LocalDateTime end;
    
    private DateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("시작일과 종료일은 필수입니다");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }
        this.start = start;
        this.end = end;
    }
    
    public static DateRange of(LocalDateTime start, LocalDateTime end) {
        return new DateRange(start, end);
    }
    
    public static DateRange lastDays(int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);
        return new DateRange(start, end);
    }
    
    public static DateRange currentMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                               .withHour(23).withMinute(59).withSecond(59);
        return new DateRange(start, end);
    }
    
    public static DateRange lastMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonth = now.minusMonths(1);
        LocalDateTime start = lastMonth.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth())
                                    .withHour(23).withMinute(59).withSecond(59);
        return new DateRange(start, end);
    }
    
    public boolean contains(LocalDateTime dateTime) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
    
    public boolean overlaps(DateRange other) {
        return !this.end.isBefore(other.start) && !other.end.isBefore(this.start);
    }
    
    public long getDays() {
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }
    
    public LocalDateTime start() {
        return start;
    }
    
    public LocalDateTime end() {
        return end;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return Objects.equals(start, dateRange.start) && Objects.equals(end, dateRange.end);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
    
    @Override
    public String toString() {
        return String.format("DateRange{%s ~ %s}", start, end);
    }
}
