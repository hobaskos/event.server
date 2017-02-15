package io.hobaskos.event.exception;


/**
 * Copied from:
 * https://github.com/spring-guides/gs-uploading-files/blob/master/initial/src/main/java/hello/storage/StorageFileNotFoundException.java
 */
public class StorageFileNotFoundException extends StorageException {

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
