package app;

public abstract class Member {
    private final int id;
    private String name;
    private int age;

    private static void checkAge(int age) {
        if (age < 18 || age > 120) {
            throw new InvalidDataException("возраст должен быть от 18 до 120");
        }
    }

    private static void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("имя клиента не может быть пустым");
        }
    }

    protected Member(String name, int age, int id) {
        checkName(name);
        checkAge(age);
        this.name = name.trim();
        this.age = age;
        this.id = id;
    }

    public void update(String name, int age) {
        checkName(name);
        checkAge(age);
        this.name = name.trim();
        this.age = age;
    }

    public boolean isAdult() {
        return age >= 18;
    }

    public void registerVisit() {}

    public void cancelVisit() {}

    public abstract boolean canBook();

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkName(name);
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        checkAge(age);
        this.age = age;
    }

    public abstract MemberType type();

    public int getVisits() {
        return 0;
    }
}