/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.docgen.model;

import java.util.LinkedList;

/**
 * Documentable node for constant.
 *
 * @since 0.985.0
 */
public class ConstantDoc extends Documentable {

    public final Object value;
    public final String typeNodeType;
    public final String href;
    public final boolean isConstant;

    /**
     * Construct.
     *
     * @param name         constant name
     * @param value        value of the constant
     * @param description  description
     * @param typeNodeType constant's type node's type if available
     * @param href         link to data type
     */
    public ConstantDoc(String name, Object value, String description, String typeNodeType, String href) {
        super(name, "fw-const", description, new LinkedList<>());
        this.value = value;
        this.typeNodeType = typeNodeType;
        this.href = href;
        this.isConstant = true;
    }
}
