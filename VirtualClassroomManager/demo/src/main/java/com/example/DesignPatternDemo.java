package com.example;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Comprehensive Design Pattern Demonstration
 * Showcases all 6 required design patterns with practical examples
 */
public class DesignPatternDemo {
    
    public static void main(String[] args) {
        System.out.println("=== DESIGN PATTERN DEMONSTRATIONS ===\n");
        
        // BEHAVIORAL PATTERNS
        demonstrateObserverPattern();
        demonstrateCommandPattern();
        demonstrateStrategyPattern();
        
        // CREATIONAL PATTERNS
        demonstrateSingletonPattern();
        demonstrateFactoryPattern();
        
        // STRUCTURAL PATTERNS
        demonstrateAdapterPattern();
        demonstrateDecoratorPattern();
    }
    
    /**
     * BEHAVIORAL PATTERN 1: Observer Pattern
     * Students get notified when assignments are added
     */
    private static void demonstrateObserverPattern() {
        System.out.println("1. OBSERVER PATTERN DEMONSTRATION");
        System.out.println("=================================");
        
        // Create classroom and students
        Classroom mathClass = new Classroom("Mathematics");
        Student alice = new Student("Alice");
        Student bob = new Student("Bob");
        
        // Enroll students (they become observers)
        mathClass.enrollStudent(alice);
        mathClass.enrollStudent(bob);
        
        // Add assignment (observers get notified)
        Assignment calculus = new Assignment("Mathematics", "Calculus Problem Set");
        mathClass.addAssignment(calculus);
        
        System.out.println("✓ Observer pattern: Students automatically notified of new assignments\n");
    }
    
    /**
     * BEHAVIORAL PATTERN 2: Command Pattern
     * Encapsulate operations as objects with undo capability
     */
    private static void demonstrateCommandPattern() {
        System.out.println("2. COMMAND PATTERN DEMONSTRATION");
        System.out.println("================================");
        
        User admin = new User("admin", "admin");
        ClassroomManager manager = ClassroomManager.getInstance(admin);
        CommandInvoker invoker = new CommandInvoker();
        
        // Execute commands
        Command createClass = new AddClassroomCommand(manager, "Physics101");
        Command enrollStudent = new EnrollStudentCommand(manager, "John", "Physics101");
        
        invoker.executeCommand(createClass);
        invoker.executeCommand(enrollStudent);
        
        System.out.println("Command history:");
        invoker.showCommandHistory();
        
        // Undo last command
        System.out.println("\nUndoing last command...");
        invoker.undoLastCommand();
        
        System.out.println("✓ Command pattern: Operations encapsulated with undo capability\n");
    }
    
    /**
     * BEHAVIORAL PATTERN 3: Strategy Pattern
     * Different grading algorithms
     */
    private static void demonstrateStrategyPattern() {
        System.out.println("3. STRATEGY PATTERN DEMONSTRATION");
        System.out.println("=================================");
        
        GradeCalculator calculator = new GradeCalculator(new StandardGradingStrategy());
        
        // Standard grading
        GradeResult result1 = calculator.calculateGrade(85, 100);
        System.out.println("Standard: " + result1);
        
        // Switch to curve grading
        calculator.setStrategy(new CurveGradingStrategy(10.0));
        GradeResult result2 = calculator.calculateGrade(85, 100);
        System.out.println("Curved: " + result2);
        
        // Switch to pass/fail
        calculator.setStrategy(new PassFailGradingStrategy(70.0));
        GradeResult result3 = calculator.calculateGrade(85, 100);
        System.out.println("Pass/Fail: " + result3);
        
        System.out.println("✓ Strategy pattern: Different grading algorithms interchangeable\n");
    }
    
    /**
     * CREATIONAL PATTERN 1: Singleton Pattern
     * Single instance of ClassroomManager and Logger
     */
    private static void demonstrateSingletonPattern() {
        System.out.println("4. SINGLETON PATTERN DEMONSTRATION");
        System.out.println("==================================");
        
        User user1 = new User("user1", "admin");
        User user2 = new User("user2", "student");
        
        ClassroomManager manager1 = ClassroomManager.getInstance(user1);
        ClassroomManager manager2 = ClassroomManager.getInstance(user2);
        
        System.out.println("Manager1 == Manager2: " + (manager1 == manager2));
        
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();
        
        System.out.println("Logger1 == Logger2: " + (logger1 == logger2));
        System.out.println("✓ Singleton pattern: Single instances maintained\n");
    }
    
    /**
     * CREATIONAL PATTERN 2: Factory Pattern
     * Create different types of users
     */
    private static void demonstrateFactoryPattern() {
        System.out.println("5. FACTORY PATTERN DEMONSTRATION");
        System.out.println("================================");
        
        // Create different user types using factory
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put("department", "Computer Science");
        adminProps.put("accessLevel", 3);
        
        User admin = UserFactory.createUser("admin", "admin001", adminProps);
        System.out.println("Created: " + admin);
        
        Map<String, Object> studentProps = new HashMap<>();
        studentProps.put("grade", "Senior");
        studentProps.put("major", "Computer Science");
        
        User student = UserFactory.createUser("student", "student001", studentProps);
        System.out.println("Created: " + student);
        
        Map<String, Object> instructorProps = new HashMap<>();
        instructorProps.put("subject", "Mathematics");
        instructorProps.put("experience", 8);
        
        User instructor = UserFactory.createUser("instructor", "instructor001", instructorProps);
        System.out.println("Created: " + instructor);
        
        User guest = UserFactory.createUser("guest", "guest001");
        System.out.println("Created: " + guest);
        
        System.out.println("✓ Factory pattern: Different user types created based on role\n");
    }
    
    /**
     * STRUCTURAL PATTERN 1: Adapter Pattern
     * Integrate different data sources
     */
    private static void demonstrateAdapterPattern() {
        System.out.println("6. ADAPTER PATTERN DEMONSTRATION");
        System.out.println("================================");
        
        UnifiedDataManager dataManager = new UnifiedDataManager();
        
        // Add legacy system through adapter
        LegacyClassroomSystem legacySystem = new LegacyClassroomSystem();
        ClassroomDataInterface legacyAdapter = new LegacySystemAdapter(legacySystem);
        dataManager.addDataSource(legacyAdapter);
        
        // Add external API through adapter
        ExternalEducationAPI externalAPI = new ExternalEducationAPI();
        ClassroomDataInterface apiAdapter = new ExternalAPIAdapter(externalAPI);
        dataManager.addDataSource(apiAdapter);
        
        // Use unified interface
        dataManager.printSummary();
        
        System.out.println("✓ Adapter pattern: Different data sources unified under common interface\n");
    }
    
    /**
     * STRUCTURAL PATTERN 2: Decorator Pattern
     * Add features to assignments dynamically
     */
    private static void demonstrateDecoratorPattern() {
        System.out.println("7. DECORATOR PATTERN DEMONSTRATION");
        System.out.println("==================================");
        
        // Create basic assignment
        AssignmentComponent basicAssignment = new BasicAssignment(
            "Computer Science", "Data Structures Project", 7.5, 180);
        
        System.out.println("Basic: " + basicAssignment.getDisplayInfo());
        
        // Add deadline
        AssignmentComponent withDeadline = new DeadlineDecorator(
            basicAssignment, LocalDateTime.now().plusDays(7), true);
        
        System.out.println("With Deadline: " + withDeadline.getDisplayInfo());
        
        // Add grading criteria
        Map<String, Integer> criteria = new HashMap<>();
        criteria.put("Code Quality", 40);
        criteria.put("Documentation", 20);
        criteria.put("Testing", 25);
        criteria.put("Performance", 15);
        
        AssignmentComponent withGrading = new GradingDecorator(withDeadline, criteria);
        System.out.println("With Grading: " + withGrading.getDisplayInfo());
        
        // Add collaboration features
        AssignmentComponent withCollaboration = new CollaborationDecorator(
            withGrading, true, 3, Arrays.asList("GitHub", "Slack", "Zoom"));
        
        System.out.println("With Collaboration: " + withCollaboration.getDisplayInfo());
        
        // Add multimedia requirements
        AssignmentComponent fullAssignment = new MultimediaDecorator(
            withCollaboration, 
            Arrays.asList("Video Demo", "Screenshots"), 
            50, 
            Arrays.asList("MP4", "PNG", "JPG"));
        
        System.out.println("Full Assignment: " + fullAssignment.getDisplayInfo());
        
        System.out.println("\nRequirements:");
        for (String req : fullAssignment.getRequirements()) {
            System.out.println("  - " + req);
        }
        
        System.out.println("✓ Decorator pattern: Assignment features added dynamically\n");
    }
}
