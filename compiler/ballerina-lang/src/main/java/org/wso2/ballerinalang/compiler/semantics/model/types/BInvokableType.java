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

import org.ballerinalang.model.types.InvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * @since 0.94
 */
public class BInvokableType extends BType implements InvokableType {

    public List<BType> paramTypes;
    public BType retType;

    public BInvokableType(List<BType> paramTypes, BType retType, BTypeSymbol tsymbol) {
        super(TypeTags.INVOKABLE, tsymbol);
        this.paramTypes = paramTypes;
        this.retType = retType;
    }

    @Override
    public List<BType> getParameterTypes() {
        return paramTypes;
    }

    @Override
    public BType getReturnType() {
        return retType;
    }

    @Override
    public String getDesc() {
        return TypeDescriptor.SIG_FUNCTION;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        return getTypeName(TypeDescriptor.SIG_FUNCTION, paramTypes, retType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BInvokableType)) {
            return false;
        }
        BInvokableType that = (BInvokableType) o;

        if (paramTypes != null ? !paramTypes.equals(that.paramTypes) : that.paramTypes != null) {
            return false;
        }

        if (retType != null ? !retType.equals(that.retType) : that.retType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = paramTypes != null ? paramTypes.hashCode() : 0;
        result = 31 * result + (retType != null ? retType.hashCode() : 0);
        return result;
    }

    private static String getTypeName(String typeDescriptor, List<BType> paramType, BType retType) {
        return (TypeDescriptor.SIG_FUNCTION.equals(typeDescriptor) ? "function " : "")
                + "(" + (paramType.size() != 0 ? getBTypeListAsString(paramType) : "") + ")"
                + " returns (" + retType + ")"; // TODO improve this with void type
    }

    private static String getBTypeListAsString(List<BType> typeNames) {
        StringBuffer br = new StringBuffer();
        int i = 0;
        for (BType type : typeNames) {
            br.append(type);
            if (++i < typeNames.size()) {
                br.append(",");
            }
        }
        return br.toString();
    }
}
