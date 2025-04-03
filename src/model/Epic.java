package model;

import java.util.HashSet;
import java.util.Set;

public class Epic extends Task {
    private Set<Integer> subtasksId;

    public Epic(String title, String description) {
        super(title, description);
        subtasksId = new HashSet<>();
        setType(TaskType.EPIC);
    }

    public Epic(Epic epic) {
        super(epic);
        subtasksId = new HashSet<>(epic.subtasksId);
    }

    public Epic(Task task, Set<Integer> subtasks) {
        super(task);
        this.subtasksId = subtasks;
    }

    public Epic(Integer id, String title, String description, TaskStatus status, TaskType type, Set<Integer> subtasks) {
        super(id, title, description, status, type);
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
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", type=" + getType() +
                ", title=" + getTitle() +
                ", description=" + getDescription() +
                ", status=" + getStatus() +
                '}';
    }
}
