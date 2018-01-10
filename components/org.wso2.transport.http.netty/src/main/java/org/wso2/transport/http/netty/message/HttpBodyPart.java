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

package org.wso2.transport.http.netty.message;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one body part of a multipart message.
 */
public class HttpBodyPart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] content;
    private final String contentType;
    private final String partName;
    private final String fileName;
    private final int size;
    private Map<String, Object> headers = new HashMap<>();

    public HttpBodyPart(String partName, byte[] content, String contentType, int size) {
        this(partName, null, content, contentType, size);
    }

    public HttpBodyPart(String partName, String fileName, byte[] content, String contentType, int size) {
        this.partName = partName;
        this.fileName = fileName;
        this.content = Arrays.copyOf(content, content.length);
        this.contentType = contentType;
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }

    public String getPartName() {
        return partName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getSize() {
        return size;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
