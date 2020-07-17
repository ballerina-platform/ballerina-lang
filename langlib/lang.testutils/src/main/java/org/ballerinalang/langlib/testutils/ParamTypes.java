/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.ballerinalang.langlib.testutils;

/**
 * ParamTypes Class holds user given parameter info.
 *
 * @since 2.0.0
 */


public class ParamTypes {
    private String type;
    private String elementType;
    private String elementValue;

    public ParamTypes(String type, String elementType, String elementValue) {
        this.type = type;
        this.elementType = elementType;
        this.elementValue = elementValue;
    }

    public String getType() {
        return this.type;
    }

    public String getElementType() {
        return this.elementType;
    }

    public String getElementValue() {
        return this.elementValue;
    }

    public boolean isStructured() {
        return !type.equals(elementType);
    }
}
