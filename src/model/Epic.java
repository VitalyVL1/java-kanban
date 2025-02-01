package model;

import java.util.HashMap;
import java.util.Map;

public class Epic extends Task {
    private Map<Integer, Subtask> subtasks;

    public Epic(String title, String description) {
        super(title, description);
        subtasks = new HashMap<>();
    }

    public void addOrUpdateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        checkStatus();
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public Subtask removeSubtask(Subtask subtask) {
        Subtask removedSubtask = subtasks.remove(subtask.getId());
        checkStatus();
        return removedSubtask;
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

   public void checkStatus() {
        int subtasksCount = subtasks.size();

        if (subtasksCount == 0) {
            status = TaskStatus.NEW;
            return;
        }

        long doneCount = subtasks.values().stream().filter(subtask -> subtask.status == TaskStatus.DONE).count();
        long newCount = subtasks.values().stream().filter(subtask -> subtask.status == TaskStatus.NEW).count();

        if (doneCount == subtasksCount) {
            status = TaskStatus.DONE;
        } else if (newCount == subtasksCount) {
            status = TaskStatus.NEW;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public void setStatus(TaskStatus status) {
        checkStatus();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
