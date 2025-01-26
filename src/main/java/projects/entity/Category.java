package projects.entity;

/**
 * Represents a category for a project.
 */
public class Category {
    private Integer categoryId;
    private String categoryName;

    // Getters and Setters

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "Category [ID=" + categoryId + ", Name=" + categoryName + "]";
    }
}
