package org.ballerinalang.net.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.transport.http.netty.message.multipart.HttpBodyPart;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipartUtil {
    private static final Logger log = LoggerFactory.getLogger(MultipartUtil.class);

    private static final int ACTUAL_CONTENT = 0;
    private static final int FILE_NAME = 0;
    private static final int PART_NAME = 1;
    private static final int CONTENT_TYPE = 2;
    private static final int PART_SIZE = 0;

    public static List<HttpBodyPart> extractMultiparts(InputStream inputStream) {
        List<HttpBodyPart> multiparts = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            multiparts = (List<HttpBodyPart>) objectInputStream.readObject();
        } catch (IOException e) {
            log.error("IOException occurred while extracting multiparts from recieved inputstream", e);
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFoundException occurred while extracting multiparts from recieved inputstream", e);
        }
        return multiparts;
    }

    public static BRefValueArray fillPartsArray(Context context, List<HttpBodyPart> multiparts) {
        ArrayList<BStruct> parts = new ArrayList<>();
        for (HttpBodyPart httpBodyPart : multiparts) {
            BStruct partStruct = ConnectorUtils
                    .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_HTTP, Constants.PART);
            populatePartStructs(httpBodyPart, partStruct);
            parts.add(partStruct);
        }
        if (!parts.isEmpty()) {
            BStructType type = parts.get(0).getType();
            BStruct[] result = parts.toArray(new BStruct[parts.size()]);
            BRefValueArray partsArray = new BRefValueArray(result, type);
            return partsArray;
        }
        return null;
    }

    private static void populatePartStructs(HttpBodyPart httpBodyPart, BStruct balPart) {
        balPart.setBlobField(ACTUAL_CONTENT, httpBodyPart.getContent());
        balPart.setStringField(FILE_NAME, httpBodyPart.getFileName());
        balPart.setStringField(PART_NAME, httpBodyPart.getPartName());
        balPart.setStringField(CONTENT_TYPE, httpBodyPart.getContentType());
        balPart.setIntField(PART_SIZE, httpBodyPart.getSize());
    }


    public static class MultipartDataSource  implements MessageDataSource{
        private List<HttpBodyPart> bodyParts = null;

        MultipartDataSource(List<HttpBodyPart> bodyParts) {
            this.bodyParts = bodyParts;
        }

        public List<HttpBodyPart> getBodyParts() {
            return bodyParts;
        }

        @Override
        public String getValueAsString(String s) {
            return null;
        }

        @Override
        public String getValueAsString(String s, Map<String, String> map) {
            return null;
        }

        @Override
        public Object getValue(String s) {
            return null;
        }

        @Override
        public Object getDataObject() {
            return null;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public void setContentType(String s) {

        }

        @Override
        public void serializeData() {

        }

        @Override
        public String getMessageAsString() {
            return null;
        }
    }
}
