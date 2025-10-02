package com.example;

/**
 * Strategy Pattern Implementation - Behavioral Design Pattern
 * Defines a family of algorithms for grading assignments
 */
public interface GradingStrategy {
    double calculateGrade(int score, int maxScore);
    String getGradeLetter(double percentage);
    String getStrategyName();
}

/**
 * Standard percentage-based grading strategy
 */
class StandardGradingStrategy implements GradingStrategy {
    @Override
    public double calculateGrade(int score, int maxScore) {
        if (maxScore == 0) return 0.0;
        return (double) score / maxScore * 100;
    }
    
    @Override
    public String getGradeLetter(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
    
    @Override
    public String getStrategyName() {
        return "Standard Grading";
    }
}

/**
 * Curve-based grading strategy that adjusts scores
 */
class CurveGradingStrategy implements GradingStrategy {
    private double curveBonus;
    
    public CurveGradingStrategy(double curveBonus) {
        this.curveBonus = curveBonus;
    }
    
    @Override
    public double calculateGrade(int score, int maxScore) {
        if (maxScore == 0) return 0.0;
        double basePercentage = (double) score / maxScore * 100;
        return Math.min(100.0, basePercentage + curveBonus);
    }
    
    @Override
    public String getGradeLetter(double percentage) {
        if (percentage >= 85) return "A";
        if (percentage >= 75) return "B";
        if (percentage >= 65) return "C";
        if (percentage >= 55) return "D";
        return "F";
    }
    
    @Override
    public String getStrategyName() {
        return "Curve Grading (+" + curveBonus + "%)";
    }
}

/**
 * Pass/Fail grading strategy
 */
class PassFailGradingStrategy implements GradingStrategy {
    private double passingThreshold;
    
    public PassFailGradingStrategy(double passingThreshold) {
        this.passingThreshold = passingThreshold;
    }
    
    @Override
    public double calculateGrade(int score, int maxScore) {
        if (maxScore == 0) return 0.0;
        return (double) score / maxScore * 100;
    }
    
    @Override
    public String getGradeLetter(double percentage) {
        return percentage >= passingThreshold ? "PASS" : "FAIL";
    }
    
    @Override
    public String getStrategyName() {
        return "Pass/Fail (" + passingThreshold + "% threshold)";
    }
}

/**
 * Context class that uses grading strategies
 */
class GradeCalculator {
    private GradingStrategy strategy;
    
    public GradeCalculator(GradingStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(GradingStrategy strategy) {
        this.strategy = strategy;
        Logger.logInfo("Grading strategy changed to: " + strategy.getStrategyName());
    }
    
    public GradeResult calculateGrade(int score, int maxScore) {
        double percentage = strategy.calculateGrade(score, maxScore);
        String letter = strategy.getGradeLetter(percentage);
        return new GradeResult(score, maxScore, percentage, letter, strategy.getStrategyName());
    }
}

/**
 * Result object for grade calculations
 */
class GradeResult {
    private final int score;
    private final int maxScore;
    private final double percentage;
    private final String letterGrade;
    private final String strategyUsed;
    
    public GradeResult(int score, int maxScore, double percentage, String letterGrade, String strategyUsed) {
        this.score = score;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.letterGrade = letterGrade;
        this.strategyUsed = strategyUsed;
    }
    
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public double getPercentage() { return percentage; }
    public String getLetterGrade() { return letterGrade; }
    public String getStrategyUsed() { return strategyUsed; }
    
    @Override
    public String toString() {
        return String.format("Score: %d/%d (%.1f%%) = %s [%s]", 
                           score, maxScore, percentage, letterGrade, strategyUsed);
    }
}
