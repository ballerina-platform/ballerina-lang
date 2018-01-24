/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.mime.util;

import io.netty.handler.codec.http.HttpRequest;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING_7_BIT;

/**
 * This class holds attributes required for creating a body part as a file upload.
 *
 * @since 0.96
 */
public class FileUploadContentHolder {

    private HttpRequest request; //Netty Http request
    private String bodyPartName; //Name for the body part
    private String fileName; //File name to be used for file upload
    private String contentType; //Content-Type of the actual content
    private String contentTransferEncoding; //What sort of encoding transformation the body was subjected to
    private Charset charset; //Charset used in the content
    private long size; //Size of the file upload
    private InputStream inputStream; //When the content is given as an input stream
    private File file; //When the content is given as a file
    private Constants.BodyPartForm bodyPartFormat; //Type of the body part

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public String getBodyPartName() {
        return bodyPartName;
    }

    public void setBodyPartName(String bodyPartName) {
        this.bodyPartName = bodyPartName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getContentTransferEncoding() {
        if (this.contentTransferEncoding == null) {
            return CONTENT_TRANSFER_ENCODING_7_BIT;
        }
        return contentTransferEncoding;
    }

    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Constants.BodyPartForm getBodyPartFormat() {
        return bodyPartFormat;
    }

    public void setBodyPartFormat(Constants.BodyPartForm bodyPartFormat) {
        this.bodyPartFormat = bodyPartFormat;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
