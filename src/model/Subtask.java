package model;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String title, String description, Epic epic) {
        super(title, description);
        this.epicId = epic.getId();
        setType(TaskType.SUBTASK);
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicId = subtask.getEpicId();
    }

    public Subtask(Integer id, String title, String description, TaskStatus status, TaskType type, Epic epic) {
        super(id, title, description, status, type);
        this.epicId = epic.getId();
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpic(Epic epic) {
        this.epicId = epic.getId();
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
