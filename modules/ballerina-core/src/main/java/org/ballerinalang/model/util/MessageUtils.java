/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.util;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Common Utils methods used in Message.
 */
public class MessageUtils {

    /**
     * Convert input stream to String.
     *
     * @param in Message payload as an input stream
     * @return Message payload as string
     */
    public static String getStringFromInputStream(InputStream in) {
        StringBuilder sb = new StringBuilder(4096);
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
        } catch (IOException ioe) {
            throw new BallerinaException(ioe.getMessage(), ioe);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // Do nothing.
            }
            try {
                reader.close();
            } catch (IOException e) {
                // Do nothing.
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // Do nothing.
            }
        }
        return sb.toString();
    }

    /**
     * This method will return new copy of BValue.
     *
     * @param orgBValue The original BValue instance
     * @return Duplicate BValue instance
     */
//    public static BValue getBValueCopy(BValue orgBValue) {
//        BValue bValue;
//        if (orgBValue instanceof StringValue) {
//            String value = (String) orgBValue.getValue();
//            bValue = new StringValue(value);
//        } else if (orgBValue instanceof IntValue) {
//            Integer value = (Integer) orgBValue.getValue();
//            bValue = new IntValue(value);
//        } else if (orgBValue instanceof LongType) {
//            Long value = (Long) orgBValue.getValue();
//            bValue = new LongValue(value);
//        } else if (orgBValue instanceof FloatValue) {
//            Float value = (Float) orgBValue.getValue();
//            bValue = new FloatValue(value);
//        } else if (orgBValue instanceof DoubleValue) {
//            Double value = (Double) orgBValue.getValue();
//            bValue = new DoubleValue(value);
//        } else if (orgBValue instanceof BooleanValue) {
//            Boolean value = (Boolean) orgBValue.getValue();
//            bValue = new BooleanValue(value);
//        } else if (orgBValue instanceof JSONValue) {
//            JsonElement value = (JsonElement) orgBValue.getValue();
//            bValue = new JSONValue(value);
//        } else if (orgBValue instanceof MessageValue) {
//            CarbonMessage value = (CarbonMessage) orgBValue.getValue();
//            bValue = new MessageValue(value);
//        } else {
//            throw new BallerinaException("Unsupported orgBValue: " + orgBValue.getValue());
//        }
//        return bValue;
//    }
}
