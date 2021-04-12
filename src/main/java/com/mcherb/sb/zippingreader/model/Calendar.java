package com.mcherb.sb.zippingreader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calendar {

    private String id;

    private OffsetDateTime created;

    private String label;

    private LocalDate fromDate;

    private LocalDate toDate;

    private List<ExceptionDate> exceptionDates;
}
