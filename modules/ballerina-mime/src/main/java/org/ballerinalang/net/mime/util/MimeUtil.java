package org.ballerinalang.net.mime.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.net.mime.util.Constants.BYTE_DATA_INDEX;
import static org.ballerinalang.net.mime.util.Constants.FALSE;
import static org.ballerinalang.net.mime.util.Constants.IS_IN_MEMORY_INDEX;
import static org.ballerinalang.net.mime.util.Constants.JSON_DATA_INDEX;
import static org.ballerinalang.net.mime.util.Constants.MEDIA_TYPE_INDEX;
import static org.ballerinalang.net.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.net.mime.util.Constants.PARAMETER_MAP_INDEX;
import static org.ballerinalang.net.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.net.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.net.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.net.mime.util.Constants.SUFFIX_INDEX;
import static org.ballerinalang.net.mime.util.Constants.TEMP_FILE_PATH_INDEX;
import static org.ballerinalang.net.mime.util.Constants.TEXT_DATA_INDEX;
import static org.ballerinalang.net.mime.util.Constants.TRUE;
import static org.ballerinalang.net.mime.util.Constants.XML_DATA_INDEX;

/**
 * Entity related operations and mime utility functions have been included here.
 */
public class MimeUtil {

    public static void setStringPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 1) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            String payload = StringUtils.getStringFromInputStream(inputStream);
            entityStruct.setStringField(TEXT_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    public static void setJsonPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            BJSON payload = new BJSON(inputStream);
            entityStruct.setRefField(JSON_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    public static void setXmlPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            BXML payload = XMLUtils.parse(inputStream);
            entityStruct.setRefField(XML_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    public static void setBinaryPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, FALSE);
        } else {
            BBlob payload = null;
            try {
                payload = new BBlob(toByteArray(inputStream));
            } catch (IOException e) {
                throw new BallerinaException("Error while converting inputstream to a byte array: " + e.getMessage());
            }
            entityStruct.setRefField(BYTE_DATA_INDEX, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY_INDEX, TRUE);
        }
    }

    public static void setContentType(Context context, BStruct entityStruct, String contentType) {
        BStruct mediaType = parseMediaType(context, contentType);
        if (contentType == null) {
            mediaType.setStringField(PRIMARY_TYPE_INDEX, Constants.DEFAULT_PRIMARY_TYPE);
            mediaType.setStringField(SUBTYPE_INDEX, Constants.DEFAULT_SUB_TYPE);
        }
        entityStruct.setRefField(MEDIA_TYPE_INDEX, mediaType);
    }

    public static String getBaseType(String contentType) {
        try {
            MimeType mimeType = new MimeType(contentType);
            return mimeType.getBaseType();
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
    }

    public static BStruct parseMediaType(Context context, String contentType) {
        BStruct mediaType = ConnectorUtils
                .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_MIME, Constants.MEDIA_TYPE);
        MimeType mimeType = null;
        try {
            mimeType = new MimeType(contentType);
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
        mediaType.setStringField(PRIMARY_TYPE_INDEX, mimeType.getPrimaryType());
        mediaType.setStringField(SUBTYPE_INDEX, mimeType.getSubType());
        if (mimeType.getSubType() != null && mimeType.getSubType().contains(Constants.SUFFIX_ATTACHMENT)) {
            mediaType.setStringField(SUFFIX_INDEX, mimeType.getSubType()
                    .substring(mimeType.getSubType().lastIndexOf(Constants.SUFFIX_ATTACHMENT) + 1));
        }
        MimeTypeParameterList parameterList = mimeType.getParameters();
        Enumeration keys = parameterList.getNames();
        BMap<String, BValue> parameterMap = new BMap<>();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = parameterList.get(key);
            parameterMap.put(key, new BString(value));
        }
        mediaType.setRefField(PARAMETER_MAP_INDEX, parameterMap);
        return mediaType;
    }

    public static void setContentLength(BStruct entityStruct, int length) {
        entityStruct.setIntField(SIZE_INDEX, length);
    }

    private static BStruct createBallerinaFileHandler(Context context, BStruct entityStruct) {
        BStruct fileStruct = ConnectorUtils
                .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_FILE, Constants.FILE);
        fileStruct.setStringField(TEMP_FILE_PATH_INDEX, "/home/rukshani/BallerinaWork/multipart/MIME/temp.tmp");
        entityStruct.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
        return entityStruct;
    }

    private static void writeToTemporaryFile(InputStream inputStream) {
        try {
            OutputStream os = new FileOutputStream("/home/rukshani/BallerinaWork/multipart/MIME/temp.tmp");
            byte[] buffer = new byte[1024];
            int bytesRead;
            //read from inputstream to buffer
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            //flush OutputStream to write any buffered data to file
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new BallerinaException("Error while writing the payload info into a temp file: " + e.getMessage());
        }
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        int n1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (; -1 != (n1 = input.read(buffer)); ) {
            output.write(buffer, 0, n1);
        }
        byte[] bytes = output.toByteArray();
        output.close();
        return bytes;
    }
}
