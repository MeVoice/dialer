package com.demitive.callrouter.events;

import java.util.HashMap;

/**
 * Created by Admin on 8/16/2016.
 */
public class ClickEvent {
    public Object get(Object key) {
        return event.get(key);
    }

    public final HashMap event=new HashMap();

    public ClickEvent() {
    }
    public void put(Object key, Object value) {
        event.put(key, value);
    }
}
