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
package org.ballerinalang.net.grpc.builder.components;

import java.util.Arrays;

import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.EMPTY_STRING;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PACKAGE_SEPARATOR_REGEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.PARENT_SPLITTING_INDEX;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.QUERY_PARAMETER;

/**
 * Parameter definition class.
 *
 * @since 1.2.0
 */
public class Param {
    private String fieldName;
    private String parentName;
    private String fieldType;
    private String paramSource;
    private boolean repeatedParam;
    private String queryAccessName;

    public Param(String fieldName, String parentName, String fieldType, String paramSource, boolean repeatedParam) {
        this.fieldName = fieldName;
        this.parentName = getLastParentName(parentName);
        this.fieldType = fieldType;
        this.paramSource = paramSource;
        this.repeatedParam = repeatedParam;
        this.queryAccessName = getAccessName(parentName, paramSource);
    }

    private String getLastParentName(String parentName) {
        String [] parentNames = parentName.split(PACKAGE_SEPARATOR_REGEX);
        //the length of parentNames is always greater than zero
        return parentNames[parentNames.length - 1];
    }

    private String getAccessName(String parentName, String paramSource) {
        if (paramSource.equals(QUERY_PARAMETER)) {
            String [] parentNames = parentName.split(PACKAGE_SEPARATOR_REGEX);
            if (parentNames.length > PARENT_SPLITTING_INDEX) {
                String[] bodyAccessNames = Arrays.copyOfRange(parentNames, PARENT_SPLITTING_INDEX, parentNames.length);
                return String.join(PACKAGE_SEPARATOR, bodyAccessNames) + PACKAGE_SEPARATOR;
            }
        }
        return EMPTY_STRING;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getParamSource() {
        return paramSource;
    }

    public String getParentName() {
        return parentName;
    }

    public boolean getRepeatedParam() {
        return repeatedParam;
    }

    public String getQueryAccessName() {
        return queryAccessName;
    }
}
