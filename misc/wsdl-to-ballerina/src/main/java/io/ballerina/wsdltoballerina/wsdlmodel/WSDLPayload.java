/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.wsdltoballerina.wsdlmodel;

import java.util.ArrayList;
import java.util.List;

public class WSDLPayload {
    private String name;
    private List<WSDLHeader> headers;
    private WSDLMessage message;

    public WSDLPayload() {
        this.headers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WSDLHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<WSDLHeader> headers) {
        this.headers = headers;
    }

    public void addHeader(WSDLHeader header) {
        this.headers.add(header);
    }

    public WSDLMessage getMessage() {
        return message;
    }

    public void setMessage(WSDLMessage message) {
        this.message = message;
    }
}
