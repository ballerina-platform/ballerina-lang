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
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Represents error type in Ballerina.
 *
 * @since 0.983.0
 */
public class BErrorType extends BType implements ErrorType {

    public BType reasonType;
    public BType detailType;

    private static final String DOLLAR = "$";
    private static final String ERROR = "error<";
    private static final String SPACE = " ";
    private static final String COMMA = ",";
    private static final String CLOSE_ERROR = ">";

    public BErrorType(BTypeSymbol tSymbol, BType reasonType, BType detailType) {
        super(TypeTags.ERROR, tSymbol);
        this.reasonType = reasonType;
        this.detailType = detailType;
    }

    public BErrorType(BTypeSymbol tSymbol) {
        super(TypeTags.ERROR, tSymbol);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public BType getReasonType() {
        return reasonType;
    }

    @Override
    public BType getDetailType() {
        return detailType;
    }

    @Override
    public String getDesc() {
        return TypeDescriptor.SIG_ERROR + getQualifiedTypeName() + ";";
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (tsymbol != null && tsymbol.name != null && !tsymbol.name.value.startsWith(DOLLAR)) {
            return String.valueOf(tsymbol);
        }
        return ERROR + reasonType + COMMA + SPACE + detailType + CLOSE_ERROR;
    }
}
