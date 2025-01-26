package projects.service;

import java.sql.SQLException;
import java.util.List;
import projects.dao.ProjectDao;
import projects.entity.Project;

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
        return projectDao.fetchProjectById(projectId);
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
}
