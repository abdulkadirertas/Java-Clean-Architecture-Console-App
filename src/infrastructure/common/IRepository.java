package infrastructure.common;

import java.util.List;

public interface IRepository<T> {
    /**
     * Adds a new entity instance to the repository
     * @param id The unique identifier of the instances
     * @param entity The entity instance to be added
     */
    public void add(String id, T entity);

    /**
     * Returns the entity instance corresponding to given id
     * @param id The unique identifier of the instance to be retrieved
     * @return The entity instance associated with the specified ID, or null if not found
     */
    public T get(String id);

    /**
     * Fetches all entities from the repository and returns them as a list
     * @return A list containing all entity instance
     */
    public List<T> getAll();

    /**
     * Loads entity data from the persistent storage and initializes the repository state
     */
    public void load();

    /**
     * Saves all currently existing data to the data source
     */
    public void save();
}
