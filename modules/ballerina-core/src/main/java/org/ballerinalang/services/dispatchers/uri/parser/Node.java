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

import java.util.Map;

public abstract class Node {

    protected String token;
    protected Node next;

    protected Node(String token) {
        this.token = token;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public int matchAll(String uriFragment, Map<String, String> variables) {
        int matchLength = match(uriFragment, variables);
        if (matchLength < 0) {
            return -1;
        } else if (matchLength < uriFragment.length()) {
            if (next != null) {
                uriFragment = uriFragment.substring(matchLength);
                return matchLength + next.matchAll(uriFragment, variables);
            } else {
                // We have more content in the URI to match
                // But there aren't any nodes left to match against
                // return -1;
                if(uriFragment.endsWith("/")){
                    matchLength = matchLength +1;
                }
                return matchLength;
            }
        } else if (matchLength == uriFragment.length() && next != null) {
            if(next.getToken().equalsIgnoreCase("*"))
            {
                return matchLength;
            }
            if(next.getToken().equalsIgnoreCase("/*"))
            {
                return matchLength;
            }
            // We have matched all the characters in the URI
            // But there are some nodes left to be matched against
            return -1;
        }
        else {
            return matchLength;
        }
    }

    abstract String expand(Map<String,String> variables);
    abstract int match(String uriFragment, Map<String,String> variables);
    abstract String getToken();
    abstract char getFirstCharacter();
}
