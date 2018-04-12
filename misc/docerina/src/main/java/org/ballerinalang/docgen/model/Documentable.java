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
package org.ballerinalang.docgen.model;


import java.util.List;

/**
 * Documentable node abstract class.
 */
public abstract class Documentable {
    public final String name;

    public String icon;

    public final String description;

    public final List<Documentable> children;

    /**
     *
     * @param name name of the node.
     * @param icon icon of the node.
     * @param description description of the node.
     * @param children children of the node if any.
     */
    public Documentable(String name, String icon, String description, List<Documentable> children) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.children = children;
    }
}
