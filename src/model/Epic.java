package model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Epic extends Task {
    private Map<Integer, Subtask> subtasks;

    public Epic(String title, String description) {
        super(title, description);
        subtasks = new HashMap<>();
        setType(TaskType.EPIC);
    }

    public Epic(Epic epic) {
        super(epic);
        this.subtasks = epic.getSubtasks().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new Subtask(e.getValue())));
    }

    public Epic(Task task, Map<Integer, Subtask> subtasks) {
        super(task);
        this.subtasks = subtasks;
    }

    public Epic(Integer id, String title, String description, TaskStatus status, TaskType type, Map<Integer, Subtask> subtasks) {
        super(id, title, description, status, type);
        this.subtasks = subtasks;
    }

    public void addOrUpdateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(Map<Integer, Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask.getId());
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
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
