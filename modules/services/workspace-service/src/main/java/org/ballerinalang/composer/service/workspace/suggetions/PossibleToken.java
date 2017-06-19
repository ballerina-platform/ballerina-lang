/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.suggetions;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Represents a possible token for current position
 */
public class PossibleToken {

    private int tokenType;

    private int lastTokenIndex;

    private String tokenName;

    private ParserRuleContext currentContext;

    public PossibleToken (int tokenType, String tokenName, ParserRuleContext currentContext) {
        this.tokenType = tokenType;
        this.tokenName = tokenName;
        this.currentContext = currentContext;
    }

    public PossibleToken() {
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public int getTokenType() {
        return tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public ParserRuleContext getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(ParserRuleContext currentContext) {
        this.currentContext = currentContext;
    }

    public int getLastTokenIndex() {
        return lastTokenIndex;
    }

    public void setLastTokenIndex(int lastTokenIndex) {
        this.lastTokenIndex = lastTokenIndex;
    }
}
