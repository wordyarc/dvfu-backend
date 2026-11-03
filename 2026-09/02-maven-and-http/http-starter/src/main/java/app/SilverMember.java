package app;

public class SilverMember extends LimitedMember {

    public SilverMember(String name, int age, int id) {
        super(name, age, id);
    }

    @Override
    protected int limit() {
        return 8;
    }

    @Override
    public MemberType type() {
        return MemberType.SILVER;
    }
}