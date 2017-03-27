package io.hobaskos.event.web.rest;

import io.hobaskos.event.BackendApp;

import io.hobaskos.event.domain.Device;
import io.hobaskos.event.domain.User;
import io.hobaskos.event.repository.DeviceRepository;
import io.hobaskos.event.service.DeviceService;
import io.hobaskos.event.repository.search.DeviceSearchRepository;
import io.hobaskos.event.service.dto.DeviceDTO;
import io.hobaskos.event.service.mapper.DeviceMapper;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hobaskos.event.domain.enumeration.DeviceType;
/**
 * Test class for the DeviceResource REST controller.
 *
 * @see DeviceResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApp.class)
public class DeviceResourceIntTest {

    private static final DeviceType DEFAULT_TYPE = DeviceType.IOS;
    private static final DeviceType UPDATED_TYPE = DeviceType.ANDROID;

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    @Inject
    private DeviceRepository deviceRepository;

    @Inject
    private DeviceMapper deviceMapper;

    @Inject
    private DeviceService deviceService;

    @Inject
    private DeviceSearchRepository deviceSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restDeviceMockMvc;

    private Device device;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DeviceResource deviceResource = new DeviceResource();
        ReflectionTestUtils.setField(deviceResource, "deviceService", deviceService);
        this.restDeviceMockMvc = MockMvcBuilders.standaloneSetup(deviceResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Device createEntity(EntityManager em) {
        Device device = new Device()
                .type(DEFAULT_TYPE)
                .token(DEFAULT_TOKEN);
        // Add required entity
        User user = UserResourceIntTest.createRandomEntity(em);
        em.persist(user);
        em.flush();
        device.setUser(user);
        return device;
    }

    @Before
    public void initTest() {
        deviceSearchRepository.deleteAll();
        device = createEntity(em);
    }

    @Test
    @Ignore
    @Transactional
    public void createDevice() throws Exception {
        int databaseSizeBeforeCreate = deviceRepository.findAll().size();

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.deviceToDeviceDTO(device);

        restDeviceMockMvc.perform(post("/api/devices")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDTO)))
            .andExpect(status().isCreated());

        // Validate the Device in the database
        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeCreate + 1);
        Device testDevice = deviceList.get(deviceList.size() - 1);
        assertThat(testDevice.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testDevice.getToken()).isEqualTo(DEFAULT_TOKEN);

        // Validate the Device in ElasticSearch
        Device deviceEs = deviceSearchRepository.findOne(testDevice.getId());
        assertThat(deviceEs).isEqualToComparingFieldByField(testDevice);
    }

    @Test
    @Ignore
    @Transactional
    public void createDeviceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = deviceRepository.findAll().size();

        // Create the Device with an existing ID
        Device existingDevice = new Device();
        existingDevice.setId(1L);
        DeviceDTO existingDeviceDTO = deviceMapper.deviceToDeviceDTO(existingDevice);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceMockMvc.perform(post("/api/devices")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingDeviceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Ignore
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = deviceRepository.findAll().size();
        // set the field null
        device.setType(null);

        // Create the Device, which fails.
        DeviceDTO deviceDTO = deviceMapper.deviceToDeviceDTO(device);

        restDeviceMockMvc.perform(post("/api/devices")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Ignore
    @Transactional
    public void checkTokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = deviceRepository.findAll().size();
        // set the field null
        device.setToken(null);

        // Create the Device, which fails.
        DeviceDTO deviceDTO = deviceMapper.deviceToDeviceDTO(device);

        restDeviceMockMvc.perform(post("/api/devices")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDevices() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);

        // Get all the deviceList
        restDeviceMockMvc.perform(get("/api/devices?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(device.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN.toString())));
    }

    @Test
    @Transactional
    public void getDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);

        // Get the device
        restDeviceMockMvc.perform(get("/api/devices/{id}", device.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(device.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDevice() throws Exception {
        // Get the device
        restDeviceMockMvc.perform(get("/api/devices/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Ignore
    @Transactional
    public void updateDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);
        deviceSearchRepository.save(device);
        int databaseSizeBeforeUpdate = deviceRepository.findAll().size();

        // Update the device
        Device updatedDevice = deviceRepository.findOne(device.getId());
        updatedDevice
                .type(UPDATED_TYPE)
                .token(UPDATED_TOKEN);
        DeviceDTO deviceDTO = deviceMapper.deviceToDeviceDTO(updatedDevice);

        restDeviceMockMvc.perform(put("/api/devices")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDTO)))
            .andExpect(status().isOk());

        // Validate the Device in the database
        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeUpdate);
        Device testDevice = deviceList.get(deviceList.size() - 1);
        assertThat(testDevice.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testDevice.getToken()).isEqualTo(UPDATED_TOKEN);

        // Validate the Device in ElasticSearch
        Device deviceEs = deviceSearchRepository.findOne(testDevice.getId());
        assertThat(deviceEs).isEqualToComparingFieldByField(testDevice);
    }

    @Test
    @Ignore
    @Transactional
    public void updateNonExistingDevice() throws Exception {
        int databaseSizeBeforeUpdate = deviceRepository.findAll().size();

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.deviceToDeviceDTO(device);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDeviceMockMvc.perform(put("/api/devices")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDTO)))
            .andExpect(status().isCreated());

        // Validate the Device in the database
        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);
        deviceSearchRepository.save(device);
        int databaseSizeBeforeDelete = deviceRepository.findAll().size();

        // Get the device
        restDeviceMockMvc.perform(delete("/api/devices/{id}", device.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean deviceExistsInEs = deviceSearchRepository.exists(device.getId());
        assertThat(deviceExistsInEs).isFalse();

        // Validate the database is empty
        List<Device> deviceList = deviceRepository.findAll();
        assertThat(deviceList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);
        deviceSearchRepository.save(device);

        // Search the device
        restDeviceMockMvc.perform(get("/api/_search/devices?query=id:" + device.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(device.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN.toString())));
    }
}
