package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> history = new HashMap<>();
    private Node<Task> first;
    private Node<Task> last;

    @Override
    public void add(Task task) {
        if (task == null) return;

        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }

        if (task instanceof Subtask) {
            history.put(task.getId(), linkLast(new Subtask((Subtask) task)));
        } else if (task instanceof Epic) {
            history.put(task.getId(), linkLast(new Epic((Epic) task)));
        } else {
            history.put(task.getId(), linkLast(new Task(task)));
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (history.isEmpty() || !history.containsKey(id)) {
            return;
        }

        removeNode(history.remove(id));
    }

    private class Node<T extends Task> {
        private T data;
        private Node<T> next;
        private Node<T> previous;

        public Node(T data) {
            this.data = data;
        }
    }

    private Node<Task> linkLast(Task task) {
        if (task == null) return null;

        Node<Task> newNode = new Node<>(task);

        if (first == null) {
            first = newNode;
        } else {
            last.next = newNode;
            newNode.previous = last;
        }
        last = newNode;

        return newNode;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> current = first;

        while (current != null) {
            Task temp = current.data;

            if (temp instanceof Subtask) {
                tasks.add(new Subtask((Subtask) temp));
            } else if (temp instanceof Epic) {
                tasks.add(new Epic((Epic) temp));
            } else {
                tasks.add(new Task(temp));
            }

            current = current.next;
        }

        return tasks;
    }

    private void removeNode(Node<Task> node) {
        if (node == null) return;

        if (node == first) {
            first = first.next;
        } else {
            node.previous.next = node.next;
        }

        if (node == last) {
            last = last.previous;
        } else {
            node.next.previous = node.previous;
        }
    }
}
