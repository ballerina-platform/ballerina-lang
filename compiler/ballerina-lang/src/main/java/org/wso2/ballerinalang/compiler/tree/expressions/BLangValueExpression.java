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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

/**
 * {@code BLangValueExpression} represents value expressions that may be lvalues.
 * i.e: field access, index-based access & variable refs.
 * y = 1;
 * x.y = 1; // isLValue = true;
 * x["y"] = 1;
 * z = x.y; // isLValue = false;
 *
 * @since 2.0.0
 */
public abstract class BLangValueExpression extends BLangExpression {

    // Semantic Data
    public boolean isLValue = false;
    public boolean isCompoundAssignmentLValue = false;
    public BSymbol symbol;

}
