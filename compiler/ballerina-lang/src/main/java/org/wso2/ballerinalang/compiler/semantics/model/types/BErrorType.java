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

import org.ballerinalang.model.types.ErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

/**
 * Represents error type in Ballerina.
 *
 * @since 0.983.0
 */
public class BErrorType extends BType implements ErrorType {
    public BType detailType;
    public BTypeIdSet typeIdSet;

    private static final String DOLLAR = "$";
    private static final String ERROR = "error<";
    private static final String CLOSE_ERROR = ">";

    public BErrorType(BTypeSymbol tSymbol, BType detailType) {
        super(TypeTags.ERROR, tSymbol, Flags.READONLY);
        this.detailType = detailType;
        this.typeIdSet = BTypeIdSet.emptySet();
    }

    public BErrorType(BTypeSymbol tSymbol) {
        super(TypeTags.ERROR, tSymbol, Flags.READONLY);
        this.typeIdSet = BTypeIdSet.emptySet();
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public BType getDetailType() {
        return detailType;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (tsymbol != null && tsymbol.name != null && !tsymbol.name.value.startsWith(DOLLAR)
                && !tsymbol.name.value.isEmpty()) {
            return String.valueOf(tsymbol);
        }
        return ERROR +  detailType + CLOSE_ERROR;
    }
}
