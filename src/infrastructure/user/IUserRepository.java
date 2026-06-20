package infrastructure.user;

import infrastructure.common.IRepository;

public interface IUserRepository<T> extends IRepository<T> {

    /**
     * Searches the repository for an entity with the specified phone number.
     * @param phone The phone number to search for
     * @return The entity if found, or null if no matching phone number exists
     */
    T findByPhone(String phone);
}
