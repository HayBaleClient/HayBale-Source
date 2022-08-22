package me.luxtix.haybale.event.events;

public interface EventPriority {
    public static final int HIGHEST = 200;
    public static final int HIGH = 100;
    public static final int MEDIUM = 0;
    public static final int LOW = -100;
    public static final int LOWEST = -200;
    public static final int DEFAULT = 0;
}