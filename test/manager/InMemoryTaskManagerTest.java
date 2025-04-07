package manager;

class InMemoryTaskManagerTest extends TaskManagerTest <InMemoryTaskManager>{
    @Override
    protected TaskManager init() {
        return Managers.getDefault();
    }
}