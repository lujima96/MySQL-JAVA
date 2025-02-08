package projects.entity;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a project with various attributes including materials, steps, and categories.
 */
public class Project {
    private Integer projectId;
    private String projectName;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private Integer difficulty;
    private String notes;

    private List<Material> materials = new LinkedList<>();
    private List<Step> steps = new LinkedList<>();
    private List<Category> categories = new LinkedList<>();

    // Getters and Setters for basic fields

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public BigDecimal getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(BigDecimal estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public BigDecimal getActualHours() {
        return actualHours;
    }

    public void setActualHours(BigDecimal actualHours) {
        this.actualHours = actualHours;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public List<Category> getCategories() {
        return categories;
    }

    /**
     * Sets the materials for the project based on a list of Material objects.
     *
     * @param materials List of Material objects.
     */
    public void setMaterials(List<Material> materials) {
        if (materials == null || materials.isEmpty()) {
            this.materials = new LinkedList<>();
            return;
        }

        this.materials = new LinkedList<>(materials);
    }

    /**
     * Sets the steps for the project based on a list of Step objects.
     *
     * @param steps List of Step objects.
     */
    public void setSteps(List<Step> steps) {
        if (steps == null || steps.isEmpty()) {
            this.steps = new LinkedList<>();
            return;
        }

        this.steps = new LinkedList<>(steps);
    }

    /**
     * Sets the categories for the project based on a list of Category objects.
     *
     * @param categories List of Category objects.
     */
    public void setCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            this.categories = new LinkedList<>();
            return;
        }

        this.categories = new LinkedList<>(categories);
    }

    /**
     * Adds a single Material to the project's material list.
     *
     * @param material The Material object to add.
     */
    public void addMaterial(Material material) {
        if (material == null) {
            return;
        }
        this.materials.add(material);
    }

    /**
     * Adds a single Step to the project's step list.
     *
     * @param step The Step object to add.
     */
    public void addStep(Step step) {
        if (step == null) {
            return;
        }
        this.steps.add(step);
    }

    /**
     * Adds a single Category to the project's category list.
     *
     * @param category The Category object to add.
     */
    public void addCategory(Category category) {
        if (category == null) {
            return;
        }
        this.categories.add(category);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("\n   ID=").append(projectId);
        result.append("\n   Name=").append(projectName);
        result.append("\n   Estimated Hours=").append(estimatedHours);
        result.append("\n   Actual Hours=").append(actualHours);
        result.append("\n   Difficulty=").append(difficulty);
        result.append("\n   Notes=").append(notes);

        result.append("\n   Materials:");
        for (Material material : materials) {
            result.append("\n      ").append(material);
        }

        result.append("\n   Steps:");
        for (Step step : steps) {
            result.append("\n      ").append(step);
        }

        result.append("\n   Categories:");
        for (Category category : categories) {
            result.append("\n      ").append(category);
        }

        return result.toString();
    }


}
