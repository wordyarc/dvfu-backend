package app;

public class GoldMember extends LimitedMember {

    public GoldMember(String name, int age, int id) {
        super(name, age, id);
    }

    @Override
    protected int limit() {
        return 12;
    }

    @Override
    public MemberType type() {
        return MemberType.GOLD;
    }
}