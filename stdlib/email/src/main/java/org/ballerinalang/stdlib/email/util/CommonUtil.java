/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.email.util;

import org.ballerinalang.mime.util.MimeConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Contains the common utility functions.
 *
 * @since 1.2.1
 */
public class CommonUtil {

    /**
     * Check whether the content type is based on text.
     *
     * @param contentType Content Type of a MIME Body Type
     * @return boolean Whether the MIME Body Type is text based
     */
    protected static boolean isTextBased(String contentType) {
        return contentType.startsWith(MimeConstants.TEXT_AS_PRIMARY_TYPE)
                || contentType.endsWith(MimeConstants.XML_SUFFIX)
                || contentType.endsWith(MimeConstants.JSON_SUFFIX)
                || contentType.startsWith(MimeConstants.APPLICATION_JSON)
                || contentType.startsWith(MimeConstants.APPLICATION_XML)
                || contentType.startsWith(MimeConstants.APPLICATION_FORM);
    }

    /**
     * Check whether the content type is based on JSON.
     *
     * @param contentType Content Type of a MIME Body Type
     * @return boolean Whether the MIME Body Type is JSON based
     */
    protected static boolean isJsonBased(String contentType) {
        return contentType.contains("json");
    }

    /**
     * Check whether the content type is based on XML.
     *
     * @param contentType Content Type of a MIME Body Type
     * @return boolean Whether the MIME Body Type is XML based
     */
    protected static boolean isXmlBased(String contentType) {
        return contentType.contains("xml");
    }

    /**
     * Convert an InputStream to a byte array.
     *
     * @param inputStream InputStream input
     * @return byte[] Whether the MIME Body Type is text based
     * @throws IOException If an error occurs during reading the InputStream
     */
    public static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
