/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;

/**
 * Wrapper class to hold request and response filter functions.
 */
public class FilterHolder {
    private FunctionRefCPEntry requestFilterFunction;
    private FunctionRefCPEntry responseFilterFunction;

    public FilterHolder(FunctionRefCPEntry requestFilterFunction, FunctionRefCPEntry responseFilterFunction) {
        this.requestFilterFunction = requestFilterFunction;
        this.responseFilterFunction = responseFilterFunction;
    }

    public FunctionRefCPEntry getRequestFilterFunction() {
        return requestFilterFunction;
    }

    public FunctionRefCPEntry getResponseFilterFunction() {
        return responseFilterFunction;
    }
}
