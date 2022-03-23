/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.util;

import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Common utility methods used for String manipulation.
 * 
 * @since 0.95.3
 */
public class StringUtils {

    /**
     * Check whether two strings are equal in value.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return flag indicating whether the two string values are equal
     */
    public static boolean isEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        } else if (s1 == null || s2 == null) {
            return false;
        } else {
            return s1.equals(s2);
        }
    }

    /**
     * Convert input stream to String.
     *
     * @param in Input stream to be converted to string
     * @return Converted string
     */
    public static String getStringFromInputStream(InputStream in) {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String result;
        try {
            int data;
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
            result = bos.toString();
        } catch (IOException ioe) {
            throw new BallerinaException("Error occurred when reading input stream", ioe);
        } finally {
            try {
                bos.close();
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    public static String getStringFromInputStream(InputStream inputStream, String charset) {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new InputStreamReader(inputStream, Charset.forName(charset))) {
            int character;
            while ((character = reader.read()) != -1) {
                textBuilder.append((char) character);
            }
        } catch (IOException e) {
            throw new BallerinaException("Error occurred when reading input stream with the charset" + charset, e);
        }
        return textBuilder.toString();
    }
}
