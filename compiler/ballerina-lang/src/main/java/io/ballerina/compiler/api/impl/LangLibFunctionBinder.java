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

import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A util class for creating type param resolved version of the lang lib functions.
 *
 * @since 2.0.0
 */
public class LangLibFunctionBinder {

    private final Types types;
    private final CompilerContext context;

    public LangLibFunctionBinder(Types types, CompilerContext context) {
        this.types = types;
        this.context = context;
    }

    public LangLibFunctionBinder(CompilerContext context) {
        this.types = Types.getInstance(context);
        this.context = context;
    }

    /**
     * Given a lang lib function symbol, this method will create a new instance of the symbol if it's a, function that
     * can be called using a method call expr. i.e., first param's type kind is the same as the lang library the
     * function belongs to and contains a type param component in it. This will resolve all occurrences of the type
     * param component in the first param.
     *
     * @param original  The lang lib function symbol
     * @param boundType The type to bind the type param to
     * @return The type param resolved lang lib function symbol
     */
    public BInvokableSymbol cloneAndBind(BInvokableSymbol original, BType type, BType boundType) {
        if (boundType == null || original.params.size() == 0) {
            return original;
        }

        BVarSymbol firstParam = original.params.get(0);
        BType typeParam = new TypeParamFinder().find(firstParam.getType());

        if (typeParam == null || !types.isAssignable(type, firstParam.getType())) {
            return original;
        }

        TypeParamResolver resolver = new TypeParamResolver(typeParam, context);
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
        // The following null check is required since this method is also used for duplicating the rest param symbols.
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
        ((List<AnnotationAttachmentSymbol>) duplicate.getAnnotations()).addAll(original.getAnnotations());
        duplicate.bodyExist = original.bodyExist;
        duplicate.markdownDocumentation = original.markdownDocumentation;
        duplicate.receiverSymbol = original.receiverSymbol;

        return duplicate;
    }

    private BInvokableType duplicateType(BInvokableType original, List<BVarSymbol> newParams, BVarSymbol newRestParam,
                                         BType newRetType) {

        List<BType> paramTypes = new ArrayList<>();
        if (newParams.size() == original.paramTypes.size()) {
            paramTypes.addAll(newParams.stream().map(BSymbol::getType).collect(Collectors.toList()));
        } else {
            paramTypes.addAll(original.paramTypes);
        }

        BInvokableType duplicate = new BInvokableType(paramTypes, original.restType, original.retType, null);
        BInvokableTypeSymbol originalSym = (BInvokableTypeSymbol) original.tsymbol;
        BInvokableTypeSymbol duplicateTSym = new BInvokableTypeSymbol(original.tag, originalSym.flags,
                                                                      originalSym.pkgID, duplicate,
                                                                      originalSym.owner, originalSym.pos,
                                                                      originalSym.origin);

        duplicate.retType = newRetType;
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

        for (AnnotationAttachmentSymbol annot : original.getAnnotations()) {
            duplicate.addAnnotation(annot);
        }

        duplicate.isDefaultable = original.isDefaultable;
        duplicate.state = original.state;

        return duplicate;
    }
}
