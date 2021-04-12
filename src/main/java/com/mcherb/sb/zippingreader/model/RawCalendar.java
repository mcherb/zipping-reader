package com.mcherb.sb.zippingreader.model;

import com.mcherb.sb.zippingreader.Identifiable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RawCalendar implements Identifiable<String> {

    private String id;
    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;
    private String saturday;
    private String sunday;
    private String startDate;
    private String endDate;


    @Override
    public String id() {
        return id;
    }
}
