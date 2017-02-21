package io.hobaskos.event.service;

import io.hobaskos.event.service.dto.UserConnectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Interface for managing UserConnection.
 */
public interface UserConnectionService {

    /**
     * Save a userConnection.
     *
     * @param userConnectionDTO the entity to save
     * @return the persisted entity
     */
    UserConnectionDTO save(UserConnectionDTO userConnectionDTO);

    /**
     *  Get all the userConnections.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<UserConnectionDTO> findAll(Pageable pageable);

    /**
     *  Get the "id" userConnection.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    UserConnectionDTO findOne(Long id);

    /**
     *  Delete the "id" userConnection.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the userConnection corresponding to the query.
     *
     *  @param query the query of the search
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<UserConnectionDTO> search(String query, Pageable pageable);
}
