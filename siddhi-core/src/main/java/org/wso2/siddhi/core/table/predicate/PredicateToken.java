/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.table.predicate;

public class PredicateToken {

    public enum Type {
        VARIABLE, OPERATOR, VALUE;
    }

    private String tokenValue;
    private Type getTokenType;

    public PredicateToken() {
    }

    public PredicateToken(Type tokenType, String tokenValue) {
        this.getTokenType = tokenType;
        this.tokenValue = tokenValue;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public Type getGetTokenType() {
        return getTokenType;
    }

    public void setGetTokenType(Type getTokenType) {
        this.getTokenType = getTokenType;
    }
}
