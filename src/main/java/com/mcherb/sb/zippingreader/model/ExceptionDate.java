package com.mcherb.sb.zippingreader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDate {

    private long techId;
    private LocalDate date;
    private boolean circulating;
}
