package com.geek_alarm.android.tasks;

/**
 * TaskType describes particular kind of task. E.g. inverse matrix or derivative.
  */
public class TaskType {

    public static enum Level {
        NONE(0), EASY(1), MEDIUM(2), HARD(3);

        private int value;

        private Level(int value) {
            this.value = value;
        }

        public static Level fromValue(int value) {
            for (Level level : values()) {
                if (level.getValue() == value) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Cannont find level for " + value);
        }

        public int getValue() {
            return value;
        }
    }

    private String type;
    private String name;
    private String description;
    private Level level;

    public TaskType(String type, String name, String description, Level level)  {
        this.description = description;
        this.level = level;
        this.name = name;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public Level getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}