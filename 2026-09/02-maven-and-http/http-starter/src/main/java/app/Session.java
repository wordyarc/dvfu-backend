package app;

import java.util.ArrayList;
import java.util.List;

public class Session {

    private final int id;
    private String name;
    private int capacity;
    private final List<Integer> memberIds = new ArrayList<>();

    public Session(int id, String name, int capacity) {
        checkName(name);
        checkCapacity(capacity);
        this.id = id;
        this.name = name.trim();
        this.capacity = capacity;
    }

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

    public int getCapacity() {
        return capacity;
    }

    public void update(String name, int capacity) {
        checkName(name);
        checkCapacity(capacity);
        if (capacity < memberIds.size()) {
            throw new RuleViolationException("Нельзя сделать мест меньше, чем уже записано клиентов");
        }
        this.name = name.trim();
        this.capacity = capacity;
    }
    public List<Integer> getMemberIds() {
        return new ArrayList<>(memberIds);
    }

    public int getFreePlaces() {
        return capacity - memberIds.size();
    }

    public boolean hasFreePlaces() {
        return getFreePlaces() > 0;
    }

    public boolean isBooked(int memberId) {
        return memberIds.contains(memberId);
    }

    public boolean isEmpty() {
        return memberIds.isEmpty();
    }

    public void addMember(int memberId) {
        if (isBooked(memberId)) {
            throw new RuleViolationException("Клиент уже записан на это занятие");
        }
        if (!hasFreePlaces()) {
            throw new RuleViolationException("На занятии нет свободных мест");
        }
        memberIds.add(memberId);
    }

    public void removeMember(int memberId) {
        boolean removed = memberIds.remove(Integer.valueOf(memberId));
        if (!removed) {
            throw new RuleViolationException("Клиент не записан на это занятие");
        }
    }

    private static void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("Название занятия не может быть пустым");
        }
    }

    private static void checkCapacity(int capacity) {
        if (capacity < 1 || capacity > 500) {
            throw new InvalidDataException("Вместимость должна быть от 1 до 500");
        }
    }
}