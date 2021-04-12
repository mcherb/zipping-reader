package com.mcherb.sb.zippingreader.model;

import com.mcherb.sb.zippingreader.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawExceptionDate implements Identifiable<String> {

    private String id;
    private String date;
    private String exceptionType;

    @Override
    public String id() {
        return id;
    }
}
