package com.example;

public class Submission {
    private final String studentId;
    private final String className;
    private final String assignmentDetails;
    private final long timestamp;

    public Submission(String studentId, String className, String assignmentDetails) {
        this.studentId = studentId;
        this.className = className;
        this.assignmentDetails = assignmentDetails;
        this.timestamp = System.currentTimeMillis();
    }

    public String getStudentId() {
        return studentId;
    }

    public String getClassName() {
        return className;
    }

    public String getAssignmentDetails() {
        return assignmentDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


