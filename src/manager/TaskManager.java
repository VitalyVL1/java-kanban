package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    List<Epic> getEpics();

    List<Task> getTasks();

    List<Subtask> getSubtasks();

    int addEpic(Epic epic);

    int addTask(Task task);

    int addSubtask(Subtask subtask);

    Epic removeEpic(int id);

    Task removeTask(int id);

    Subtask removeSubtask(int id);

    List<Subtask> getAllSubtasksByEpic(Epic epic);

    void clearEpics();

    void clearTasks();

    void clearSubtasks();

    void updateEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    List<Task> getHistory();
}
