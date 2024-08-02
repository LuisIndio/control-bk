package com.register.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("storage")
@Data
@Component
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "src/main/resources/static/";


}

