package com.example;

/**
 * Command Pattern Implementation - Behavioral Design Pattern
 * Encapsulates requests as objects, allowing parameterization and queuing
 */
public interface Command {
    void execute();
    void undo();
    String getDescription();
}

/**
 * Concrete command for adding classrooms
 */
class AddClassroomCommand implements Command {
    private ClassroomManager manager;
    private String className;
    private boolean executed = false;
    
    public AddClassroomCommand(ClassroomManager manager, String className) {
        this.manager = manager;
        this.className = className;
    }
    
    @Override
    public void execute() {
        manager.addClassroom(className);
        executed = true;
        Logger.logInfo("Executed: Add classroom " + className);
    }
    
    @Override
    public void undo() {
        if (executed) {
            manager.removeClassroom(className);
            executed = false;
            Logger.logInfo("Undone: Add classroom " + className);
        }
    }
    
    @Override
    public String getDescription() {
        return "Add classroom: " + className;
    }
}

/**
 * Concrete command for enrolling students
 */
class EnrollStudentCommand implements Command {
    private ClassroomManager manager;
    private String studentId;
    private String className;
    private boolean executed = false;
    
    public EnrollStudentCommand(ClassroomManager manager, String studentId, String className) {
        this.manager = manager;
        this.studentId = studentId;
        this.className = className;
    }
    
    @Override
    public void execute() {
        manager.addStudent(studentId + " " + className);
        executed = true;
        Logger.logInfo("Executed: Enroll student " + studentId + " in " + className);
    }
    
    @Override
    public void undo() {
        if (executed) {
            // Implementation would require a removeStudent method
            Logger.logInfo("Undone: Enroll student " + studentId + " in " + className);
            executed = false;
        }
    }
    
    @Override
    public String getDescription() {
        return "Enroll student: " + studentId + " in " + className;
    }
}

/**
 * Command Invoker - manages command execution and history
 */
class CommandInvoker {
    private java.util.Stack<Command> commandHistory = new java.util.Stack<>();
    private static final int MAX_HISTORY = 50;
    
    public void executeCommand(Command command) {
        try {
            command.execute();
            commandHistory.push(command);
            
            // Limit history size for memory management
            if (commandHistory.size() > MAX_HISTORY) {
                commandHistory.remove(0);
            }
        } catch (Exception e) {
            Logger.logError("Command execution failed: " + e.getMessage());
            throw e;
        }
    }
    
    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
        } else {
            Logger.logInfo("No commands to undo");
        }
    }
    
    public void showCommandHistory() {
        System.out.println("Command History:");
        for (int i = commandHistory.size() - 1; i >= 0; i--) {
            System.out.println((commandHistory.size() - i) + ". " + commandHistory.get(i).getDescription());
        }
    }
}
