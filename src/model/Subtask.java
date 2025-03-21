package model;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String title, String description, Epic epic) {
        super(title, description);
        this.epic = epic;
        setType(TaskType.SUBTASK);
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epic = subtask.getEpic();
    }

    public Subtask(Integer id, String title, String description, TaskStatus status, TaskType type, Epic epic) {
        super(id, title, description, status, type);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", type=" + getType() +
                ", title=" + getTitle() +
                ", description=" + getDescription() +
                ", status=" + getStatus() +
                '}';
    }
}
