package service;

import manager.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    public static final int HISTORY_SIZE = 10;

    private final List<Task> history = new ArrayList<>(HISTORY_SIZE);

    @Override
    public void add(Task task) {
        if (history.size() == HISTORY_SIZE) {
            history.removeFirst();
        }

        if (task instanceof Epic) {
            history.add(new Epic((Epic) task));
        } else if (task instanceof Subtask) {
            history.add(new Subtask((Subtask) task));
        } else {
            history.add(new Task(task));
        }

    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void remove(Task task) {
        if (task == null || history.isEmpty()) {
            return;
        }

        while (history.contains(task)) {
            history.remove(task);
        }
    }
}
