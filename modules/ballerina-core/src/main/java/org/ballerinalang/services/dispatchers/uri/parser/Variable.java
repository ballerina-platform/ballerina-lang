/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.services.dispatchers.uri.parser;


import org.ballerinalang.services.dispatchers.uri.URITemplateException;

public class Variable {

    private String name;
    private int prefix = -1;

    public Variable(String value) throws URITemplateException {
        this.name = value;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!isValid(value.charAt(i)) && ch != ':') {
                throw new URITemplateException("Invalid character: '" + ch + "' in expression");
            } else if (ch == ':') {
                this.name = value.substring(0, i);
                this.prefix = Integer.parseInt(value.substring(i + 1));
                if (prefix <= 0) {
                    throw new URITemplateException("Invalid variable prefix: " + prefix);
                }
                break;
            }
        }
    }

    private boolean isValid(char ch) {
        return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
                (ch >= '0' && ch <= '9') || ch == '.'  || ch == '-' || ch == '_');
    }

    public String getName() {
        return name;
    }

    public String modify(String value) {
        if (prefix > 0 && prefix < value.length()) {
            return value.substring(0, prefix);
        }
        return value;
    }

    public boolean checkModifier(String value) {
        if (prefix > 0) {
            return value.length() == prefix;
        }
        return true;
    }
}
