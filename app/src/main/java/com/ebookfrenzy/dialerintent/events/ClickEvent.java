package com.ebookfrenzy.dialerintent.events;

import java.util.HashMap;

/**
 * Created by Admin on 8/16/2016.
 */
public class ClickEvent {
    public HashMap getParameters() {
        return parameters;
    }

    public final HashMap parameters;

    public ClickEvent(HashMap parameters) {
        this.parameters = parameters;
    }
}
