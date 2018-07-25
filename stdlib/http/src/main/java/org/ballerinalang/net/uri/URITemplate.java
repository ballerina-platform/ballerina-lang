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

package org.ballerinalang.net.uri;

import org.ballerinalang.net.uri.parser.DataElement;
import org.ballerinalang.net.uri.parser.DataElementFactory;
import org.ballerinalang.net.uri.parser.DataReturnAgent;
import org.ballerinalang.net.uri.parser.Node;
import org.ballerinalang.net.uri.parser.URITemplateParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Basic URI Template implementation.
 *
 * @param <DataType> Data type stored in the  element.
 * @param <InboundMsgType> Inbound message type for additional checks.
 **/
public class URITemplate<DataType, InboundMsgType> {

    private Node<DataType, InboundMsgType> syntaxTree;

    public URITemplate(Node<DataType, InboundMsgType> syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public DataType matches(String uri, Map<String, String> variables, InboundMsgType inboundMsg) {
        DataReturnAgent<DataType> dataReturnAgent = new DataReturnAgent<>();
        boolean isFound = syntaxTree.matchAll(uri, variables, 0, inboundMsg, dataReturnAgent);
        if (isFound) {
            return dataReturnAgent.getData();
        }
        if (dataReturnAgent.getError() != null) {
            throw dataReturnAgent.getError();
        }
        return null;
    }

    public void parse(String uriTemplate, DataType resource,
                      DataElementFactory<? extends DataElement<DataType, InboundMsgType>>
                              elementCreator) throws URITemplateException, UnsupportedEncodingException {
        uriTemplate = removeTheFirstAndLastBackSlash(uriTemplate);

        URITemplateParser<DataType, InboundMsgType> parser = new URITemplateParser<>(syntaxTree, elementCreator);
        parser.parse(uriTemplate, resource);
    }

    private String removeTheFirstAndLastBackSlash(String template) throws URITemplateException {
        String uri = template;
        if ("/".equals(uri)) {
            return uri;
        }
        if (uri.startsWith("*")) {
            throw new URITemplateException("Invalid path literal");
        }
        if (!"/".equals(uri) && uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        return uri;
    }
}
