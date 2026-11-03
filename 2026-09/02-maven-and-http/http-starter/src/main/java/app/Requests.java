package app;

import app.MemberType;

public final class Requests {

    private Requests() {
    }

    public record MemberRequest(MemberType type, String name, Integer age) {
    }

    public record SessionRequest(String name, Integer capacity) {
    }

    public record BookRequest(Integer memberId) {
    }
}