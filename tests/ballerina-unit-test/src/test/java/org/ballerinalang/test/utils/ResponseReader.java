/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.ballerinalang.mime.util.MimeConstants.UTF_8;

/**
 * Utility class for managing responses.
 */
public class ResponseReader {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseReader.class);

    /**
     * Get the response value from input stream.
     *
     * @param response carbon response
     * @return return value from  input stream as a string
     */
    public static String getReturnValue(HTTPCarbonMessage response) {
        Reader reader;
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        try {
            reader = new InputStreamReader(new HttpMessageDataStreamer(response).getInputStream(), UTF_8);
            while (true) {
                int size = reader.read(buffer, 0, buffer.length);
                if (size < 0) {
                    break;
                }
                out.append(buffer, 0, size);
            }
        } catch (IOException e) {
            LOG.error("Error occured while reading the response value in getReturnValue", e.getMessage());
        }
        return out.toString();
    }
}
