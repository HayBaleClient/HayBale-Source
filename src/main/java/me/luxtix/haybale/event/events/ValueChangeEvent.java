package me.luxtix.haybale.event.events;

import me.luxtix.haybale.event.EventStage;
import me.luxtix.haybale.features.setting.Setting;

public class ValueChangeEvent
        extends EventStage {
    public Setting setting;
    public Object value;

    public ValueChangeEvent(Setting setting, Object value) {
        this.setting = setting;
        this.value = value;
    }
}

