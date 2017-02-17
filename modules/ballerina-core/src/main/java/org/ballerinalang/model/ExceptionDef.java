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
package org.ballerinalang.model;

import org.ballerinalang.model.values.BString;

/**
 * Ballerina Exception Definition.
 */
public class ExceptionDef implements CompilationUnit {

    private BString category;
    private BString message;
    private ExceptionDef cause;
    private String stackTrace;


    public ExceptionDef() {
        category = new BString("");
        message = new BString("");
        stackTrace = "";
    }

    public ExceptionDef(String message) {
        category = new BString("");
        this.message = new BString(message);
        stackTrace = "";
    }

    public ExceptionDef(String message, String category) {
        this.message = new BString(message);
        this.category = new BString(category);
        stackTrace = "";
    }

    @Override
    public void accept(NodeVisitor visitor) {
    }

    @Override
    public NodeLocation getNodeLocation() {
        return null;
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

    public ExceptionDef getCause() {
        return cause;
    }

    public void setCause(ExceptionDef cause) {
        this.cause = cause;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {
        return "exception " + (category.stringValue().equals("") ? "" : category.stringValue() + ": ") +
                message.stringValue();
    }
}
