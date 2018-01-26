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
    public List<BType> retTypes;
    public String typeDescriptor;

    // This field is only applicable for functions and actions at the moment.
    private BType receiverType;

    public BInvokableType(List<BType> paramTypes,
                          List<BType> retTypes, BTypeSymbol tsymbol) {
        super(TypeTags.INVOKABLE, tsymbol);
        this.paramTypes = paramTypes;
        this.retTypes = retTypes;
    }

    @Override
    public List<BType> getParameterTypes() {
        return paramTypes;
    }

    @Override
    public List<BType> getReturnTypes() {
        return retTypes;
    }

    @Override
    public String getDesc() {
        return typeDescriptor;
    }

    public BType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(BType receiverType) {
        this.receiverType = receiverType;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        return getTypeName(typeDescriptor, paramTypes, retTypes);
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
        if (retTypes != null ? !retTypes.equals(that.retTypes) : that.retTypes != null) {
            return false;
        }
        if (typeDescriptor != null ? !typeDescriptor.equals(that.typeDescriptor) : that.typeDescriptor != null) {
            return false;
        }
        return receiverType != null ? receiverType.equals(that.receiverType) : that.receiverType == null;
    }

    @Override
    public int hashCode() {
        int result = paramTypes != null ? paramTypes.hashCode() : 0;
        result = 31 * result + (retTypes != null ? retTypes.hashCode() : 0);
        result = 31 * result + (typeDescriptor != null ? typeDescriptor.hashCode() : 0);
        result = 31 * result + (receiverType != null ? receiverType.hashCode() : 0);
        return result;
    }

    public static String getTypeName(String typeDescriptor, List<BType> paramType, List<BType> retType) {
        return (TypeDescriptor.SIG_FUNCTION.equals(typeDescriptor) ? "function " : "")
                + "(" + (paramType.size() != 0 ? getBTypListAsString(paramType) : "") + ")"
                + (retType.size() != 0 ? " returns (" + getBTypListAsString(retType) + ")" : "");
    }

    private static String getBTypListAsString(List<BType> typeNames) {
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
