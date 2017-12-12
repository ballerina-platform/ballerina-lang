package org.ballerinalang.mime.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXML;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MimeUtil {

    public static final int TEXT_DATA = 1;
    public static final int JSON_DATA = 2;
    public static final int XML_DATA = 3;
    public static final int BYTE_DATA = 0;
    public static final int SIZE = 0;
    public static final int OVERFLOW_DATA = 4;
    public static final int TEMP_FILE_PATH = 0;

    public static void setStringPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 1) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
        } else {
            String payload = StringUtils.getStringFromInputStream(inputStream);
            entityStruct.setStringField(TEXT_DATA, payload);
        }
    }

    public static void setJsonPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
        } else {
            BJSON payload = new BJSON(inputStream);
            entityStruct.setRefField(JSON_DATA, payload);
        }
    }

    public static void setXmlPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
        } else {
            BXML payload = XMLUtils.parse(inputStream);
            entityStruct.setRefField(XML_DATA, payload);
        }
    }

    public static void setBinaryPayload(Context context, BStruct entityStruct, InputStream inputStream,
            int contentLength) {
        if (contentLength > 30) {
            writeToTemporaryFile(inputStream);
            createBallerinaFileHandler(context, entityStruct);
        } else {
            BBlob payload = null;
            try {
                payload = new BBlob(toByteArray(inputStream));
            } catch (IOException e) {
                e.printStackTrace();
            }
            entityStruct.setRefField(BYTE_DATA, payload);
        }
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
            e.printStackTrace();
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
