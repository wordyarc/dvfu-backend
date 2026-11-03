package app;

import app.Member;
import app.Session;

import java.util.LinkedHashMap;
import java.util.Map;

final class Views {

    private Views() {
    }

    static Map<String, Object> member(Member m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("type", m.type().name());
        map.put("name", m.getName());
        map.put("age", m.getAge());
        map.put("visits", m.getVisits());
        map.put("canBook", m.canBook());
        return map;
    }

    static Map<String, Object> session(Session s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("name", s.getName());
        map.put("capacity", s.getCapacity());
        map.put("freePlaces", s.getFreePlaces());
        map.put("memberIds", s.getMemberIds());
        return map;
    }
}