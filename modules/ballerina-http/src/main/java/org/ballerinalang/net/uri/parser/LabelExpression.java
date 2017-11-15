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

package org.ballerinalang.net.uri.parser;


import org.ballerinalang.net.uri.URITemplateException;

import java.util.Map;

/**
 * LabelExpression represents path segments that start with ..
 *
 * @param <DataElementType> Specific data element created by the user.
 */
public class LabelExpression<DataElementType extends DataElement> extends SimpleStringExpression<DataElementType> {

    public LabelExpression(DataElementType dataElement, String token) throws URITemplateException {
        super(dataElement, token);
    }

    @Override
    public String expand(Map<String, String> variables) {
        String result = super.expand(variables);
        if (result != null) {
            return getSeparator() + result;
        }
        return result;
    }

    @Override
    public int match(String uriFragment, Map<String, String> variables) {
        if (uriFragment.startsWith(String.valueOf(getSeparator()))) {
            return super.match(uriFragment.substring(1), variables) + 1;
        }
        return 0;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }

    @Override
    public char getFirstCharacter() {
        return getSeparator();
    }
}
