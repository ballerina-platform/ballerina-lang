/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.config;

/**
 * Configuration for the request size validation.
 */
public class RequestSizeValidationConfig {

    private int uriMaxSize = 4096;
    private int headerMaxSize = 8192;
    private int maxChunkSize = 8192;
    private int entityBodyMaxSize = -1;

    public int getUriMaxSize() {
        return uriMaxSize;
    }

    public void setUriMaxSize(int uriMaxSize) {
        this.uriMaxSize = uriMaxSize;
    }

    public int getHeaderMaxSize() {
        return headerMaxSize;
    }

    public void setHeaderMaxSize(int headerMaxSize) {
        this.headerMaxSize = headerMaxSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public int getEntityBodyMaxSize() {
        return entityBodyMaxSize;
    }

    public void setEntityBodyMaxSize(int entityBodyMaxSize) {
        this.entityBodyMaxSize = entityBodyMaxSize;
    }
}
