package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Decorator Pattern Implementation - Structural Design Pattern
 * Adds additional features to assignments dynamically
 */

/**
 * Base component interface for assignments
 */
interface AssignmentComponent {
    String getDetails();
    String getClassName();
    double getDifficulty();
    int getEstimatedTime(); // in minutes
    List<String> getRequirements();
    String getDisplayInfo();
}

/**
 * Basic assignment implementation
 */
class BasicAssignment implements AssignmentComponent {
    private final String className;
    private final String details;
    private final double difficulty;
    private final int estimatedTime;
    private final List<String> requirements;
    
    public BasicAssignment(String className, String details, double difficulty, int estimatedTime) {
        this.className = className;
        this.details = details;
        this.difficulty = difficulty;
        this.estimatedTime = estimatedTime;
        this.requirements = new ArrayList<>();
    }
    
    @Override
    public String getDetails() {
        return details;
    }
    
    @Override
    public String getClassName() {
        return className;
    }
    
    @Override
    public double getDifficulty() {
        return difficulty;
    }
    
    @Override
    public int getEstimatedTime() {
        return estimatedTime;
    }
    
    @Override
    public List<String> getRequirements() {
        return new ArrayList<>(requirements);
    }
    
    @Override
    public String getDisplayInfo() {
        return String.format("Assignment: %s [Class: %s, Difficulty: %.1f, Time: %d min]", 
                           details, className, difficulty, estimatedTime);
    }
}

/**
 * Base decorator class
 */
abstract class AssignmentDecorator implements AssignmentComponent {
    protected AssignmentComponent assignment;
    
    public AssignmentDecorator(AssignmentComponent assignment) {
        this.assignment = assignment;
    }
    
    @Override
    public String getDetails() {
        return assignment.getDetails();
    }
    
    @Override
    public String getClassName() {
        return assignment.getClassName();
    }
    
    @Override
    public double getDifficulty() {
        return assignment.getDifficulty();
    }
    
    @Override
    public int getEstimatedTime() {
        return assignment.getEstimatedTime();
    }
    
    @Override
    public List<String> getRequirements() {
        return assignment.getRequirements();
    }
    
    @Override
    public String getDisplayInfo() {
        return assignment.getDisplayInfo();
    }
}

/**
 * Decorator that adds deadline functionality
 */
class DeadlineDecorator extends AssignmentDecorator {
    private final LocalDateTime deadline;
    private final boolean isStrict;
    
    public DeadlineDecorator(AssignmentComponent assignment, LocalDateTime deadline, boolean isStrict) {
        super(assignment);
        this.deadline = deadline;
        this.isStrict = isStrict;
    }
    
    public LocalDateTime getDeadline() {
        return deadline;
    }
    
    public boolean isStrict() {
        return isStrict;
    }
    
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(deadline);
    }
    
    public long getHoursUntilDeadline() {
        return java.time.Duration.between(LocalDateTime.now(), deadline).toHours();
    }
    
    @Override
    public String getDisplayInfo() {
        String baseInfo = super.getDisplayInfo();
        String deadlineStr = deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String urgency = isOverdue() ? " [OVERDUE]" : 
                        getHoursUntilDeadline() < 24 ? " [DUE SOON]" : "";
        
        return baseInfo + String.format(" | Deadline: %s%s%s", 
                                      deadlineStr, 
                                      isStrict ? " (Strict)" : " (Flexible)",
                                      urgency);
    }
    
    @Override
    public List<String> getRequirements() {
        List<String> reqs = super.getRequirements();
        reqs.add("Must be submitted by: " + deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        if (isStrict) {
            reqs.add("No late submissions accepted");
        }
        return reqs;
    }
}

/**
 * Decorator that adds grading criteria
 */
class GradingDecorator extends AssignmentDecorator {
    private final Map<String, Integer> gradingCriteria;
    private final int totalPoints;
    
    public GradingDecorator(AssignmentComponent assignment, Map<String, Integer> gradingCriteria) {
        super(assignment);
        this.gradingCriteria = new HashMap<>(gradingCriteria);
        this.totalPoints = gradingCriteria.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public Map<String, Integer> getGradingCriteria() {
        return new HashMap<>(gradingCriteria);
    }
    
    public int getTotalPoints() {
        return totalPoints;
    }
    
    @Override
    public String getDisplayInfo() {
        String baseInfo = super.getDisplayInfo();
        return baseInfo + String.format(" | Total Points: %d", totalPoints);
    }
    
    @Override
    public List<String> getRequirements() {
        List<String> reqs = super.getRequirements();
        reqs.add("Grading Criteria:");
        for (Map.Entry<String, Integer> criterion : gradingCriteria.entrySet()) {
            reqs.add("  - " + criterion.getKey() + ": " + criterion.getValue() + " points");
        }
        return reqs;
    }
}

/**
 * Decorator that adds collaboration features
 */
class CollaborationDecorator extends AssignmentDecorator {
    private final boolean allowGroupWork;
    private final int maxGroupSize;
    private final List<String> collaborationTools;
    
    public CollaborationDecorator(AssignmentComponent assignment, boolean allowGroupWork, 
                                int maxGroupSize, List<String> collaborationTools) {
        super(assignment);
        this.allowGroupWork = allowGroupWork;
        this.maxGroupSize = maxGroupSize;
        this.collaborationTools = new ArrayList<>(collaborationTools);
    }
    
    public boolean isGroupWorkAllowed() {
        return allowGroupWork;
    }
    
    public int getMaxGroupSize() {
        return maxGroupSize;
    }
    
    public List<String> getCollaborationTools() {
        return new ArrayList<>(collaborationTools);
    }
    
    @Override
    public double getDifficulty() {
        // Group work might reduce individual difficulty
        double baseDifficulty = super.getDifficulty();
        return allowGroupWork ? baseDifficulty * 0.8 : baseDifficulty;
    }
    
    @Override
    public String getDisplayInfo() {
        String baseInfo = super.getDisplayInfo();
        String collabInfo = allowGroupWork ? 
            String.format(" | Group Work: Yes (Max %d members)", maxGroupSize) :
            " | Group Work: Individual only";
        return baseInfo + collabInfo;
    }
    
    @Override
    public List<String> getRequirements() {
        List<String> reqs = super.getRequirements();
        if (allowGroupWork) {
            reqs.add("Group work allowed (maximum " + maxGroupSize + " members)");
            reqs.add("Collaboration tools: " + String.join(", ", collaborationTools));
        } else {
            reqs.add("Individual work only - no collaboration allowed");
        }
        return reqs;
    }
}

/**
 * Decorator that adds multimedia requirements
 */
class MultimediaDecorator extends AssignmentDecorator {
    private final List<String> requiredMediaTypes;
    private final long maxFileSize; // in MB
    private final List<String> supportedFormats;
    
    public MultimediaDecorator(AssignmentComponent assignment, List<String> requiredMediaTypes, 
                             long maxFileSize, List<String> supportedFormats) {
        super(assignment);
        this.requiredMediaTypes = new ArrayList<>(requiredMediaTypes);
        this.maxFileSize = maxFileSize;
        this.supportedFormats = new ArrayList<>(supportedFormats);
    }
    
    public List<String> getRequiredMediaTypes() {
        return new ArrayList<>(requiredMediaTypes);
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public List<String> getSupportedFormats() {
        return new ArrayList<>(supportedFormats);
    }
    
    @Override
    public int getEstimatedTime() {
        // Multimedia assignments typically take longer
        return super.getEstimatedTime() + (requiredMediaTypes.size() * 30);
    }
    
    @Override
    public String getDisplayInfo() {
        String baseInfo = super.getDisplayInfo();
        return baseInfo + String.format(" | Media Required: %s (Max %dMB)", 
                                       String.join(", ", requiredMediaTypes), maxFileSize);
    }
    
    @Override
    public List<String> getRequirements() {
        List<String> reqs = super.getRequirements();
        reqs.add("Required media types: " + String.join(", ", requiredMediaTypes));
        reqs.add("Maximum file size: " + maxFileSize + "MB");
        reqs.add("Supported formats: " + String.join(", ", supportedFormats));
        return reqs;
    }
}

/**
 * Factory class for creating decorated assignments
 */
class AssignmentBuilder {
    private AssignmentComponent assignment;
    
    public AssignmentBuilder(String className, String details, double difficulty, int estimatedTime) {
        this.assignment = new BasicAssignment(className, details, difficulty, estimatedTime);
    }
    
    public AssignmentBuilder withDeadline(LocalDateTime deadline, boolean isStrict) {
        this.assignment = new DeadlineDecorator(assignment, deadline, isStrict);
        return this;
    }
    
    public AssignmentBuilder withGrading(Map<String, Integer> criteria) {
        this.assignment = new GradingDecorator(assignment, criteria);
        return this;
    }
    
    public AssignmentBuilder withCollaboration(boolean allowGroupWork, int maxGroupSize, List<String> tools) {
        this.assignment = new CollaborationDecorator(assignment, allowGroupWork, maxGroupSize, tools);
        return this;
    }
    
    public AssignmentBuilder withMultimedia(List<String> mediaTypes, long maxFileSize, List<String> formats) {
        this.assignment = new MultimediaDecorator(assignment, mediaTypes, maxFileSize, formats);
        return this;
    }
    
    public AssignmentComponent build() {
        return assignment;
    }
}
