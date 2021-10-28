/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.types;

import io.ballerina.types.Definition;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.EnumSet;
import java.util.Set;

/**
 * {@code BLangType} is the abstract implementation of the {@link TypeNode}
 * which represents a type node in the Ballerina AST.
 *
 * @since 0.94
 */
public abstract class BLangType extends BLangNode implements TypeNode {
    public boolean nullable;
    public boolean grouped;
    public Set<Flag> flagSet = EnumSet.noneOf(Flag.class);
    public Definition defn;

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean isGrouped() {
        return grouped;
    }
}
