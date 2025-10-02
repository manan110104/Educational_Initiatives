package com.example;

import java.util.*;

public class Classroom {
    private final String className;
    private Set<Student> students;
    private List<Assignment> assignments;

    public Classroom(String className) {
        this.className = className;
        this.students = new HashSet<>();
        this.assignments = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public void enrollStudent(Student student) {
        students.add(student);
        student.enrollInClassroom(className);
    }

    public void listStudents() {
        if (students.isEmpty()) {
            System.out.println("No students enrolled in " + className + ".");
            return;
        }
        System.out.println("Students in " + className + ":");
        for (Student s : students) {
            System.out.println("- " + s.getStudentId());
        }
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        for (Student s : students) {
            s.notify("New assignment scheduled in " + className + ": " + assignment.getDetails());
        }
    }

    public void listAssignments() {
        if (assignments.isEmpty()) {
            System.out.println("No assignments scheduled for " + className + ".");
            return;
        }
        System.out.println("Assignments for " + className + ":");
        for (Assignment a : assignments) {
            System.out.println("- " + a.getDetails());
        }
    }

    public Assignment findAssignment(String details) {
        for (Assignment a : assignments) {
            if (a.getDetails().equals(details)) {
                return a;
            }
        }
        return null;
    }
}

