package app;

public abstract class LimitedMember extends Member {

    private int visits = 0;

    protected LimitedMember(String name, int age, int id) {
        super(name, age, id);
    }

    protected abstract int limit();

    public int getVisits() {
        return visits;
    }

    public int remainingVisits() {
        return limit() - visits;
    }

    @Override
    public boolean canBook() {
        return visits < limit();
    }

    @Override
    public void registerVisit() {
        if (!canBook()) {
            throw new RuleViolationException("Невозможно записаться: лимит посещений исчерпан");
        }
        visits++;
    }

    @Override
    public void cancelVisit() {
        if (visits == 0) {
            throw new RuleViolationException("Невозможно отменить посещение, так как оно не было зарегистрировано");
        }
        visits--;
    }
}