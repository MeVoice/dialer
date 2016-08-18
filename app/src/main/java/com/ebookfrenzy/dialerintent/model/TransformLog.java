package com.ebookfrenzy.dialerintent.model;

import java.util.Date;

/**
 * Created by Admin on 8/17/2016.
 */
public class TransformLog {
    private String originalNumber;

    public TransformLog(String newNumber, String originalNumber, Date timestamp) {
        this.newNumber = newNumber;
        this.originalNumber = originalNumber;
        this.timestamp = timestamp;
    }

    public String getNewNumber() {
        return newNumber;
    }

    public String getOriginalNumber() {
        return originalNumber;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    private String newNumber;
    private Date timestamp;
}
