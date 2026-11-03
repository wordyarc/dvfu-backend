package app;

public class VipMember extends Member {

    public VipMember(String name, int age, int id) {
        super(name, age, id);
    }
    private int visits = 0;
    @Override
    public boolean canBook() {
        return true;
    }
    @Override
    public void registerVisit() {
        visits++;
    }
    @Override
    public void cancelVisit() {
        if (visits > 0) {
            visits--;
        } else {
            throw new RuleViolationException("Невозможно отменить посещение, так как оно не было зарегистрировано");
        }

    }
    @Override public MemberType type() { return MemberType.VIP; }
    @Override public int getVisits() { return visits; }

}
