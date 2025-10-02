package com.example;

import java.util.HashSet;
import java.util.Set;

public class Student implements NotificationObserver {
    private final String studentId;
    private Set<String> enrolledClassrooms;

    public Student(String studentId) {
        this.studentId = studentId;
        this.enrolledClassrooms = new HashSet<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public Set<String> getEnrolledClassrooms() {
        return enrolledClassrooms;
    }

    public void enrollInClassroom(String className) {
        enrolledClassrooms.add(className);
    }

    @Override
    public void notify(String message) {
        System.out.println("[Notification for " + studentId + "]: " + message);
    }
}
