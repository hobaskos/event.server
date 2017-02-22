package io.hobaskos.event.service.impl;

import io.hobaskos.event.domain.User;
import io.hobaskos.event.domain.enumeration.UserConnectionType;
import io.hobaskos.event.service.UserConnectionService;
import io.hobaskos.event.domain.UserConnection;
import io.hobaskos.event.repository.UserConnectionRepository;
import io.hobaskos.event.repository.search.UserConnectionSearchRepository;
import io.hobaskos.event.service.dto.UserConnectionDTO;
import io.hobaskos.event.service.mapper.UserConnectionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing UserConnection.
 */
@Service
@Transactional
public class UserConnectionServiceImpl implements UserConnectionService{

    private final Logger log = LoggerFactory.getLogger(UserConnectionServiceImpl.class);

    @Inject
    private UserConnectionRepository userConnectionRepository;

    @Inject
    private UserConnectionMapper userConnectionMapper;

    @Inject
    private UserConnectionSearchRepository userConnectionSearchRepository;

    /**
     * Save a userConnection.
     *
     * @param userConnectionDTO the entity to save
     * @return the persisted entity
     */
    public UserConnectionDTO save(UserConnectionDTO userConnectionDTO) {
        log.debug("Request to save UserConnection : {}", userConnectionDTO);
        UserConnection userConnection = userConnectionMapper.userConnectionDTOToUserConnection(userConnectionDTO);
        userConnection = userConnectionRepository.save(userConnection);
        UserConnectionDTO result = userConnectionMapper.userConnectionToUserConnectionDTO(userConnection);
        userConnectionSearchRepository.save(userConnection);
        return result;
    }

    /**
     * Make a friend connection between users.
     * @param requester
     * @param requestee
     * @return
     */
    public UserConnectionDTO makeConnection(User requester, User requestee) {
        return userConnectionRepository.findOneByRequesterAndRequesteeAndType(requestee, requester, UserConnectionType.PENDING)
            .map(userConnection -> {
                userConnection.setType(UserConnectionType.CONFIRMED);
                return save(userConnectionMapper.userConnectionToUserConnectionDTO(userConnection));
            })
            .orElseGet(() -> {
                UserConnectionDTO userConnectionDTO = new UserConnectionDTO();
                userConnectionDTO.setRequesterId(requester.getId());
                userConnectionDTO.setRequesteeId(requestee.getId());
                userConnectionDTO.setType(UserConnectionType.PENDING);
                return save(userConnectionDTO);
            });
    }

    /**
     * Make a follower connection
     * @param requester
     * @param requestee
     * @return
     */
    public UserConnectionDTO makeFollowingConnection(User requester, User requestee) {
        return userConnectionRepository.findOneByRequesterAndRequesteeAndType(requester, requestee, UserConnectionType.FOLLOWER)
            .map(userConnection -> userConnectionMapper.userConnectionToUserConnectionDTO(userConnection))
            .orElseGet(() -> {
                UserConnectionDTO userConnectionDTO = new UserConnectionDTO();
                userConnectionDTO.setRequesterId(requester.getId());
                userConnectionDTO.setRequesteeId(requestee.getId());
                userConnectionDTO.setType(UserConnectionType.FOLLOWER);
                return save(userConnectionDTO);
            });
    }

    /**
     *  Get all the userConnections.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<UserConnectionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all UserConnections");
        Page<UserConnection> result = userConnectionRepository.findAll(pageable);
        return result.map(userConnection -> userConnectionMapper.userConnectionToUserConnectionDTO(userConnection));
    }

    /**
     *  Get one userConnection by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public UserConnectionDTO findOne(Long id) {
        log.debug("Request to get UserConnection : {}", id);
        UserConnection userConnection = userConnectionRepository.findOne(id);
        UserConnectionDTO userConnectionDTO = userConnectionMapper.userConnectionToUserConnectionDTO(userConnection);
        return userConnectionDTO;
    }

    /**
     *  Delete the  userConnection by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete UserConnection : {}", id);
        userConnectionRepository.delete(id);
        userConnectionSearchRepository.delete(id);
    }

    /**
     * Search for the userConnection corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<UserConnectionDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of UserConnections for query {}", query);
        Page<UserConnection> result = userConnectionSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(userConnection -> userConnectionMapper.userConnectionToUserConnectionDTO(userConnection));
    }
}
