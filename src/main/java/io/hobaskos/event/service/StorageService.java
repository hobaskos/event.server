package io.hobaskos.event.service;

import org.springframework.core.io.Resource;

import java.nio.file.Path;

public interface StorageService {

    void init();

    String store(byte[] bytes, String suffix);

    Path load(String filename);

    Resource loadAsResource(String filename);
}
