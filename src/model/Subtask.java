package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String title, String description, Epic epic, LocalDateTime startTime, Duration duration) {
        super(title, description, startTime, duration);
        this.epicId = epic.getId();
        setType(TaskType.SUBTASK);
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicId = subtask.getEpicId();
    }

    public Subtask(Integer id, String title, String description, TaskStatus status, TaskType type, Epic epic, LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, type, startTime, duration);
        this.epicId = epic.getId();
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpic(Epic epic) {
        this.epicId = epic.getId();
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", type=" + getType() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                '}';
    }
}
