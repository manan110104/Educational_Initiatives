package com.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory Pattern Implementation - Creational Design Pattern
 * Creates different types of users based on roles and requirements
 */
public abstract class UserFactory {
    private static final Map<String, UserFactory> factories = new HashMap<>();
    
    static {
        factories.put("admin", new AdminUserFactory());
        factories.put("student", new StudentUserFactory());
        factories.put("instructor", new InstructorUserFactory());
        factories.put("guest", new GuestUserFactory());
    }
    
    public static User createUser(String role, String userId, Map<String, Object> properties) {
        UserFactory factory = factories.get(role.toLowerCase());
        if (factory == null) {
            Logger.logError("Unknown user role: " + role);
            throw new IllegalArgumentException("Unknown user role: " + role);
        }
        
        User user = factory.createUserInstance(userId, properties);
        Logger.logInfo("Created user: " + userId + " with role: " + role);
        return user;
    }
    
    public static User createUser(String role, String userId) {
        return createUser(role, userId, new HashMap<>());
    }
    
    protected abstract User createUserInstance(String userId, Map<String, Object> properties);
}

/**
 * Factory for creating admin users
 */
class AdminUserFactory extends UserFactory {
    @Override
    protected User createUserInstance(String userId, Map<String, Object> properties) {
        AdminUser admin = new AdminUser(userId);
        
        // Set admin-specific properties
        if (properties.containsKey("department")) {
            admin.setDepartment((String) properties.get("department"));
        }
        if (properties.containsKey("accessLevel")) {
            admin.setAccessLevel((Integer) properties.get("accessLevel"));
        }
        
        return admin;
    }
}

/**
 * Factory for creating student users
 */
class StudentUserFactory extends UserFactory {
    @Override
    protected User createUserInstance(String userId, Map<String, Object> properties) {
        EnhancedStudent student = new EnhancedStudent(userId);
        
        // Set student-specific properties
        if (properties.containsKey("grade")) {
            student.setGrade((String) properties.get("grade"));
        }
        if (properties.containsKey("major")) {
            student.setMajor((String) properties.get("major"));
        }
        
        return student;
    }
}

/**
 * Factory for creating instructor users
 */
class InstructorUserFactory extends UserFactory {
    @Override
    protected User createUserInstance(String userId, Map<String, Object> properties) {
        InstructorUser instructor = new InstructorUser(userId);
        
        // Set instructor-specific properties
        if (properties.containsKey("subject")) {
            instructor.setSubject((String) properties.get("subject"));
        }
        if (properties.containsKey("experience")) {
            instructor.setExperience((Integer) properties.get("experience"));
        }
        
        return instructor;
    }
}

/**
 * Factory for creating guest users
 */
class GuestUserFactory extends UserFactory {
    @Override
    protected User createUserInstance(String userId, Map<String, Object> properties) {
        GuestUser guest = new GuestUser(userId);
        
        // Set guest-specific properties
        if (properties.containsKey("sessionTimeout")) {
            guest.setSessionTimeout((Long) properties.get("sessionTimeout"));
        }
        
        return guest;
    }
}

/**
 * Enhanced Admin User class
 */
class AdminUser extends User {
    private String department;
    private int accessLevel = 1;
    
    public AdminUser(String userId) {
        super(userId, "admin");
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    public String getDepartment() { return department; }
    public int getAccessLevel() { return accessLevel; }
    
    @Override
    public String toString() {
        return String.format("Admin[%s, Dept: %s, Level: %d]", getUserId(), department, accessLevel);
    }
}

/**
 * Enhanced Student User class
 */
class EnhancedStudent extends User {
    private String grade;
    private String major;
    
    public EnhancedStudent(String userId) {
        super(userId, "student");
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public String getGrade() { return grade; }
    public String getMajor() { return major; }
    
    @Override
    public String toString() {
        return String.format("Student[%s, Grade: %s, Major: %s]", getUserId(), grade, major);
    }
}

/**
 * Instructor User class
 */
class InstructorUser extends User {
    private String subject;
    private int experience;
    
    public InstructorUser(String userId) {
        super(userId, "instructor");
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public void setExperience(int experience) {
        this.experience = experience;
    }
    
    public String getSubject() { return subject; }
    public int getExperience() { return experience; }
    
    @Override
    public boolean isAdmin() {
        return experience > 5; // Senior instructors have admin privileges
    }
    
    @Override
    public String toString() {
        return String.format("Instructor[%s, Subject: %s, Experience: %d years]", getUserId(), subject, experience);
    }
}

/**
 * Guest User class
 */
class GuestUser extends User {
    private long sessionTimeout = 3600000; // 1 hour default
    
    public GuestUser(String userId) {
        super(userId, "guest");
    }
    
    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    
    public long getSessionTimeout() { return sessionTimeout; }
    
    @Override
    public boolean isAdmin() {
        return false; // Guests never have admin privileges
    }
    
    @Override
    public String toString() {
        return String.format("Guest[%s, Timeout: %d ms]", getUserId(), sessionTimeout);
    }
}
