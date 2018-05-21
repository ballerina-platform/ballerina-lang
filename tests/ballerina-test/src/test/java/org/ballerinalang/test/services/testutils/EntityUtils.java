package org.ballerinalang.test.services.testutils;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BStruct;

import static org.ballerinalang.mime.util.Constants.BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;

/**
 * Utility functions for interact with Ballerina mime Entity.
 */
public class EntityUtils {

    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Enriches the mime entity with the provided data.
     *
     * @param entity      mime entity to be enriched.
     * @param mediaType   mediaType struct.
     * @param contentType content-type header value.
     * @param payload     mime entity payload.
     */
    public static void enrichTestEntity(BStruct entity, BStruct mediaType, String contentType, String payload) {
        MimeUtil.setContentType(mediaType, entity, contentType);
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

    /**
     * Enriches mime entity with provided content. However the content-type of the mediaType and content-type header
     * is different. payload will be set to ENTITY_BYTE_CHANNEL.
     *
     * @param entity            mime entity to be enriched.
     * @param mediaType         mediaType struct.
     * @param mediaContentType  content-type value of the mediaType.
     * @param headerContentType content-type header value.
     * @param payload           mime entity payload.
     */
    public static void enrichTestEntityWithDifferentHeaderAndByteChannel(BStruct entity, BStruct mediaType,
                           String mediaContentType, String headerContentType, String payload) {
        MimeUtil.setContentType(mediaType, entity, mediaContentType);
        enrichTestEntityHeaders(entity, headerContentType);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
    }

    /**
     * Enriches mime entity with provided content. However the content-type of the mediaType and content-type header
     * is different. payload will be set to BYTE_CHANNEL_STRUCT.
     *
     * @param entity            mime entity to be enriched.
     * @param mediaType         mediaType struct.
     * @param mediaContentType  content-type value of the mediaType.
     * @param headerContentType content-type header value.
     * @param payload           mime entity payload.
     */
    public static void enrichTestEntityWithDifferentHeaderAndByteChannelStruct(BStruct entity, BStruct mediaType,
                           String mediaContentType, String headerContentType, String payload) {
        MimeUtil.setContentType(mediaType, entity, mediaContentType);
        enrichTestEntityHeaders(entity, headerContentType);
        entity.addNativeData(BYTE_CHANNEL_STRUCT, EntityBodyHandler.getEntityWrapper(payload));
    }
}
