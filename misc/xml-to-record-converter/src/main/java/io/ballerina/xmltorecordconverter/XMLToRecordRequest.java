/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.xmltorecordconverter;

/**
 * Request format for XMLToRecord endpoint.
 *
 * @since 2201.7.2
 */
public class XMLToRecordRequest {

    private final String xmlValue;
    private final boolean isRecordTypeDesc;
    private final boolean isClosed;
    private final boolean forceFormatRecordFields;
    private final String textFieldName;
    private final boolean withNameSpace;
    private final boolean withoutAttributes;
    private final boolean withoutAttributeAnnot;

    public XMLToRecordRequest(String xmlValue, boolean isRecordTypeDesc, boolean isClosed,
                              boolean forceFormatRecordFields, String textFieldName, boolean withNameSpace,
                              boolean withoutAttributes, boolean withoutAttributeAnnot) {
        this.xmlValue = xmlValue;
        this.isRecordTypeDesc = isRecordTypeDesc;
        this.isClosed = isClosed;
        this.forceFormatRecordFields = forceFormatRecordFields;
        this.textFieldName = textFieldName;
        this.withNameSpace = withNameSpace;
        this.withoutAttributes = withoutAttributes;
        this.withoutAttributeAnnot = withoutAttributeAnnot;
    }

    public String getXmlValue() {
        return xmlValue;
    }

    public boolean getIsRecordTypeDesc() {
        return isRecordTypeDesc;
    }

    public boolean getIsClosed() {
        return isClosed;
    }

    public boolean getForceFormatRecordFields() {
        return forceFormatRecordFields;
    }

    public String getTextFieldName() {
        return textFieldName;
    }

    public boolean getIsWithNameSpace() {
        return withNameSpace;
    }

    public boolean getWithoutAttributes() {
        return withoutAttributes;
    }

    public boolean getWithoutAttributeAnnot() {
        return withoutAttributeAnnot;
    }
}
