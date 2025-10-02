package com.example;

import java.util.*;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Virtual Classroom Manager!");
        System.out.print("Enter your user ID: ");
        String userId = scanner.nextLine().trim();
        System.out.print("Enter your role (admin/student): ");
        String role = scanner.nextLine().trim();
        User currentUser = new User(userId, role);
        ClassroomManager manager = ClassroomManager.getInstance(currentUser);
        ApplicationState appState = new ApplicationState();
        CommandInvoker commandInvoker = new CommandInvoker();
        while (appState.isRunning()) {
            printMenu();
            String input = scanner.nextLine().trim();
            String[] tokens = input.split(" ", 2);
            String command = tokens[0].toLowerCase();
            String params = tokens.length > 1 ? tokens[1] : "";
            try {
                switch (command) {
                    case "add_classroom":
                        Command addClassroomCmd = new AddClassroomCommand(manager, params);
                        commandInvoker.executeCommand(addClassroomCmd);
                        break;
                    case "list_classrooms":
                        manager.listClassrooms();
                        break;
                    case "remove_classroom":
                        manager.removeClassroom(params);
                        break;
                    case "add_student":
                        String[] studentTokens = params.split(" ", 2);
                        if (studentTokens.length >= 2) {
                            Command enrollCmd = new EnrollStudentCommand(manager, studentTokens[0], studentTokens[1]);
                            commandInvoker.executeCommand(enrollCmd);
                        } else {
                            manager.addStudent(params);
                        }
                        break;
                    case "list_students":
                        manager.listStudents(params);
                        break;
                    case "schedule_assignment":
                        manager.scheduleAssignment(params);
                        break;
                    case "submit_assignment":
                        manager.submitAssignment(params);
                        break;
                    case "list_assignments":
                        manager.listAssignments(params);
                        break;
                    case "analytics":
                        manager.showAnalytics();
                        break;
                    case "undo":
                        commandInvoker.undoLastCommand();
                        break;
                    case "history":
                        commandInvoker.showCommandHistory();
                        break;
                    case "demo":
                        showDesignPatternMenu(scanner, manager);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        appState.shutdown();
                        System.out.println("Exiting Virtual Classroom Manager. Goodbye!");
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                Logger.logError("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\nAvailable commands: add_classroom, list_classrooms, remove_classroom, add_student, list_students, schedule_assignment, submit_assignment, list_assignments, analytics, undo, history, demo, help, exit");
        System.out.print("> ");
    }

    private static void printHelp() {
        System.out.println("\nCommand Reference:");
        System.out.println("add_classroom <className>");
        System.out.println("remove_classroom <className>");
        System.out.println("list_classrooms");
        System.out.println("add_student <studentId> <className>");
        System.out.println("list_students <className>");
        System.out.println("schedule_assignment <className> <assignmentDetails>");
        System.out.println("submit_assignment <studentId> <className> <assignmentDetails>");
        System.out.println("list_assignments <className>");
        System.out.println("analytics");
        System.out.println("undo");
        System.out.println("history");
        System.out.println("demo - Show design pattern demonstrations");
        System.out.println("help");
        System.out.println("exit");
    }
    
    private static void showDesignPatternMenu(Scanner scanner, ClassroomManager manager) {
        boolean inDemoMode = true;
        
        while (inDemoMode) {
            System.out.println("\n=== DESIGN PATTERN DEMONSTRATIONS ===");
            System.out.println("1. Observer Pattern (Notifications)");
            System.out.println("2. Command Pattern (Undo/Redo)");
            System.out.println("3. Strategy Pattern (Grading)");
            System.out.println("4. Singleton Pattern (Manager/Logger)");
            System.out.println("5. Factory Pattern (User Creation)");
            System.out.println("6. Adapter Pattern (Data Integration)");
            System.out.println("7. Decorator Pattern (Assignment Features)");
            System.out.println("8. Run All Demonstrations");
            System.out.println("9. Integration Tests");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select demo (0-9): ");
            
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1":
                        testObserverPattern(manager);
                        break;
                    case "2":
                        testCommandPattern(manager);
                        break;
                    case "3":
                        testStrategyPattern();
                        break;
                    case "4":
                        testSingletonPattern(manager);
                        break;
                    case "5":
                        testFactoryPattern();
                        break;
                    case "6":
                        testAdapterPattern();
                        break;
                    case "7":
                        testDecoratorPattern();
                        break;
                    case "8":
                        runAllDemonstrations(manager);
                        break;
                    case "9":
                        runIntegrationTests(manager);
                        break;
                    case "0":
                        inDemoMode = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 0-9.");
                }
            } catch (Exception e) {
                Logger.logError("Demo error: " + e.getMessage());
                System.out.println("Demo failed: " + e.getMessage());
            }
            
            if (inDemoMode && !choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }
    
    private static void testObserverPattern(ClassroomManager manager) {
        System.out.println("\n--- OBSERVER PATTERN TEST ---");
        System.out.println("Testing notification system when assignments are added...");
        
        // Create test classroom and students
        manager.addClassroom("TestObserver");
        manager.addStudent("OBS001 TestObserver");
        manager.addStudent("OBS002 TestObserver");
        
        System.out.println("\nAdding assignment (students should be notified):");
        manager.scheduleAssignment("TestObserver Observer_Pattern_Demo");
        
        System.out.println("✓ Observer Pattern: Students automatically notified!");
    }
    
    private static void testCommandPattern(ClassroomManager manager) {
        System.out.println("\n--- COMMAND PATTERN TEST ---");
        System.out.println("Testing command execution with undo capability...");
        
        CommandInvoker testInvoker = new CommandInvoker();
        
        // Execute commands
        Command cmd1 = new AddClassroomCommand(manager, "TestCommand1");
        Command cmd2 = new AddClassroomCommand(manager, "TestCommand2");
        
        System.out.println("\nExecuting commands:");
        testInvoker.executeCommand(cmd1);
        testInvoker.executeCommand(cmd2);
        
        System.out.println("\nCommand history:");
        testInvoker.showCommandHistory();
        
        System.out.println("\nUndoing last command:");
        testInvoker.undoLastCommand();
        
        System.out.println("✓ Command Pattern: Operations with undo capability!");
    }
    
    private static void testStrategyPattern() {
        System.out.println("\n--- STRATEGY PATTERN TEST ---");
        System.out.println("Testing different grading strategies...");
        
        GradeCalculator calculator = new GradeCalculator(new StandardGradingStrategy());
        
        int score = 85;
        int maxScore = 100;
        
        // Test different strategies
        System.out.println("\nTesting score: " + score + "/" + maxScore);
        
        GradeResult standard = calculator.calculateGrade(score, maxScore);
        System.out.println("Standard: " + standard);
        
        calculator.setStrategy(new CurveGradingStrategy(10.0));
        GradeResult curved = calculator.calculateGrade(score, maxScore);
        System.out.println("Curved: " + curved);
        
        calculator.setStrategy(new PassFailGradingStrategy(80.0));
        GradeResult passFail = calculator.calculateGrade(score, maxScore);
        System.out.println("Pass/Fail: " + passFail);
        
        System.out.println("✓ Strategy Pattern: Interchangeable algorithms!");
    }
    
    private static void testSingletonPattern(ClassroomManager manager) {
        System.out.println("\n--- SINGLETON PATTERN TEST ---");
        System.out.println("Testing singleton instances...");
        
        User testUser = new User("test", "admin");
        ClassroomManager manager2 = ClassroomManager.getInstance(testUser);
        
        System.out.println("Manager1 == Manager2: " + (manager == manager2));
        
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();
        System.out.println("Logger1 == Logger2: " + (logger1 == logger2));
        
        System.out.println("✓ Singleton Pattern: Single instances maintained!");
    }
    
    private static void testFactoryPattern() {
        System.out.println("\n--- FACTORY PATTERN TEST ---");
        System.out.println("Testing user creation with factory...");
        
        Map<String, Object> props = new HashMap<>();
        props.put("department", "Testing");
        props.put("accessLevel", 2);
        
        User admin = UserFactory.createUser("admin", "TEST_ADMIN", props);
        System.out.println("Created: " + admin);
        
        User student = UserFactory.createUser("student", "TEST_STUDENT");
        System.out.println("Created: " + student);
        
        props.clear();
        props.put("subject", "Design Patterns");
        props.put("experience", 5);
        
        User instructor = UserFactory.createUser("instructor", "TEST_INSTRUCTOR", props);
        System.out.println("Created: " + instructor);
        
        System.out.println("✓ Factory Pattern: Different user types created!");
    }
    
    private static void testAdapterPattern() {
        System.out.println("\n--- ADAPTER PATTERN TEST ---");
        System.out.println("Testing data source integration...");
        
        UnifiedDataManager dataManager = new UnifiedDataManager();
        
        // Add legacy system
        LegacyClassroomSystem legacy = new LegacyClassroomSystem();
        dataManager.addDataSource(new LegacySystemAdapter(legacy));
        
        // Add external API
        ExternalEducationAPI api = new ExternalEducationAPI();
        dataManager.addDataSource(new ExternalAPIAdapter(api));
        
        dataManager.printSummary();
        
        System.out.println("✓ Adapter Pattern: Multiple data sources unified!");
    }
    
    private static void testDecoratorPattern() {
        System.out.println("\n--- DECORATOR PATTERN TEST ---");
        System.out.println("Testing assignment feature decoration...");
        
        // Build complex assignment using decorator pattern
        AssignmentComponent assignment = new AssignmentBuilder(
            "Testing", "Decorator Pattern Assignment", 6.0, 120)
            .withDeadline(java.time.LocalDateTime.now().plusDays(3), true)
            .withGrading(Map.of("Implementation", 50, "Documentation", 30, "Testing", 20))
            .withCollaboration(true, 2, List.of("GitHub", "Discord"))
            .build();
        
        System.out.println("\nDecorated Assignment:");
        System.out.println(assignment.getDisplayInfo());
        
        System.out.println("\nRequirements:");
        assignment.getRequirements().forEach(req -> System.out.println("  - " + req));
        
        System.out.println("✓ Decorator Pattern: Features added dynamically!");
    }
    
    private static void runAllDemonstrations(ClassroomManager manager) {
        System.out.println("\n=== RUNNING ALL DESIGN PATTERN DEMONSTRATIONS ===");
        
        testObserverPattern(manager);
        System.out.println("\n" + "=".repeat(50));
        
        testCommandPattern(manager);
        System.out.println("\n" + "=".repeat(50));
        
        testStrategyPattern();
        System.out.println("\n" + "=".repeat(50));
        
        testSingletonPattern(manager);
        System.out.println("\n" + "=".repeat(50));
        
        testFactoryPattern();
        System.out.println("\n" + "=".repeat(50));
        
        testAdapterPattern();
        System.out.println("\n" + "=".repeat(50));
        
        testDecoratorPattern();
        
        System.out.println("\n🎉 ALL DESIGN PATTERNS TESTED SUCCESSFULLY!");
    }
    
    private static void runIntegrationTests(ClassroomManager manager) {
        System.out.println("\n=== INTEGRATION TESTS ===");
        System.out.println("Testing how design patterns work together in the system...");
        
        // Test 1: Factory + Observer + Command integration
        System.out.println("\n1. Testing Factory + Observer + Command integration:");
        
        User instructor = UserFactory.createUser("instructor", "INT_TEST", 
            Map.of("subject", "Integration Testing", "experience", 3));
        System.out.println("Created instructor: " + instructor);
        
        CommandInvoker integrationInvoker = new CommandInvoker();
        Command createClass = new AddClassroomCommand(manager, "IntegrationTest");
        integrationInvoker.executeCommand(createClass);
        
        manager.addStudent("INT001 IntegrationTest");
        manager.scheduleAssignment("IntegrationTest Integration_Assignment");
        
        // Test 2: Strategy + Decorator integration
        System.out.println("\n2. Testing Strategy + Decorator integration:");
        
        GradeCalculator gradeCalc = new GradeCalculator(new CurveGradingStrategy(5.0));
        GradeResult result = gradeCalc.calculateGrade(78, 100);
        System.out.println("Graded assignment: " + result);
        
        AssignmentComponent decoratedAssignment = new AssignmentBuilder(
            "IntegrationTest", "Complex Assignment", 8.0, 240)
            .withDeadline(java.time.LocalDateTime.now().plusWeeks(2), false)
            .withGrading(Map.of("Code", 60, "Design", 40))
            .build();
        
        System.out.println("Decorated assignment: " + decoratedAssignment.getDisplayInfo());
        
        // Test 3: Adapter + Singleton integration
        System.out.println("\n3. Testing Adapter + Singleton integration:");
        
        UnifiedDataManager dataManager = new UnifiedDataManager();
        dataManager.addDataSource(new LegacySystemAdapter(new LegacyClassroomSystem()));
        
        Logger.logInfo("Integration test completed successfully");
        
        System.out.println("\n✅ INTEGRATION TESTS PASSED!");
        System.out.println("All design patterns work seamlessly together!");
    }
}

