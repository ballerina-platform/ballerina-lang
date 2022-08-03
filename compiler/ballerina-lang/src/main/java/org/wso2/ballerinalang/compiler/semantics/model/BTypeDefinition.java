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

package org.wso2.ballerinalang.compiler.semantics.model;

import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

public class BTypeDefinition {
    public BType type;
    private BIntersectionType immutableType;
    public boolean hasImmutableUsed = false;

    public BTypeDefinition(BType type) {
        this.type = type;
    }

    public BType getMutableType() {
        return type;
    }

    public void setMutableType(BType type) {
        this.type = type;
    }

    public BIntersectionType getImmutableType() {
        hasImmutableUsed = true;
        return immutableType;
    }

    public void setImmutableType(BIntersectionType type) {
        this.immutableType = type;
    }
}
