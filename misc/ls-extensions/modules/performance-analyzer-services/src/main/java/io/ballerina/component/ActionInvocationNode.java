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

import org.apache.commons.lang3.StringUtils;

/**
 * model obj to store action invocations.
 */
public class ActionInvocationNode extends Node {

    private final int endPointRef;
    private final String name;
    private final String path;
    private final String pos;

    public ActionInvocationNode(Integer endPointRef, String name, String path, String pos) {

        this.endPointRef = endPointRef;
        this.name = name;
        this.path = StringUtils.strip(path, " \"");
        this.pos = pos;
    }

    public int getEndPointRef() {

        return endPointRef;
    }

    public String getName() {

        return name;
    }

    public String getPath() {

        return path;
    }

    public String getPos() {

        return this.pos;
    }
}
