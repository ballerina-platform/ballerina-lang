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

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.concurrent.CompletableFuture;

/**
 * The extended service for the XMLToRecord endpoint.
 *
 * @since 2201.7.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("xmlToRecord")
public class XMLToRecordConverterService implements ExtendedLanguageServerService {

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<XMLToRecordResponse> convert(XMLToRecordRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String xmlValue = request.getXmlValue();
            boolean isRecordTypeDesc = request.getIsRecordTypeDesc();
            boolean isClosed = request.getIsClosed();
            boolean forceFormatRecordFields = request.getForceFormatRecordFields();
            String textFieldName = request.getTextFieldName();
            boolean withNameSpace = request.getIsWithNameSpace();
            boolean withoutAttributes = request.getWithoutAttributes();
            boolean withoutAttributeAnnot = request.getWithoutAttributeAnnot();

            return XMLToRecordConverter.convert(xmlValue, isRecordTypeDesc, isClosed, forceFormatRecordFields,
                    textFieldName, withNameSpace, withoutAttributes, withoutAttributeAnnot);
        });
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }
}
