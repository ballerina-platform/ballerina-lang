/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerina.core.model.values;

/**
 * {@link BException} represents a exception value in Ballerina.
 */
public class BException implements BRefType {

    private BString category;
    private BString message;
    private BArray<BString> stackTrace;

    public BException() {
        stackTrace = new BArray<>(BString.class);
    }

    public BException(String message) {
        this.message = new BString(message);
    }

    public BException(String message, String category) {
        this.message = new BString(message);
        this.category = new BString(category);
    }

    @Override
    public String stringValue() {
        String stringValue = "{" + category.stringValue() + "}" + message.stringValue();
        return stringValue;
    }

    @Override
    public BException value() {
        return this;
    }

    public BString getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = new BString(category);
    }

    public BString getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = new BString(message);
    }

    public void addStackTrace(String trace) {
        stackTrace.add(stackTrace.size(), new BString(trace));
    }

    public BArray<BString> getStackTrace() {
        return stackTrace;
    }
}
