/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.ModuleID;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Represents type descriptors which do not fall in to any other type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaTypeSymbol extends AbstractTypeSymbol {

    private final String typeName;

    public BallerinaTypeSymbol(CompilerContext context, ModuleID moduleID, BType bType) {
        super(context, TypesFactory.getTypeDescKind(bType.getKind()), bType);
        // In an unlikely event if the `BNoType` get exposed, this would ensure that the user would know that this is
        // not a typical condition.
        this.typeName = bType.tag == TypeTags.NONE ? "$UndefinedType$" : bType.getKind().typeName();
    }

    @Override
    public String signature() {
        return this.typeName;
    }
}
