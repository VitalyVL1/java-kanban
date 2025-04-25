package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Epic extends Task {
    private Set<Integer> subtasksId;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description, null, Duration.ZERO);
        subtasksId = new HashSet<>();
        setType(TaskType.EPIC);
    }

    public Epic(Epic epic) {
        super(epic);
        subtasksId = new HashSet<>(epic.subtasksId);
        endTime = epic.endTime;
    }

    public Epic(Integer id, String title, String description, TaskStatus status, TaskType type, Set<Integer> subtasks, LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, type, startTime, duration);
        this.subtasksId = subtasks;
    }

    public void addOrUpdateSubtask(Subtask subtask) {
        subtasksId.add(subtask.getId());
    }

    public Set<Integer> getSubtasksId() {
        return subtasksId;
    }

    public void setSubtasks(Set<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    public void removeSubtask(Subtask subtask) {
        subtasksId.remove(subtask.getId());
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
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
