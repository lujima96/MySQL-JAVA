package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.entity.Category;
import projects.exception.DbException;
import projects.DaoBase;

/**
 * This class uses JDBC to perform CRUD operations on the project tables.
 */
@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {

    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    /**
     * Insert a project row into the project table along with its materials, steps, and categories.
     *
     * @param project The project to be inserted.
     * @return The Project object with the primary key.
     */
    public Project insertProject(Project project) {
        String sql = ""
                + "INSERT INTO " + PROJECT_TABLE + " "
                + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
                + "VALUES "
                + "(?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            try {
                startTransaction(conn); // Start transaction

                // Insert into PROJECT table
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameter(stmt, 1, project.getProjectName(), String.class);
                    setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                    setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                    setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                    setParameter(stmt, 5, project.getNotes(), String.class);

                    stmt.executeUpdate(); // Execute the INSERT statement
                }

                // Retrieve generated project ID
                Integer projectID = getLastInsertId(conn, PROJECT_TABLE);
                project.setProjectId(projectID); // Set the generated ID to the project object

                // Insert Materials
                insertMaterials(conn, project);

                // Insert Steps
                insertSteps(conn, project);

                // Insert Categories
                insertCategories(conn, project);

                commitTransaction(conn); // Commit transaction

                return project; // Return the project with the ID
            } catch (Exception e) {
                rollbackTransaction(conn); // Rollback in case of any exception
                throw new DbException("Error inserting project: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new DbException("Error inserting project: " + e.getMessage(), e);
        }
    }

    /**
     * Insert materials associated with a project.
     *
     * @param conn    The database connection.
     * @param project The project containing materials.
     * @throws SQLException If a database access error occurs.
     */
    private void insertMaterials(Connection conn, Project project) throws SQLException {
        List<Material> materials = project.getMaterials();
        if (materials == null || materials.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO " + MATERIAL_TABLE + " (material_name, num_required, cost, project_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (Material material : materials) {
                stmt.setString(1, material.getMaterialName());
                stmt.setInt(2, material.getNumRequired() != null ? material.getNumRequired() : 0);
                stmt.setBigDecimal(3, material.getCost() != null ? material.getCost() : BigDecimal.ZERO);
                stmt.setInt(4, project.getProjectId());
                stmt.addBatch();
            }
            stmt.executeBatch();

            // Retrieve and set generated material IDs
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                int index = 0;
                while (rs.next()) {
                    materials.get(index++).setMaterialId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Insert steps associated with a project.
     *
     * @param conn    The database connection.
     * @param project The project containing steps.
     * @throws SQLException If a database access error occurs.
     */
    private void insertSteps(Connection conn, Project project) throws SQLException {
        List<Step> steps = project.getSteps();
        if (steps == null || steps.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO " + STEP_TABLE + " (step_text, step_order, project_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            int order = 1;
            for (Step step : steps) {
                stmt.setString(1, step.getStepText());
                stmt.setInt(2, order++);
                stmt.setInt(3, project.getProjectId());
                stmt.addBatch();
            }
            stmt.executeBatch();

            // Optionally retrieve and set step IDs
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                int index = 0;
                while (rs.next()) {
                    steps.get(index++).setStepId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Insert categories associated with a project.
     *
     * @param conn    The database connection.
     * @param project The project containing categories.
     * @throws SQLException If a database access error occurs.
     */
    private void insertCategories(Connection conn, Project project) throws SQLException {
        List<Category> categories = project.getCategories();
        if (categories == null || categories.isEmpty()) {
            return;
        }

        String findCategorySql = "SELECT category_id FROM " + CATEGORY_TABLE + " WHERE category_name = ?";
        String insertCategorySql = "INSERT INTO " + CATEGORY_TABLE + " (category_name) VALUES (?)";
        String insertProjectCategorySql = "INSERT INTO " + PROJECT_CATEGORY_TABLE + " (project_id, category_id) VALUES (?, ?)";

        try (PreparedStatement findCatStmt = conn.prepareStatement(findCategorySql);
             PreparedStatement insCatStmt = conn.prepareStatement(insertCategorySql, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement insProjCatStmt = conn.prepareStatement(insertProjectCategorySql)) {

            for (Category category : categories) {
                String categoryName = category.getCategoryName();
                Integer categoryId = null;

                // Check if category exists
                findCatStmt.setString(1, categoryName);
                try (ResultSet rs = findCatStmt.executeQuery()) {
                    if (rs.next()) {
                        categoryId = rs.getInt("category_id");
                    }
                }

                // Insert category if it doesn't exist
                if (categoryId == null) {
                    insCatStmt.setString(1, categoryName);
                    insCatStmt.executeUpdate();

                    try (ResultSet rs = insCatStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            categoryId = rs.getInt(1);
                        } else {
                            throw new SQLException("Failed to retrieve category_id for new category: " + categoryName);
                        }
                    }
                }

                // Insert into project_category
                insProjCatStmt.setInt(1, project.getProjectId());
                insProjCatStmt.setInt(2, categoryId);
                insProjCatStmt.addBatch();
            }

            insProjCatStmt.executeBatch();
        }
    }

    /**
     * Fetch all projects from the project table along with their materials, steps, and categories.
     *
     * @return A list of all Project objects.
     */
    public List<Project> fetchAllProjects() {
        String sql = "SELECT project_id, project_name, estimated_hours, actual_hours, difficulty, notes "
                   + "FROM " + PROJECT_TABLE + " "
                   + "ORDER BY project_id";

        List<Project> projects = new ArrayList<>();

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Project project = new Project();
                project.setProjectId(getInt(rs, "project_id"));
                project.setProjectName(getString(rs, "project_name"));
                project.setEstimatedHours(getBigDecimal(rs, "estimated_hours"));
                project.setActualHours(getBigDecimal(rs, "actual_hours"));
                project.setDifficulty(getInt(rs, "difficulty"));
                project.setNotes(getString(rs, "notes"));

                // Fetch and set materials, steps, and categories
                project.setMaterials(fetchMaterialsForProject(conn, project.getProjectId()));
                project.setSteps(fetchStepsForProject(conn, project.getProjectId()));
                project.setCategories(fetchCategoriesForProject(conn, project.getProjectId()));

                projects.add(project);
            }

            return projects;
        } catch (SQLException e) {
            throw new DbException("Error fetching all projects: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch materials associated with a project.
     *
     * @param conn      The database connection.
     * @param projectId The ID of the project.
     * @return A list of Material objects.
     * @throws SQLException If a database access error occurs.
     */
    private List<Material> fetchMaterialsForProject(Connection conn, int projectId) throws SQLException {
        List<Material> materials = new ArrayList<>();

        String sql = "SELECT material_id, material_name, num_required, cost FROM " + MATERIAL_TABLE + " WHERE project_id = ? ORDER BY material_id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Material material = new Material();
                    material.setMaterialId(getInt(rs, "material_id"));
                    material.setMaterialName(getString(rs, "material_name"));
                    material.setNumRequired(getInt(rs, "num_required"));
                    material.setCost(getBigDecimal(rs, "cost"));
                    materials.add(material);
                }
            }
        }
        return materials;
    }

    /**
     * Fetch steps associated with a project.
     *
     * @param conn      The database connection.
     * @param projectId The ID of the project.
     * @return A list of Step objects.
     * @throws SQLException If a database access error occurs.
     */
    private List<Step> fetchStepsForProject(Connection conn, int projectId) throws SQLException {
        List<Step> steps = new ArrayList<>();

        String sql = "SELECT step_id, step_text, step_order FROM " + STEP_TABLE + " WHERE project_id = ? ORDER BY step_order";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Step step = new Step();
                    step.setStepId(getInt(rs, "step_id"));
                    step.setStepText(getString(rs, "step_text"));
                    step.setStepOrder(getInt(rs, "step_order"));
                    steps.add(step);
                }
            }
        }
        return steps;
    }

    /**
     * Fetch categories associated with a project.
     *
     * @param conn      The database connection.
     * @param projectId The ID of the project.
     * @return A list of Category objects.
     * @throws SQLException If a database access error occurs.
     */
    private List<Category> fetchCategoriesForProject(Connection conn, int projectId) throws SQLException {
        List<Category> categories = new ArrayList<>();

        String sql = "SELECT c.category_id, c.category_name "
                   + "FROM " + CATEGORY_TABLE + " c "
                   + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
                   + "WHERE pc.project_id = ? "
                   + "ORDER BY c.category_id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(getInt(rs, "category_id"));
                    category.setCategoryName(getString(rs, "category_name"));
                    categories.add(category);
                }
            }
        }
        return categories;
    }

    /**
     * Update an existing project in the project table.
     * Note: This method currently only updates the project details.
     * To update materials, steps, and categories, additional logic is required.
     *
     * @param project The project with updated information.
     * @return The number of rows affected.
     */
    public int updateProject(Project project) {
        String sql = "UPDATE " + PROJECT_TABLE + " SET project_name = ?, estimated_hours = ?, actual_hours = ?, difficulty = ?, notes = ? WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            try {
                startTransaction(conn); // Start transaction

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameter(stmt, 1, project.getProjectName(), String.class);
                    setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
                    setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
                    setParameter(stmt, 4, project.getDifficulty(), Integer.class);
                    setParameter(stmt, 5, project.getNotes(), String.class);
                    setParameter(stmt, 6, project.getProjectId(), Integer.class);

                    int rowsAffected = stmt.executeUpdate(); // Execute the UPDATE statement

                    // Optionally, update materials, steps, and categories here
                    // For example:
                    // deleteExistingMaterials(conn, project.getProjectId());
                    // insertMaterials(conn, project);
                    // Similarly for steps and categories

                    commitTransaction(conn); // Commit transaction

                    return rowsAffected; // Return the number of rows affected
                }
            } catch (Exception e) {
                rollbackTransaction(conn); // Rollback in case of any exception
                throw new DbException("Error updating project: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new DbException("Error updating project: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a project from the project table.
     *
     * @param projectId The ID of the project to delete.
     * @return The number of rows affected.
     */
    public int deleteProject(Integer projectId) {
        String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            try {
                startTransaction(conn); // Start transaction

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    setParameter(stmt, 1, projectId, Integer.class);

                    int rowsAffected = stmt.executeUpdate(); // Execute the DELETE statement

                    commitTransaction(conn); // Commit transaction

                    return rowsAffected; // Return the number of rows affected
                }
            } catch (Exception e) {
                rollbackTransaction(conn); // Rollback in case of any exception
                throw new DbException("Error deleting project: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new DbException("Error deleting project: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch a single project by its ID along with its materials, steps, and categories.
     *
     * @param projectId The ID of the project to fetch.
     * @return The Project object if found; otherwise, null.
     */
    public Project fetchProjectById(Integer projectId) {
        String sql = "SELECT project_id, project_name, estimated_hours, actual_hours, difficulty, notes FROM " + PROJECT_TABLE + " WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameter(stmt, 1, projectId, Integer.class);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Project project = new Project();
                    project.setProjectId(getInt(rs, "project_id"));
                    project.setProjectName(getString(rs, "project_name"));
                    project.setEstimatedHours(getBigDecimal(rs, "estimated_hours"));
                    project.setActualHours(getBigDecimal(rs, "actual_hours"));
                    project.setDifficulty(getInt(rs, "difficulty"));
                    project.setNotes(getString(rs, "notes"));

                    // Fetch and set materials, steps, and categories
                    project.setMaterials(fetchMaterialsForProject(conn, project.getProjectId()));
                    project.setSteps(fetchStepsForProject(conn, project.getProjectId()));
                    project.setCategories(fetchCategoriesForProject(conn, project.getProjectId()));

                    return project;
                } else {
                    return null; // Project not found
                }
            }

        } catch (SQLException e) {
            throw new DbException("Error fetching project by ID: " + e.getMessage(), e);
        }
    }

    // Helper methods to extract data from ResultSet
    protected String getString(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    protected Integer getInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    protected BigDecimal getBigDecimal(ResultSet rs, String columnName) throws SQLException {
        return rs.getBigDecimal(columnName);
    }

    // Transaction management methods

    /**
     * Starts a transaction by setting auto-commit to false.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    protected void startTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    /**
     * Commits the current transaction and sets auto-commit back to true.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    protected void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    /**
     * Rolls back the current transaction and sets auto-commit back to true.
     *
     * @param conn The database connection.
     */
    protected void rollbackTransaction(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DbException("Error rolling back transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Sets a parameter in the PreparedStatement based on its type.
     *
     * @param stmt           The PreparedStatement.
     * @param parameterIndex The index of the parameter to set.
     * @param value          The value to set.
     * @param type           The Class type of the value.
     * @throws SQLException If a database access error occurs.
     */
    protected void setParameter(PreparedStatement stmt, int parameterIndex, Object value, Class<?> type)
            throws SQLException {
        if (type == String.class) {
            stmt.setString(parameterIndex, (String) value);
        } else if (type == Integer.class) {
            stmt.setInt(parameterIndex, (Integer) value);
        } else if (type == BigDecimal.class) {
            stmt.setBigDecimal(parameterIndex, (BigDecimal) value);
        } else {
            throw new SQLException("Unhandled parameter type: " + type.getName());
        }
    }

    /**
     * Retrieves the last inserted ID for a given table.
     *
     * @param conn      The database connection.
     * @param tableName The name of the table.
     * @return The last inserted ID.
     * @throws SQLException If a database access error occurs.
     */
    protected Integer getLastInsertId(Connection conn, String tableName) throws SQLException {
        // Implement database-specific logic to retrieve the last inserted ID
        // Example for MySQL:
        String sql = "SELECT LAST_INSERT_ID()";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve last insert ID for table " + tableName);
            }
        }
    }
}
