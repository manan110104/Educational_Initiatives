package com.example;

import java.util.*;

/**
 * Adapter Pattern Implementation - Structural Design Pattern
 * Adapts different data formats for the classroom management system
 */

/**
 * Target interface that our system expects
 */
interface ClassroomDataInterface {
    List<String> getClassroomNames();
    Map<String, List<String>> getStudentEnrollments();
    Map<String, List<String>> getAssignmentData();
    void refreshData();
}

/**
 * Legacy system with incompatible interface
 */
class LegacyClassroomSystem {
    private String[] classNames;
    private HashMap<String, String[]> studentMappings;
    private Vector<String> assignmentList;
    
    public LegacyClassroomSystem() {
        // Simulate legacy data
        classNames = new String[]{"Math101", "Physics201", "Chemistry301"};
        studentMappings = new HashMap<>();
        studentMappings.put("Math101", new String[]{"S001", "S002", "S003"});
        studentMappings.put("Physics201", new String[]{"S002", "S004"});
        studentMappings.put("Chemistry301", new String[]{"S001", "S003", "S005"});
        
        assignmentList = new Vector<>();
        assignmentList.add("Math101:Algebra Homework");
        assignmentList.add("Math101:Calculus Quiz");
        assignmentList.add("Physics201:Motion Lab");
        assignmentList.add("Chemistry301:Periodic Table Test");
    }
    
    public String[] getAllClassNames() {
        return classNames;
    }
    
    public HashMap<String, String[]> getStudentMappings() {
        return studentMappings;
    }
    
    public Vector<String> getAssignmentList() {
        return assignmentList;
    }
    
    public void updateData() {
        Logger.logInfo("Legacy system data updated");
    }
}

/**
 * External API with different interface
 */
class ExternalEducationAPI {
    public String getClassroomsAsJson() {
        return "{\"classrooms\":[\"English101\",\"History201\",\"Art301\"]}";
    }
    
    public String getEnrollmentsAsXml() {
        return "<enrollments><class name=\"English101\"><student>S006</student><student>S007</student></class></enrollments>";
    }
    
    public Properties getAssignmentsAsProperties() {
        Properties props = new Properties();
        props.setProperty("English101.assignment1", "Essay Writing");
        props.setProperty("English101.assignment2", "Grammar Test");
        props.setProperty("History201.assignment1", "World War Analysis");
        return props;
    }
    
    public void syncData() {
        Logger.logInfo("External API data synchronized");
    }
}

/**
 * Adapter for Legacy System
 */
class LegacySystemAdapter implements ClassroomDataInterface {
    private LegacyClassroomSystem legacySystem;
    
    public LegacySystemAdapter(LegacyClassroomSystem legacySystem) {
        this.legacySystem = legacySystem;
    }
    
    @Override
    public List<String> getClassroomNames() {
        return Arrays.asList(legacySystem.getAllClassNames());
    }
    
    @Override
    public Map<String, List<String>> getStudentEnrollments() {
        Map<String, List<String>> enrollments = new HashMap<>();
        HashMap<String, String[]> mappings = legacySystem.getStudentMappings();
        
        for (Map.Entry<String, String[]> entry : mappings.entrySet()) {
            enrollments.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        
        return enrollments;
    }
    
    @Override
    public Map<String, List<String>> getAssignmentData() {
        Map<String, List<String>> assignments = new HashMap<>();
        Vector<String> assignmentList = legacySystem.getAssignmentList();
        
        for (String assignment : assignmentList) {
            String[] parts = assignment.split(":");
            if (parts.length == 2) {
                String className = parts[0];
                String assignmentName = parts[1];
                
                assignments.computeIfAbsent(className, k -> new ArrayList<>()).add(assignmentName);
            }
        }
        
        return assignments;
    }
    
    @Override
    public void refreshData() {
        legacySystem.updateData();
    }
}

/**
 * Adapter for External API
 */
class ExternalAPIAdapter implements ClassroomDataInterface {
    private ExternalEducationAPI externalAPI;
    
    public ExternalAPIAdapter(ExternalEducationAPI externalAPI) {
        this.externalAPI = externalAPI;
    }
    
    @Override
    public List<String> getClassroomNames() {
        String json = externalAPI.getClassroomsAsJson();
        // Simple JSON parsing (in real implementation, use proper JSON library)
        List<String> classrooms = new ArrayList<>();
        if (json.contains("English101")) classrooms.add("English101");
        if (json.contains("History201")) classrooms.add("History201");
        if (json.contains("Art301")) classrooms.add("Art301");
        return classrooms;
    }
    
    @Override
    public Map<String, List<String>> getStudentEnrollments() {
        String xml = externalAPI.getEnrollmentsAsXml();
        Map<String, List<String>> enrollments = new HashMap<>();
        
        // Simple XML parsing (in real implementation, use proper XML library)
        if (xml.contains("English101")) {
            List<String> students = new ArrayList<>();
            if (xml.contains("S006")) students.add("S006");
            if (xml.contains("S007")) students.add("S007");
            enrollments.put("English101", students);
        }
        
        return enrollments;
    }
    
    @Override
    public Map<String, List<String>> getAssignmentData() {
        Properties props = externalAPI.getAssignmentsAsProperties();
        Map<String, List<String>> assignments = new HashMap<>();
        
        for (String key : props.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length >= 2) {
                String className = parts[0];
                String assignmentName = props.getProperty(key);
                
                assignments.computeIfAbsent(className, k -> new ArrayList<>()).add(assignmentName);
            }
        }
        
        return assignments;
    }
    
    @Override
    public void refreshData() {
        externalAPI.syncData();
    }
}

/**
 * Client class that uses the adapted interfaces
 */
class UnifiedDataManager {
    private List<ClassroomDataInterface> dataSources;
    
    public UnifiedDataManager() {
        dataSources = new ArrayList<>();
    }
    
    public void addDataSource(ClassroomDataInterface dataSource) {
        dataSources.add(dataSource);
        Logger.logInfo("Added data source: " + dataSource.getClass().getSimpleName());
    }
    
    public List<String> getAllClassrooms() {
        Set<String> allClassrooms = new HashSet<>();
        
        for (ClassroomDataInterface source : dataSources) {
            allClassrooms.addAll(source.getClassroomNames());
        }
        
        return new ArrayList<>(allClassrooms);
    }
    
    public Map<String, Set<String>> getAllStudentEnrollments() {
        Map<String, Set<String>> allEnrollments = new HashMap<>();
        
        for (ClassroomDataInterface source : dataSources) {
            Map<String, List<String>> sourceEnrollments = source.getStudentEnrollments();
            
            for (Map.Entry<String, List<String>> entry : sourceEnrollments.entrySet()) {
                allEnrollments.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                             .addAll(entry.getValue());
            }
        }
        
        return allEnrollments;
    }
    
    public void refreshAllSources() {
        Logger.logInfo("Refreshing all data sources...");
        for (ClassroomDataInterface source : dataSources) {
            source.refreshData();
        }
    }
    
    public void printSummary() {
        System.out.println("\n--- Unified Data Summary ---");
        System.out.println("Total Classrooms: " + getAllClassrooms().size());
        System.out.println("Classrooms: " + getAllClassrooms());
        
        Map<String, Set<String>> enrollments = getAllStudentEnrollments();
        int totalStudents = enrollments.values().stream().mapToInt(Set::size).sum();
        System.out.println("Total Enrollments: " + totalStudents);
    }
}
