package manager;

class InMemoryHistoryManagerTest extends HistoryManagerTest {
    @Override
    protected HistoryManager init() {
        return Managers.getDefaultHistory();
    }
}