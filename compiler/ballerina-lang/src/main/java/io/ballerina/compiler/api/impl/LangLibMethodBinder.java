/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;

/**
 * A util class for creating type param resolved version of the lang lib functions.
 *
 * @since 2.0.0
 */
public class LangLibMethodBinder {

    public BInvokableSymbol cloneAndBind(BInvokableSymbol original, BType boundType) {
        if (original.params.size() == 0) {
            return original;
        }

        TypeParamResolver resolver = new TypeParamResolver();
        BVarSymbol firstParam = original.params.get(0);
        BType resolvedType = resolver.resolve(firstParam.getType(), boundType);

        if (resolvedType == firstParam.getType()) {
            return original;
        }

        BInvokableSymbol duplicate = duplicateSymbol(original);

        for (BVarSymbol param : original.params) {
            BVarSymbol newParamSymbol = createNewVarSymbol(param, boundType, resolver);
            duplicate.params.add(newParamSymbol);
        }

        duplicate.restParam = createNewVarSymbol(original.restParam, boundType, resolver);
        duplicate.retType = resolver.resolve(original.retType, boundType);
        duplicate.type = duplicateType(original.getType(), duplicate.params, duplicate.restParam, duplicate.retType);

        return duplicate;
    }

    private BVarSymbol createNewVarSymbol(BVarSymbol param, BType boundType, TypeParamResolver resolver) {
        if (param == null) {
            return null;
        }

        BType originalParamType = param.getType();
        BType newParamType = resolver.resolve(originalParamType, boundType);

        if (originalParamType == newParamType) {
            return param;
        }

        BVarSymbol duplicateParam = duplicateSymbol(param);
        duplicateParam.type = newParamType;

        return duplicateParam;
    }

    private BInvokableSymbol duplicateSymbol(BInvokableSymbol original) {
        BInvokableSymbol duplicate = Symbols.createInvokableSymbol(original.tag, original.flags, original.name,
                                                                   original.originalName, original.pkgID, original.type,
                                                                   original.owner, original.pos, original.origin);

        duplicate.annAttachments.addAll(original.annAttachments);
        duplicate.bodyExist = original.bodyExist;
        duplicate.markdownDocumentation = original.markdownDocumentation;
        duplicate.receiverSymbol = original.receiverSymbol;

        return duplicate;
    }

    private BInvokableType duplicateType(BInvokableType original, List<BVarSymbol> newParams, BVarSymbol newRestParam,
                                         BType newRetType) {
        BInvokableType duplicate = new BInvokableType(original.paramTypes, original.restType, original.retType, null);
        BInvokableTypeSymbol originalSym = (BInvokableTypeSymbol) original.tsymbol;
        BInvokableTypeSymbol duplicateTSym = new BInvokableTypeSymbol(original.tag, originalSym.flags,
                                                                      originalSym.pkgID, duplicate,
                                                                      originalSym.owner, originalSym.pos,
                                                                      originalSym.origin);

        duplicateTSym.params.addAll(newParams);
        duplicateTSym.restParam = newRestParam;
        duplicateTSym.returnType = newRetType;
        duplicateTSym.returnTypeAnnots.addAll(originalSym.returnTypeAnnots);

        duplicate.tsymbol = duplicateTSym;
        return duplicate;
    }

    private BVarSymbol duplicateSymbol(BVarSymbol original) {
        BVarSymbol duplicate = new BVarSymbol(original.flags, original.isWildcard, original.name, original.originalName,
                                              original.pkgID, original.type, original.owner, original.pos,
                                              original.origin);
        duplicate.markdownDocumentation = original.markdownDocumentation;

        for (AnnotationSymbol annot : original.getAnnotations()) {
            duplicate.addAnnotation(annot);
        }

        duplicate.isDefaultable = original.isDefaultable;
        duplicate.state = original.state;

        return duplicate;
    }
}
