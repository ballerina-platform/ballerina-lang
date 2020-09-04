/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.datamapper.utils;

/**
 * This class is a simple representation of an HTTP response.
 */
public class HttpResponse {
    private String data;
    private int responseCode;

    /**
     * This method set data and response code of a HTTP request response.
     * @param data {@link String}
     * @param responseCode {@link int}
     */
    public HttpResponse(String data, int responseCode) {
        this.data = data;
        this.responseCode = responseCode;
    }

    /**
     * This method returns data of HTTP request response.
     * @return data {@link String}
     */
    public String getData() {
        return data;
    }

    /**
     * This method returns response code of HTTP request.
     * @return responseCode {@link int}
     */
    public int getResponseCode() {
        return responseCode;
    }
}

