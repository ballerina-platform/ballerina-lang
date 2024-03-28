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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import org.ballerinalang.model.types.NoType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;

/**
 * @since 0.94
 */
public class BNoType extends BType implements NoType {

    public BNoType(int tag) {
        super(tag, null);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SemType semType() {
        return PredefinedType.UNDEF;
    }
}
