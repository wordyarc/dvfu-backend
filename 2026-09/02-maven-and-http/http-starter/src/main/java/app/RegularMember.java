package app;

public class RegularMember extends Member {

    public RegularMember(String name, int age, int id) {
        super(name, age, id);
    }

    @Override
    public boolean canBook() {
        return true;
    }

    @Override
    public MemberType type() {
        return MemberType.REGULAR;
    }
}