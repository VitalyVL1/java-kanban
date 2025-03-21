package model;

public class Subtask extends Task {
    private final Epic epic;

    public Subtask(String title, String description, Epic epic) {
        super(title, description);
        this.epic = epic;
        setType(TaskType.SUBTASK);
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epic = subtask.getEpic();
    }

    public Epic getEpic() {
        return epic;
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
