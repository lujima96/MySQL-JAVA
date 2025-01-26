package projects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import projects.service.ProjectService;

/*
 * This class is a menu app that accepts user input obtained by the scanner object.
 * Afterwards those inputs are used to perform CRUD operations on the project tables
 */
public class ProjectsApp {
    private Scanner scanner;
    private ProjectService projectService = new ProjectService();

    // @formatter:off
    private List<String> operations = List.of(
        "1) Add a project",
        "2) List all projects",
        "3) Update a project",
        "4) Delete a project",
        "5) View project details",
        "0) Exit"
    );
    // @formatter:on

    /*
     * Entry point to application
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new ProjectsApp(scanner).processUserSelections();
        }
    }

    /**
     * Constructor that accepts a Scanner object.
     *
     * @param scanner The Scanner object for user input.
     */
    public ProjectsApp(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * This method provides a list of operations, gives the user a menu, and continues until
     * the user terminates the application.
     */
    private void processUserSelections() {
        boolean done = false;

        while (!done) {
            try {
                int selection = getUserSelection();
                switch (selection) {
                    case 0:
                        done = exitMenu();
                        break;
                    case 1:
                        createProject();
                        break;
                    case 2:
                        listProjects();
                        break;
                    case 3:
                        updateProject();
                        break;
                    case 4:
                        deleteProject();
                        break;
                    case 5:
                        viewProjectDetails();
                        break;
                    default:
                        System.out.println("\n" + selection + " is not a valid selection. Try again.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage() + " Try again.");
            }
        }
    }

    /**
     * Creates a new project by collecting user input and saving it to the database.
     *
     * @throws SQLException If a database access error occurs.
     */
    private void createProject() throws SQLException {
        String projectName = getStringInput("Enter the project name");
        BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
        BigDecimal actualHours = getDecimalInput("Enter the actual hours");
        Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
        String notes = getStringInput("Enter the project notes");

        // Validate difficulty
        if (difficulty != null && (difficulty < 1 || difficulty > 5)) {
            System.out.println("Difficulty must be between 1 and 5.");
            return;
        }

        // Prompt for materials with detailed input
        List<Material> materials = getDetailedMaterialsInput("Enter the materials (format: Name:NumRequired:Cost, separated by commas)");

        // Prompt for steps and categories as before
        List<Step> steps = getDetailedStepsInput("Enter the steps (format: Description:Order, separated by commas)");
        List<Category> categories = getDetailedCategoriesInput("Enter the categories (format: Name, separated by commas)");

        Project project = new Project();

        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        // Add materials, steps, and categories using helper methods
        for (Material material : materials) {
            project.addMaterial(material);
        }

        for (Step step : steps) {
            project.addStep(step);
        }

        for (Category category : categories) {
            project.addCategory(category);
        }

        Project dbProject = projectService.addProject(project);
        System.out.println("You have successfully created project:" + dbProject);
    }

    /**
     * Prompts the user to enter materials with details and parses them into Material objects.
     *
     * @param prompt The prompt message to display.
     * @return A list of Material objects with all fields set.
     */
    private List<Material> getDetailedMaterialsInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();

        if (input.isBlank()) {
            return List.of();
        }

        List<Material> materials = new java.util.LinkedList<>();
        String[] materialEntries = input.split(",");

        for (String entry : materialEntries) {
            String[] parts = entry.trim().split(":");
            if (parts.length != 3) {
                System.out.println("Invalid format for material: " + entry);
                continue; // Skip invalid entries
            }

            String name = parts[0].trim();
            Integer numRequired = null;
            BigDecimal cost = null;

            try {
                numRequired = Integer.valueOf(parts[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for numRequired in material: " + entry);
                continue; // Skip invalid entries
            }

            try {
                cost = new BigDecimal(parts[2].trim()).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for cost in material: " + entry);
                continue; // Skip invalid entries
            }

            Material material = new Material();
            material.setMaterialName(name);
            material.setNumRequired(numRequired);
            material.setCost(cost);

            materials.add(material);
        }

        return materials;
    }

    /**
     * Prompts the user to enter steps with details and parses them into Step objects.
     *
     * @param prompt The prompt message to display.
     * @return A list of Step objects with all fields set.
     */
    private List<Step> getDetailedStepsInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();

        if (input.isBlank()) {
            return List.of();
        }

        List<Step> steps = new java.util.LinkedList<>();
        String[] stepEntries = input.split(",");

        for (String entry : stepEntries) {
            String[] parts = entry.trim().split(":");
            if (parts.length != 2) {
                System.out.println("Invalid format for step: " + entry);
                continue; // Skip invalid entries
            }

            String description = parts[0].trim();
            Integer order = null;

            try {
                order = Integer.valueOf(parts[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for step order in step: " + entry);
                continue; // Skip invalid entries
            }

            Step step = new Step();
            step.setStepText(description);
            step.setStepOrder(order);

            steps.add(step);
        }

        return steps;
    }

    /**
     * Prompts the user to enter categories with details and parses them into Category objects.
     *
     * @param prompt The prompt message to display.
     * @return A list of Category objects with all fields set.
     */
    private List<Category> getDetailedCategoriesInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();

        if (input.isBlank()) {
            return List.of();
        }

        List<Category> categories = new java.util.LinkedList<>();
        String[] categoryEntries = input.split(",");

        for (String entry : categoryEntries) {
            String name = entry.trim();
            if (name.isEmpty()) {
                System.out.println("Invalid format for category: " + entry);
                continue; // Skip invalid entries
            }

            Category category = new Category();
            category.setCategoryName(name);

            categories.add(category);
        }

        return categories;
    }

    /**
     * Gets user input from console and converts it to BigDecimal.
     *
     * @param prompt The prompt message to display to the user.
     * @return BigDecimal representation of the input, or null if no input was provided.
     * @throws DbException If the input is not a valid decimal number.
     */
    private BigDecimal getDecimalInput(String prompt) {
        String input = getStringInput(prompt);

        if (Objects.isNull(input)) {
            return null;
        }
        try {
            // Creates BigDecimal object and sets to two decimal places
            return new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new DbException("Error: '" + input + "' is not a valid decimal number.");
        }
    }

    /**
     * Allows the user to exit the application. It prints a goodbye message
     * and returns true to terminate the application.
     *
     * @return {@code true} indicating the application should exit.
     */
    private boolean exitMenu() {
        System.out.println("Exiting the application. Goodbye!");
        return true;
    }

    /**
     * Prints available menu selections and gets the user's selection.
     *
     * @return The menu selection as an integer, or -1 if no valid selection was made.
     */
    private int getUserSelection() {
        printOperations();

        Integer input = getIntInput("Enter a menu selection");

        return Objects.isNull(input) ? -1 : input;
    }

    /**
     * Prints a prompt to the console and retrieves the user's input as a String.
     *
     * @param prompt The prompt message to display.
     * @return The user's input as a trimmed String, or null if no input was provided.
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();

        return input.isBlank() ? null : input.trim();
    }

    /**
     * Prompts the user for an integer input and returns the value.
     *
     * @param prompt The message to display to the user.
     * @return The integer entered by the user, or null if no input was provided.
     * @throws DbException If the input is not a valid integer.
     */
    private Integer getIntInput(String prompt) {
        String input = getStringInput(prompt);

        if (Objects.isNull(input)) {
            return null;
        }

        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            throw new DbException("Error: '" + input + "' is not a valid number.");
        }
    }

    /**
     * Prompts the user for a comma-separated list input and converts it into a List<String>.
     *
     * @param prompt The message to display to the user.
     * @return A list of trimmed, non-empty strings.
     */
    private List<String> getListInput(String prompt) {
        String input = getStringInput(prompt);
        if (Objects.isNull(input)) {
            return List.of();
        }
        return List.of(input.split(",")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
    }

    /**
     * Prints available menu selections, one item per line.
     */
    private void printOperations() {
        System.out.println("\nThese are the available selections. Press the Enter key to quit:");

        // Using a lambda expression to print each operation
        operations.forEach(line -> System.out.println("  " + line));
    }

    /**
     * Lists all projects by fetching them from the ProjectService and printing each one.
     */
    private void listProjects() {
        List<Project> projects = projectService.fetchAllProjects();
        System.out.println("\nProjects:");
        projects.forEach(project -> System.out.println("  " + project));
    }

    /**
     * Placeholder for updating a project. Currently not implemented.
     */
    private void updateProject() {
        // Implementation goes here
        System.out.println("Update project feature is not yet implemented.");
    }

    /**
     * Placeholder for deleting a project. Currently not implemented.
     */
    private void deleteProject() {
        // Implementation goes here
        System.out.println("Delete project feature is not yet implemented.");
    }

    /**
     * Placeholder for viewing project details. Currently not implemented.
     */
    private void viewProjectDetails() {
        // Implementation goes here
        System.out.println("View project details feature is not yet implemented.");
    }
}
