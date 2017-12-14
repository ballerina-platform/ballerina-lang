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

/**
 * Entity related operations and mime utility functions have been included here.
 */
public class MimeUtil {

    //Entity properties
    public static final int TEXT_DATA = 1;
    public static final int JSON_DATA = 2;
    public static final int XML_DATA = 3;
    public static final int BYTE_DATA = 0;
    public static final int SIZE = 0;
    public static final int OVERFLOW_DATA = 4;
    public static final int TEMP_FILE_PATH = 0;
    public static final int IS_IN_MEMORY = 0;
    public static final int MEDIA_TYPE = 0;

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    //Media type properties
    public static final int PRIMARY_TYPE = 0;
    public static final int SUBTYPE = 1;
    public static final int SUFFIX = 2;
    public static final int PARAMETER_MAP = 0;

    public static void setStringPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 1) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY, FALSE);
        } else {
            String payload = StringUtils.getStringFromInputStream(inputStream);
            entityStruct.setStringField(TEXT_DATA, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY, TRUE);
        }
    }

    public static void setJsonPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY, FALSE);
        } else {
            BJSON payload = new BJSON(inputStream);
            entityStruct.setRefField(JSON_DATA, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY, TRUE);
        }
    }

    public static void setXmlPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY, FALSE);
        } else {
            BXML payload = XMLUtils.parse(inputStream);
            entityStruct.setRefField(XML_DATA, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY, TRUE);
        }
    }

    public static void setBinaryPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
            entityStruct.setBooleanField(IS_IN_MEMORY, FALSE);
        } else {
            BBlob payload = null;
            try {
                payload = new BBlob(toByteArray(inputStream));
            } catch (IOException e) {
                throw new BallerinaException("Error while converting inputstream to a byte array: " + e.getMessage());
            }
            entityStruct.setRefField(BYTE_DATA, payload);
            entityStruct.setBooleanField(IS_IN_MEMORY, TRUE);
        }
    }

    public static void setContentType(Context context, BStruct entityStruct, String contentType) {
        BStruct mediaType = parseMediaType(context, contentType);
        if (contentType == null) {
            mediaType.setStringField(PRIMARY_TYPE, Constants.DEFAULT_PRIMARY_TYPE);
            mediaType.setStringField(SUBTYPE, Constants.DEFAULT_SUB_TYPE);
        }
        entityStruct.setRefField(MEDIA_TYPE, mediaType);
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
        mediaType.setStringField(PRIMARY_TYPE, mimeType.getPrimaryType());
        mediaType.setStringField(SUBTYPE, mimeType.getSubType());
        if (mimeType.getSubType() != null && mimeType.getSubType().contains(Constants.SUFFIX)) {
            mediaType.setStringField(SUFFIX,
                    mimeType.getSubType().substring(mimeType.getSubType().lastIndexOf(Constants.SUFFIX) + 1));
        }
        MimeTypeParameterList parameterList = mimeType.getParameters();
        Enumeration keys = parameterList.getNames();
        BMap<String, BValue> parameterMap = new BMap<>();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = parameterList.get(key);
            parameterMap.put(key, new BString(value));
        }
        mediaType.setRefField(PARAMETER_MAP, parameterMap);
        return mediaType;
    }

    public static void setContentLength(BStruct entityStruct, int length) {
        entityStruct.setIntField(SIZE, length);
    }

    private static BStruct createBallerinaFileHandler(Context context, BStruct entityStruct) {
        BStruct fileStruct = ConnectorUtils
                .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_FILE, Constants.FILE);
        fileStruct.setStringField(TEMP_FILE_PATH, "/home/rukshani/BallerinaWork/multipart/MIME/temp.tmp");
        entityStruct.setRefField(OVERFLOW_DATA, fileStruct);
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
