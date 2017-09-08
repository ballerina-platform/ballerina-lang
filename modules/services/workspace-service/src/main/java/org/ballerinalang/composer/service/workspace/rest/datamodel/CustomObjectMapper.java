package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * CustomObjectMapper
 */
public class CustomObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 1L;

    public CustomObjectMapper() {
        registerModule(new BLangModule());
        enable(SerializationFeature.INDENT_OUTPUT);
    }
}
