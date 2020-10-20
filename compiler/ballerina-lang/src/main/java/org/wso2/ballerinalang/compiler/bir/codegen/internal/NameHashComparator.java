/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import io.ballerina.runtime.IdentifierUtils;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;

import java.util.Comparator;

/**
 * BIR Param name comparator to sort according its hash code.
 *
 * @since 1.2.0
 */
public class NameHashComparator implements Comparator<NamedNode> {

    @Override
    public int compare(NamedNode o1, NamedNode o2) {
        String name1 = IdentifierUtils.decodeIdentifier(o1.getName().value);
        String name2 = IdentifierUtils.decodeIdentifier(o2.getName().value);
        return Integer.compare(name1.hashCode(), name2.hashCode());
    }
}
