package manager;

public class FileBackedTaskManager extends InMemoryTaskManager{
    public FileBackedTaskManager(HistoryManager historyManager) {
        super(historyManager);
    }

    public void save(){

    }
}
