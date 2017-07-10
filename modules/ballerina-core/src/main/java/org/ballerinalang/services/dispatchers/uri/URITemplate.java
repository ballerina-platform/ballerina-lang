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

package org.ballerinalang.services.dispatchers.uri;

import org.ballerinalang.services.dispatchers.uri.parser.Node;
import org.ballerinalang.services.dispatchers.uri.parser.URITemplateParser;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;

/**
 * Basic URI Template implementation.
 *
 **/

public class URITemplate {

    private Node syntaxTree;

    public URITemplate(Node syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public String expand(Map<String, String> variables) {
        return null;
    }

    public ResourceInfo matches(String uri, Map<String, String> variables, CarbonMessage carbonMessage) {
        return syntaxTree.matchAll(uri, variables, carbonMessage, 0);
    }

    public void parse(String uriTemplate, ResourceInfo resource) throws URITemplateException {
        uriTemplate = removeTheFirstAndLastBackSlash(uriTemplate);

        URITemplateParser parser = new URITemplateParser(syntaxTree);
        parser.parse(uriTemplate, resource);
    }

    public String removeTheFirstAndLastBackSlash(String template) {
        String uri = template;
        if (!uri.equals("/")) {
            if (!"/".equals(uri) && uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
            }

            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            return uri;
        }
        return uri;
    }
}
