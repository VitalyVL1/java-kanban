package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация менеджера истории просмотров, хранящая данные в оперативной памяти.
 *
 * <p>Использует двусвязный список {@link Node} для хранения порядка просмотров
 * и {@link HashMap} для быстрого доступа к узлам по идентификатору задачи.
 * Такая структура обеспечивает O(1) доступ к любому элементу истории и
 * эффективное удаление.</p>
 *
 * <p>Особенности реализации:</p>
 * <ul>
 *   <li><b>Быстрый доступ</b> - поиск задачи в истории по ID за O(1)</li>
 *   <li><b>Связный список</b> - сохраняет порядок просмотров (от старых к новым)</li>
 *   <li><b>Защитные копии</b> - в историю сохраняются копии задач, чтобы избежать внешних модификаций</li>
 *   <li><b>Удаление дубликатов</b> - повторный просмотр перемещает задачу в конец</li>
 * </ul>
 *
 * @see HistoryManager
 * @see Node
 */
public class InMemoryHistoryManager implements HistoryManager {

    /** Карта для быстрого доступа к узлам по ID задачи */
    private final Map<Integer, Node<Task>> history = new HashMap<>();

    /** Первый (самый старый) узел в истории */
    private Node<Task> first;

    /** Последний (самый новый) узел в истории */
    private Node<Task> last;

    /**
     * {@inheritDoc}
     *
     * <p>Реализация с двусвязным списком:</p>
     * <ul>
     *   <li>Если задача уже есть в истории, она удаляется со старой позиции</li>
     *   <li>Новая задача добавляется в конец списка (становится самой новой)</li>
     *   <li>В истории хранятся копии задач, оригиналы не изменяются</li>
     * </ul>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Возвращает список задач в порядке от самой старой к самой новой,
     * проходя по связному списку от {@link #first} до {@link #last}.</p>
     */
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Удаляет задачу из истории за O(1) благодаря прямому доступу к узлу
     * через {@link #history} и корректной перелинковке соседних узлов.</p>
     */
    @Override
    public void remove(int id) {
        if (history.isEmpty() || !history.containsKey(id)) {
            return;
        }

        removeNode(history.remove(id));
    }

    /**
     * Внутренний класс для представления узла двусвязного списка.
     *
     * <p>Каждый узел содержит ссылки на предыдущий и следующий узлы,
     * что позволяет эффективно удалять элементы из середины списка.</p>
     *
     * @param <T> тип задачи (Task, Epic или Subtask)
     */
    private class Node<T extends Task> {
        /** Данные узла - задача */
        private T data;
        /** Ссылка на следующий узел */
        private Node<T> next;
        /** Ссылка на предыдущий узел */
        private Node<T> previous;

        /**
         * Создает новый узел с заданными данными.
         *
         * @param data задача для хранения в узле
         */
        public Node(T data) {
            this.data = data;
        }

        /**
         * Проверяет, есть ли следующий узел.
         *
         * @return true если есть следующий узел
         */
        public boolean hasNext() {
            return next != null;
        }

        /**
         * Проверяет, есть ли предыдущий узел.
         *
         * @return true если есть предыдущий узел
         */
        public boolean hasPrevious() {
            return previous != null;
        }
    }

    /**
     * Добавляет задачу в конец двусвязного списка.
     *
     * <p>Создает новый узел и связывает его с текущим последним узлом.
     * Обновляет ссылки {@link #first} и {@link #last} при необходимости.</p>
     *
     * @param task задача для добавления
     * @return созданный узел или null, если задача null
     */
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

    /**
     * Возвращает список всех задач из истории в порядке от старых к новым.
     *
     * <p>Проходит по связному списку от {@link #first} до {@link #last},
     * создавая защитные копии каждой задачи.</p>
     *
     * @return список задач из истории (может быть пустым)
     */
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

    /**
     * Удаляет указанный узел из двусвязного списка.
     *
     * <p>Корректно обновляет ссылки соседних узлов и указатели
     * {@link #first} и {@link #last} при удалении крайних элементов.</p>
     *
     * @param node узел для удаления
     */
    private void removeNode(Node<Task> node) {
        if (node == null) return;

        if (node == first) {
            first = first.next;
        } else {
            if (node.hasPrevious()) node.previous.next = node.next;
        }

        if (node == last) {
            last = last.previous;
        } else {
            if (node.hasNext()) node.next.previous = node.previous;
        }
    }
}