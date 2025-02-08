package projects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
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
        "5) Select a project",
        "0) Exit"
    );
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
                        selectProject();
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

        // Use interactive prompts instead of comma-separated input
        List<Material> materials = getMaterialsInput();
        List<Step> steps = getStepsInput();
        List<Category> categories = getCategoryInput();

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
        System.out.println("You have successfully created project: " + dbProject);
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
    public List<String> getListInput(String prompt) {
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


    private void updateProject() {
    	   // Display the list of projects to help the user select the one to update.
        listProjects();
        
        // Prompt the user to enter the ID of the project they wish to update.
        Integer projectId = getIntInput("Enter the project ID to update");
        if (projectId == null) {
            System.out.println("No project selected. Returning to main menu.");
            return;
        }
        
        try {
            // Retrieve the existing project.
            Project project = projectService.fetchProjectById(projectId);
            
            // Prompt the user for new values. If the user provides no input, keep the existing value.
            String projectName = getStringInput("Enter new project name (" + project.getProjectName() + ")");
            if (projectName != null && !projectName.isBlank()) {
                project.setProjectName(projectName);
            }
            
            String estimatedHoursInput = getStringInput("Enter new estimated hours (" + project.getEstimatedHours() + ")");
            if (estimatedHoursInput != null && !estimatedHoursInput.isBlank()) {
                try {
                    BigDecimal estimatedHours = new BigDecimal(estimatedHoursInput).setScale(2, RoundingMode.HALF_UP);
                    project.setEstimatedHours(estimatedHours);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid decimal value for estimated hours. Keeping existing value.");
                }
            }
            
            String actualHoursInput = getStringInput("Enter new actual hours (" + project.getActualHours() + ")");
            if (actualHoursInput != null && !actualHoursInput.isBlank()) {
                try {
                    BigDecimal actualHours = new BigDecimal(actualHoursInput).setScale(2, RoundingMode.HALF_UP);
                    project.setActualHours(actualHours);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid decimal value for actual hours. Keeping existing value.");
                }
            }
            
            String difficultyInput = getStringInput("Enter new difficulty (1-5) (" + project.getDifficulty() + ")");
            if (difficultyInput != null && !difficultyInput.isBlank()) {
                try {
                    Integer difficulty = Integer.valueOf(difficultyInput);
                    if (difficulty < 1 || difficulty > 5) {
                        System.out.println("Difficulty must be between 1 and 5. Keeping existing value.");
                    } else {
                        project.setDifficulty(difficulty);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number for difficulty. Keeping existing value.");
                }
            }
            
            String notes = getStringInput("Enter new project notes (" + project.getNotes() + ")");
            if (notes != null && !notes.isBlank()) {
                project.setNotes(notes);
            }
            
            // You can add similar prompts to update materials, steps, and categories if required.
            // For now, we're only updating the core project details.
            
            // Persist the updated project via the service layer.
            projectService.modifyProjectDetails(project);
            System.out.println("Project updated successfully: " + project);
        } catch (Exception e) {
            System.out.println("Error updating project: " + e.getMessage());
        }
    	
    }

    /**
     * Placeholder for deleting a project. Currently not implemented.
     */
    private void deleteProject() {
    	// Display the list of projects to assist the user in selecting one.
        listProjects();

        // Prompt the user for the project ID to delete.
        Integer projectId = getIntInput("Enter the project ID to delete");
        if (projectId == null) {
            System.out.println("No project selected. Returning to main menu.");
            return;
        }

        // Ask for confirmation before deletion.
        String confirmation = getStringInput("Are you sure you want to delete project with ID " + projectId + " (y/n)?");
        if (confirmation == null || !confirmation.equalsIgnoreCase("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try {
            // Invoke the service layer to perform the deletion.
            projectService.deleteProject(projectId);
            System.out.println("Project with ID " + projectId + " has been successfully deleted.");
        } catch (Exception e) {
            System.out.println("Error deleting project: " + e.getMessage());
        }
    		
    }
    


    private void selectProject() {
 // Listing projects so user can decide
    	listProjects();
 // Prompting user for project ID.
    	Integer projectId = getIntInput("Enter a project ID to view details");
 // Checking if valid id was selected
    	if(projectId == null) {
    		System.out.println("You need to enter a project id");
    		return;
    	}
 // Fetching project details from the service
    	Project project = projectService.fetchProjectById(projectId);
    	
    	if(project == null) {
    		System.out.println("Project with ID" + projectId + " not found");
    		return;
    	}
  // Displaying the basic project details.
    	System.out.println("\nProject Details:");
    	System.out.println("  " + project);
    	
    	   // Displaying associated materials

        if (!project.getMaterials().isEmpty()) {
            System.out.println("\nMaterials:");
            project.getMaterials().forEach(material ->
                System.out.println("  " + material));
        }
    }
    
    private List<Material> getMaterialsInput(){
    	List<Material> materials = new ArrayList<>();
    	while(true) {
    		// Prompting for material name
    		String name = getStringInput("Enter the material name (or type 'done' to finish)");
    			if(name == null || name.equalsIgnoreCase("done")) {
    				break; //User exits if he is finished by typing done
    				
    			}
    			
    	// Prompt for number required and validate
    			Integer numRequired = getIntInput("Enter the number required for \"" + name + "\"");
    			if (numRequired == null) {
    				System.out.println("Invalid input. Use an integer.");
    				continue;
    			}
    	// Prompting for cost and validating
    			String costInput = getStringInput("Enter the cost for \"" + name + "\"");
    			BigDecimal cost;
    			try {
    				cost = new BigDecimal(costInput).setScale(2, RoundingMode.HALF_UP);
    			} catch (NumberFormatException e) {
    				System.out.println("Enter a numerical price");
    				continue;
    			}
    		// Creating and adding Material object.
    			Material material = new Material();
    			material.setMaterialName(name);
    			material.setNumRequired(numRequired);
    			material.setCost(cost);
    			materials.add(material);
    			System.out.println("Material added: " + material);
    	}
		return materials;
    }

    private List<Step> getStepsInput(){
    	List<Step> steps = new ArrayList<>();
    	while(true) {
    		// Prompting user for step description
    		String description = getStringInput("Enter step description (or type 'done' to finish)");
    		if (description == null || description.equalsIgnoreCase("done")) {
    			break;
    		}
    		// Prompt for step order and validation
    		Integer order = getIntInput("Enter the order for this step");
    		if(order == null) {
    			System.out.println("Invalid input for step order. Please try again");
    			continue;
    		}
            // Create and add the Step object.
            Step step = new Step();
            step.setStepText(description);
            step.setStepOrder(order);
            steps.add(step);
            System.out.println("Step added: " + step);
    	}
		return steps;
    }
    
    private List<Category> getCategoryInput() {
    	List<Category> categories = new ArrayList<>();
    	while(true) {
    		// Prompting category name
    		String name = getStringInput("Enter category name (or type 'done' to finish)");
    		if (name == null || name.equalsIgnoreCase("done")) {
    			break;
    		}
    		
            // Create and add the Category object.
            Category category = new Category();
            category.setCategoryName(name);
            categories.add(category);
            System.out.println("Category added: " + category);
    	}
		return categories;
    }
    
}
