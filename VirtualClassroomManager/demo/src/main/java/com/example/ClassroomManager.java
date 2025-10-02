package com.example;

import java.util.*;

public class ClassroomManager {
    private static ClassroomManager instance;
    private Map<String, Classroom> classrooms;
    private Map<String, Student> students;
    private List<Assignment> assignments;
    private List<Submission> submissions;
    private User currentUser;
    private ResilientOperationWrapper resilientWrapper;

    private ClassroomManager(User user) {
        classrooms = new HashMap<>();
        students = new HashMap<>();
        assignments = new ArrayList<>();
        submissions = new ArrayList<>();
        currentUser = user;
        resilientWrapper = new ResilientOperationWrapper();
    }

    public static synchronized ClassroomManager getInstance(User user) {
        if (instance == null) {
            instance = new ClassroomManager(user);
        }
        return instance;
    }

    public void addClassroom(String className) {
        resilientWrapper.executeResilient(() -> {
            if (!currentUser.isAdmin()) {
                Logger.logError("Only admin can add classrooms.");
                return;
            }
            if (className == null || className.isEmpty()) {
                Logger.logError("Class name cannot be empty.");
                return;
            }
            if (classrooms.containsKey(className)) {
                Logger.logError("Classroom '" + className + "' already exists.");
                return;
            }
            
            // Simulate potential transient failure
            if (Math.random() < 0.1) { // 10% chance of transient failure
                throw new TransientException("Temporary database connection issue");
            }
            
            classrooms.put(className, new Classroom(className));
            Logger.logInfo("Classroom '" + className + "' has been created.");
            System.out.println("Classroom " + className + " has been created.");
        }, "addClassroom");
    }

    public void listClassrooms() {
        if (classrooms.isEmpty()) {
            System.out.println("No classrooms available.");
            return;
        }
        System.out.println("Classrooms:");
        for (String name : classrooms.keySet()) {
            System.out.println("- " + name);
        }
    }

    public void removeClassroom(String className) {
        if (!currentUser.isAdmin()) {
            Logger.logError("Only admin can remove classrooms.");
            return;
        }
        if (!classrooms.containsKey(className)) {
            Logger.logError("Classroom '" + className + "' does not exist.");
            return;
        }
        classrooms.remove(className);
        Logger.logInfo("Classroom '" + className + "' has been removed.");
        System.out.println("Classroom " + className + " has been removed.");
    }

    public void addStudent(String params) {
        String[] tokens = params.split(" ", 2);
        if (tokens.length < 2) {
            Logger.logError("Usage: add_student <studentId> <className>");
            return;
        }
        String studentId = tokens[0];
        String className = tokens[1];
        if (!classrooms.containsKey(className)) {
            Logger.logError("Classroom '" + className + "' does not exist.");
            return;
        }
        Student student = students.getOrDefault(studentId, new Student(studentId));
        students.put(studentId, student);
        classrooms.get(className).enrollStudent(student);
        Logger.logInfo("Student '" + studentId + "' has been enrolled in '" + className + "'.");
        System.out.println("Student " + studentId + " has been enrolled in " + className + ".");
    }

    public void listStudents(String className) {
        if (!classrooms.containsKey(className)) {
            Logger.logError("Classroom '" + className + "' does not exist.");
            return;
        }
        classrooms.get(className).listStudents();
    }

    public void scheduleAssignment(String params) {
        String[] tokens = params.split(" ", 2);
        if (tokens.length < 2) {
            Logger.logError("Usage: schedule_assignment <className> <assignmentDetails>");
            return;
        }
        String className = tokens[0];
        String details = tokens[1];
        if (!classrooms.containsKey(className)) {
            Logger.logError("Classroom '" + className + "' does not exist.");
            return;
        }
        Assignment assignment = new Assignment(className, details);
        assignments.add(assignment);
        classrooms.get(className).addAssignment(assignment);
        Logger.logInfo("Assignment for '" + className + "' has been scheduled.");
        System.out.println("Assignment for " + className + " has been scheduled.");
    }

    public void submitAssignment(String params) {
        String[] tokens = params.split(" ", 3);
        if (tokens.length < 3) {
            Logger.logError("Usage: submit_assignment <studentId> <className> <assignmentDetails>");
            return;
        }
        String studentId = tokens[0];
        String className = tokens[1];
        String details = tokens[2];
        if (!classrooms.containsKey(className)) {
            Logger.logError("Classroom '" + className + "' does not exist.");
            return;
        }
        if (!students.containsKey(studentId)) {
            Logger.logError("Student '" + studentId + "' does not exist.");
            return;
        }
        Assignment assignment = classrooms.get(className).findAssignment(details);
        if (assignment == null) {
            Logger.logError("Assignment '" + details + "' does not exist in '" + className + "'.");
            return;
        }
        Submission submission = new Submission(studentId, className, details);
        submissions.add(submission);
        Logger.logInfo("Assignment submitted by Student '" + studentId + "' in '" + className + "'.");
        System.out.println("Assignment submitted by Student " + studentId + " in " + className + ".");
    }

    public void listAssignments(String className) {
        if (!classrooms.containsKey(className)) {
            Logger.logError("Classroom '" + className + "' does not exist.");
            return;
        }
        classrooms.get(className).listAssignments();
    }

    public void showAnalytics() {
        System.out.println("\n--- Analytics ---");
        System.out.println("Total Classrooms: " + classrooms.size());
        System.out.println("Total Students: " + students.size());
        System.out.println("Total Assignments: " + assignments.size());
        System.out.println("Total Submissions: " + submissions.size());
    }
}


