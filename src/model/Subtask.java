package model;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String title, String description, Epic epic) {
        super(title, description);
        this.epic = epic;
        epic.addOrUpdateSubtask(this);
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
       this.status = status;
       epic.checkStatus();
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
