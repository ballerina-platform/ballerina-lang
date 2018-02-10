package org.ballerinalang.mime.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.jvnet.mimepull.MIMEConfig;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.BOUNDARY;
import static org.ballerinalang.mime.util.Constants.BYTE_LIMIT;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.Constants.CONTENT_LENGTH;
import static org.ballerinalang.mime.util.Constants.ENTITY;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.FIRST_ELEMENT;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;

public class MultipartDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartDecoder.class);

    /**
     * Decode multiparts from a given input stream.
     *
     * @param context     Represent ballerina context
     * @param entity      Represent ballerina entity which needs to be populated with body parts
     * @param contentType Content-Type of the top level message
     * @param inputStream Represent input stream coming from the request/response
     */
    public static void parseBody(Context context, BStruct entity, String contentType, InputStream inputStream) {
        try {
            List<MIMEPart> mimeParts = decodeBodyParts(contentType, inputStream);
            if (mimeParts != null && !mimeParts.isEmpty()) {
                populateBallerinaParts(context, entity, mimeParts);
            }
        } catch (MimeTypeParseException e) {
            LOG.error("Error occured while decoding body parts from inputstream", e.getMessage());
        }
    }

    public static List<MIMEPart> decodeBodyParts(String contentType, InputStream inputStream) throws MimeTypeParseException {
        MimeType mimeType = new MimeType(contentType);
        final MIMEMessage mimeMessage = new MIMEMessage(inputStream,
                mimeType.getParameter(BOUNDARY),
                getMimeConfig());
        return mimeMessage.getAttachments();
    }

    /**
     * Create mime configuration with the maximum memory limit.
     *
     * @return MIMEConfig which defines configuration for MIME message parsing and storing
     */
    private static MIMEConfig getMimeConfig() {
        MIMEConfig mimeConfig = new MIMEConfig();
        mimeConfig.setMemoryThreshold(BYTE_LIMIT);
        return mimeConfig;
    }

    /**
     * Populate ballerina body parts from the given mime parts and set it to top level entity.
     *
     * @param context   Represent ballerina context
     * @param entity    Represent top level entity that the body parts needs to be attached to
     * @param mimeParts List of decoded mime parts
     */
    private static void populateBallerinaParts(Context context, BStruct entity, List<MIMEPart> mimeParts) {
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        for (final MIMEPart mimePart : mimeParts) {
            BStruct partStruct = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, ENTITY);
            BStruct mediaType = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);
            partStruct.setRefField(ENTITY_HEADERS_INDEX, HeaderUtil.setBodyPartHeaders(mimePart.getAllHeaders(), new BMap<>()));
            List<String> lengthHeaders = mimePart.getHeader(CONTENT_LENGTH);
            if (HeaderUtil.isHeaderExist(lengthHeaders)) {
                HeaderUtil.setContentLength(partStruct, Integer.parseInt(lengthHeaders.get(FIRST_ELEMENT)));
            } else {
                HeaderUtil.setContentLength(partStruct, NO_CONTENT_LENGTH_FOUND);
            }
            MimeUtil.setContentType(mediaType, partStruct, mimePart.getContentType());
            List<String> contentDispositionHeaders = mimePart.getHeader(CONTENT_DISPOSITION);
            if (HeaderUtil.isHeaderExist(contentDispositionHeaders)) {
                BStruct contentDisposition = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME,
                        CONTENT_DISPOSITION_STRUCT);
                HeaderUtil.setContentDisposition(contentDisposition, partStruct, contentDispositionHeaders.get(FIRST_ELEMENT));
            }
            MimeUtil.populateBodyContent(context, partStruct, mimePart);
            bodyParts.add(partStruct);
        }
        MimeUtil.setPartsToTopLevelEntity(entity, bodyParts);
    }
}
