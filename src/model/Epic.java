package model;

import java.util.HashMap;
import java.util.Map;

public class Epic extends Task {
    private final Map<Integer, Subtask> subtasks;

    public Epic(String title, String description) {
        super(title, description);
        subtasks = new HashMap<>();
    }

    public void addOrUpdateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public Subtask removeSubtask(Subtask subtask) {
        return subtasks.remove(subtask.getId());
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}
