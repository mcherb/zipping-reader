package com.mcherb.sb.zippingreader.model;

import lombok.Value;

import java.util.List;

@Value
public class RawCompleteCalendar {
    RawCalendar calendar;
    List<RawExceptionDate> rawExceptionDate;
}
