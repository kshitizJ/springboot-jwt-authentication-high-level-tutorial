package com.jwt.supportportal.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpResponse {

    // By default the date will be given in milli seconds so we need to format it.
    // Here we are using @JsonFormat annotation to format the data in DD - MM - YY
    // format. We also need to add time zone to tell which should be displayed when
    // we display the date.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Asia/Kolkata")
    private Date timeStamp;

    private Integer httpStatusCode;

    private HttpStatus httpStatus;

    private String reason;

    private String message;

    public HttpResponse(Integer httpStatusCode, HttpStatus httpStatus, String reason, String message) {
        this.timeStamp = new Date();
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
    }

}
