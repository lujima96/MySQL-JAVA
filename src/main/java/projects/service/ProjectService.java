package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

/**
 * This class provides services related to Project operations.
 */
public class ProjectService {
    private ProjectDao projectDao = new ProjectDao();

    /**
     * Adds a new project along with its materials, steps, and categories.
     *
     * @param project The project to add.
     * @return The added project with the generated project ID.
     */
    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    /**
     * Fetches all projects with their associated materials, steps, and categories.
     *
     * @return A list of all projects.
     */
    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects();
    }

    /**
     * Fetches a project by its ID along with its materials, steps, and categories.
     *
     * @param projectId The ID of the project to fetch.
     * @return The fetched project or null if not found.
     */
    public Project fetchProjectById(Integer projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        return projectDao.fetchProjectById(projectId)
            .orElseThrow(() -> new NoSuchElementException( // Use existing import
                "Project not found with ID: " + projectId));
    }


    /**
     * Updates an existing project.
     *
     * @param project The project with updated information.
     * @return The number of rows affected.
     */
    public int updateProject(Project project) {
        return projectDao.updateProject(project);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param projectId The ID of the project to delete.
     * @return The number of rows affected.
     */
    public int deleteProject(Integer projectId) {
        return projectDao.deleteProject(projectId);
    }

	public void modifyProjectDetails(Project project) {
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
		}
	
		
	}
}
