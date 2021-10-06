/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.component;

import io.ballerina.tools.text.LineRange;

/**
 * model obj to store endpoints.
 *
 * @since 2.0.0
 */
public class EndPointNode {

    private final String pkgID;
    private final String name;
    private final String baseUrl;
    private final LineRange pos;

    public EndPointNode(String pkgID, String name, String baseUrl, LineRange pos) {

        this.pkgID = pkgID;
        this.name = name;
        this.baseUrl = baseUrl;
        this.pos = pos;
    }

    public String getPkgID() {

        return pkgID;
    }

    public String getName() {

        return name;
    }

    public String getBaseUrl() {

        return baseUrl;
    }

    public String getPos() {

        return pos.toString();
    }
}
