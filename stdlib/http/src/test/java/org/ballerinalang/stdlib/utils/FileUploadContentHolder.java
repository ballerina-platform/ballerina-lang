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

package org.ballerinalang.stdlib.utils;

import io.netty.handler.codec.http.HttpRequest;
import org.ballerinalang.mime.util.MimeConstants;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_TRANSFER_ENCODING_7_BIT;

/**
 * This class holds attributes required for creating a body part as a file upload for testing purpose.
 *
 * @since 0.96
 */
public class FileUploadContentHolder {

    private HttpRequest request;
    private String bodyPartName;
    private String fileName;
    private String contentType;
    private String contentTransferEncoding;
    private Charset charset;
    private long fileSize;
    private InputStream contentStream;
    private File file; //When the content is given as a file
    private MimeConstants.BodyPartForm bodyPartFormat; //Type of the body part

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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long size) {
        this.fileSize = size;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream inputStream) {
        this.contentStream = inputStream;
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

    public MimeConstants.BodyPartForm getBodyPartFormat() {
        return bodyPartFormat;
    }

    public void setBodyPartFormat(MimeConstants.BodyPartForm bodyPartFormat) {
        this.bodyPartFormat = bodyPartFormat;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
