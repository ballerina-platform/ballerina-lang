package org.ballerinalang.test.services.testutils;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.values.BStruct;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;

/**
 * Utility functions for interact with Ballerina mime Entity.
 */
public class TestEntityUtils {

    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Enriches the mime entity with the provided data.
     *
     * @param entity      mime entity to be enriched.
     * @param contentType content-type header value.
     * @param payload     mime entity payload.
     */
    public static void enrichTestEntity(BStruct entity, String contentType, String payload) {
        enrichTestEntityHeaders(entity, contentType);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
    }

    /**
     * Enriches entity with provided header.
     *
     * @param entity      mime entity to be enriched.
     * @param contentType content-type header value.
     */
    public static void enrichTestEntityHeaders(BStruct entity, String contentType) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(CONTENT_TYPE, contentType);
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
    }
}
