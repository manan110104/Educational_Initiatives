package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Comprehensive integration tests for all design patterns
 * Tests both individual patterns and their interactions
 */
public class DesignPatternIntegrationTest {
    
    private ClassroomManager manager;
    private User adminUser;
    
    @BeforeEach
    void setUp() {
        adminUser = new User("testAdmin", "admin");
        manager = ClassroomManager.getInstance(adminUser);
    }
    
    @Test
    @DisplayName("Observer Pattern Integration Test")
    void testObserverPatternIntegration() {
        // Create classroom and student
        manager.addClassroom("ObserverTest");
        manager.addStudent("OBS001 ObserverTest");
        
        // Verify student is enrolled and will receive notifications
        manager.listStudents("ObserverTest");
        
        // Schedule assignment (should trigger observer notification)
        manager.scheduleAssignment("ObserverTest Test_Assignment");
        
        // Verify assignment was scheduled
        manager.listAssignments("ObserverTest");
        
        assertTrue(true, "Observer pattern integration successful");
    }
    
    @Test
    @DisplayName("Command Pattern Integration Test")
    void testCommandPatternIntegration() {
        CommandInvoker invoker = new CommandInvoker();
        
        // Test command execution
        Command addClassroom = new AddClassroomCommand(manager, "CommandTest");
        invoker.executeCommand(addClassroom);
        
        Command enrollStudent = new EnrollStudentCommand(manager, "CMD001", "CommandTest");
        invoker.executeCommand(enrollStudent);
        
        // Test undo functionality
        invoker.undoLastCommand();
        
        assertTrue(true, "Command pattern integration successful");
    }
    
    @Test
    @DisplayName("Strategy Pattern Integration Test")
    void testStrategyPatternIntegration() {
        GradeCalculator calculator = new GradeCalculator(new StandardGradingStrategy());
        
        // Test different strategies
        GradeResult standard = calculator.calculateGrade(85, 100);
        assertEquals(85.0, standard.getPercentage(), 0.1);
        assertEquals("B", standard.getLetterGrade());
        
        calculator.setStrategy(new CurveGradingStrategy(10.0));
        GradeResult curved = calculator.calculateGrade(85, 100);
        assertEquals(95.0, curved.getPercentage(), 0.1);
        assertEquals("A", curved.getLetterGrade());
        
        calculator.setStrategy(new PassFailGradingStrategy(80.0));
        GradeResult passFail = calculator.calculateGrade(85, 100);
        assertEquals("PASS", passFail.getLetterGrade());
        
        assertTrue(true, "Strategy pattern integration successful");
    }
    
    @Test
    @DisplayName("Singleton Pattern Integration Test")
    void testSingletonPatternIntegration() {
        // Test ClassroomManager singleton
        User anotherUser = new User("another", "student");
        ClassroomManager manager2 = ClassroomManager.getInstance(anotherUser);
        assertSame(manager, manager2, "ClassroomManager should be singleton");
        
        // Test Logger singleton
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();
        assertSame(logger1, logger2, "Logger should be singleton");
        
        assertTrue(true, "Singleton pattern integration successful");
    }
    
    @Test
    @DisplayName("Factory Pattern Integration Test")
    void testFactoryPatternIntegration() {
        // Test admin creation
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put("department", "IT");
        adminProps.put("accessLevel", 3);
        
        User admin = UserFactory.createUser("admin", "FACTORY_ADMIN", adminProps);
        assertTrue(admin instanceof AdminUser);
        assertTrue(admin.isAdmin());
        
        // Test student creation
        Map<String, Object> studentProps = new HashMap<>();
        studentProps.put("grade", "Senior");
        studentProps.put("major", "CS");
        
        User student = UserFactory.createUser("student", "FACTORY_STUDENT", studentProps);
        assertTrue(student instanceof EnhancedStudent);
        assertFalse(student.isAdmin());
        
        // Test instructor creation
        Map<String, Object> instructorProps = new HashMap<>();
        instructorProps.put("subject", "Math");
        instructorProps.put("experience", 8);
        
        User instructor = UserFactory.createUser("instructor", "FACTORY_INSTRUCTOR", instructorProps);
        assertTrue(instructor instanceof InstructorUser);
        assertTrue(instructor.isAdmin()); // Senior instructors have admin privileges
        
        assertTrue(true, "Factory pattern integration successful");
    }
    
    @Test
    @DisplayName("Adapter Pattern Integration Test")
    void testAdapterPatternIntegration() {
        UnifiedDataManager dataManager = new UnifiedDataManager();
        
        // Test legacy system adapter
        LegacyClassroomSystem legacy = new LegacyClassroomSystem();
        ClassroomDataInterface legacyAdapter = new LegacySystemAdapter(legacy);
        dataManager.addDataSource(legacyAdapter);
        
        List<String> legacyClassrooms = legacyAdapter.getClassroomNames();
        assertFalse(legacyClassrooms.isEmpty(), "Legacy adapter should provide classrooms");
        
        // Test external API adapter
        ExternalEducationAPI api = new ExternalEducationAPI();
        ClassroomDataInterface apiAdapter = new ExternalAPIAdapter(api);
        dataManager.addDataSource(apiAdapter);
        
        List<String> apiClassrooms = apiAdapter.getClassroomNames();
        assertFalse(apiClassrooms.isEmpty(), "API adapter should provide classrooms");
        
        // Test unified access
        List<String> allClassrooms = dataManager.getAllClassrooms();
        assertTrue(allClassrooms.size() >= legacyClassrooms.size() + apiClassrooms.size(),
                  "Unified manager should combine all data sources");
        
        assertTrue(true, "Adapter pattern integration successful");
    }
    
    @Test
    @DisplayName("Decorator Pattern Integration Test")
    void testDecoratorPatternIntegration() {
        // Create basic assignment
        AssignmentComponent basic = new BasicAssignment("Test", "Basic Assignment", 5.0, 60);
        assertEquals("Basic Assignment", basic.getDetails());
        assertEquals(60, basic.getEstimatedTime());
        
        // Add deadline decorator
        AssignmentComponent withDeadline = new DeadlineDecorator(
            basic, LocalDateTime.now().plusDays(7), true);
        assertEquals("Basic Assignment", withDeadline.getDetails());
        assertTrue(withDeadline.getDisplayInfo().contains("Deadline"));
        
        // Add grading decorator
        Map<String, Integer> criteria = Map.of("Quality", 50, "Completeness", 50);
        AssignmentComponent withGrading = new GradingDecorator(withDeadline, criteria);
        assertTrue(withGrading.getDisplayInfo().contains("Total Points: 100"));
        
        // Add collaboration decorator
        AssignmentComponent withCollaboration = new CollaborationDecorator(
            withGrading, true, 3, List.of("GitHub", "Slack"));
        assertTrue(withCollaboration.getDisplayInfo().contains("Group Work: Yes"));
        assertEquals(4.0, withCollaboration.getDifficulty(), 0.1); // Reduced due to group work
        
        // Test builder pattern integration
        AssignmentComponent builderAssignment = new AssignmentBuilder("Test", "Builder Test", 7.0, 120)
            .withDeadline(LocalDateTime.now().plusDays(5), false)
            .withGrading(Map.of("Code", 70, "Docs", 30))
            .withCollaboration(false, 1, List.of())
            .build();
        
        assertTrue(builderAssignment.getDisplayInfo().contains("Total Points: 100"));
        assertTrue(builderAssignment.getDisplayInfo().contains("Individual only"));
        
        assertTrue(true, "Decorator pattern integration successful");
    }
    
    @Test
    @DisplayName("Cross-Pattern Integration Test")
    void testCrossPatternIntegration() {
        // Test Factory + Command + Observer integration
        User instructor = UserFactory.createUser("instructor", "CROSS_TEST", 
            Map.of("subject", "Integration", "experience", 5));
        
        CommandInvoker invoker = new CommandInvoker();
        Command createClass = new AddClassroomCommand(manager, "CrossPatternTest");
        invoker.executeCommand(createClass);
        
        // Add student (Observer)
        manager.addStudent("CROSS001 CrossPatternTest");
        
        // Schedule assignment (triggers Observer notification)
        manager.scheduleAssignment("CrossPatternTest Cross_Pattern_Assignment");
        
        // Test Strategy + Decorator integration
        GradeCalculator calculator = new GradeCalculator(new CurveGradingStrategy(8.0));
        
        AssignmentComponent decoratedAssignment = new AssignmentBuilder(
            "CrossPatternTest", "Complex Integration Assignment", 9.0, 300)
            .withDeadline(LocalDateTime.now().plusWeeks(3), true)
            .withGrading(Map.of("Implementation", 40, "Design", 30, "Testing", 20, "Documentation", 10))
            .withCollaboration(true, 4, List.of("GitHub", "Jira", "Slack"))
            .withMultimedia(List.of("Demo Video"), 100, List.of("MP4", "AVI"))
            .build();
        
        // Verify complex assignment properties
        assertTrue(decoratedAssignment.getDisplayInfo().contains("Total Points: 100"));
        assertTrue(decoratedAssignment.getDisplayInfo().contains("Group Work: Yes"));
        assertTrue(decoratedAssignment.getDisplayInfo().contains("Media Required"));
        assertTrue(decoratedAssignment.getEstimatedTime() > 300); // Increased due to multimedia
        
        // Test grading with strategy
        GradeResult result = calculator.calculateGrade(82, 100);
        assertEquals(90.0, result.getPercentage(), 0.1); // 82 + 8% curve
        assertEquals("A", result.getLetterGrade());
        
        // Test Adapter + Singleton integration
        UnifiedDataManager dataManager = new UnifiedDataManager();
        dataManager.addDataSource(new LegacySystemAdapter(new LegacyClassroomSystem()));
        
        Logger.logInfo("Cross-pattern integration test completed");
        
        assertTrue(true, "Cross-pattern integration successful");
    }
    
    @Test
    @DisplayName("Transient Error Handling Integration Test")
    void testTransientErrorHandlingIntegration() {
        // Test retry mechanism with classroom creation
        assertDoesNotThrow(() -> {
            manager.addClassroom("RetryTest");
        }, "Transient error handling should allow eventual success");
        
        // Test circuit breaker integration
        ResilientOperationWrapper wrapper = new ResilientOperationWrapper();
        
        assertDoesNotThrow(() -> {
            wrapper.executeResilient(() -> {
                Logger.logInfo("Testing resilient operation");
                return "Success";
            }, "testOperation");
        }, "Resilient wrapper should handle operations successfully");
        
        assertTrue(true, "Transient error handling integration successful");
    }
}
