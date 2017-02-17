/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model;

import org.ballerinalang.model.builder.CallableUnitBuilder;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code Resource} is a single request handler within a {@code Service}.
 * The resource concept is designed to be access protocol independent.
 * But in the initial release of the language it is intended to work with HTTP.
 * <p>
 * The structure of a ResourceDefinition is as follows:
 * <p>
 * [ResourceAnnotations]
 * resource ResourceName (Message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
 * ConnectionDeclaration;*
 * VariableDeclaration;*
 * WorkerDeclaration;*
 * Statement;+
 * }*
 *
 * @since 0.8.0
 */
public class Resource implements Node, SymbolScope, CallableUnit {
    private NodeLocation location;

    // BLangSymbol related attributes
    protected String name;
    protected String pkgPath;
    protected boolean isPublic;
    protected SymbolName symbolName;

    // TODO Refactor
    private int stackFrameSize;
    private Annotation[] annotations;
    private ParameterDef[] parameterDefs;
    private BType[] parameterTypes;
    private Worker[] workers;
    private BlockStmt resourceBody;


    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;
    private int tempStackFrameSize;

    private Resource(SymbolScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
    }

    /**
     * Get an Annotation from a given name.
     *
     * @param name name of the annotation
     * @return Annotation
     */
    public Annotation getAnnotation(String packageName, String name) {
        /* ToDo : Annotations should be a map. */

        String annotationFqn = packageName.concat(":").concat(name);
        for (Annotation annotation : annotations) {
            if (annotation.getName().equals(annotationFqn)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Get all the Annotations associated with a Resource.
     *
     * @return list of Annotations
     */
    public Annotation[] getResourceAnnotations() {
        return annotations;
    }

    /**
     * Get all the Workers associated with a Resource.
     *
     * @return list of Workers
     */
    public Worker[] getWorkers() {
        return workers;
    }

    /**
     * Get resource body.
     *
     * @return returns the block statement
     */
    public BlockStmt getResourceBody() {
        return resourceBody;
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }

    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    @Override
    public int getTempStackFrameSize() {
        return tempStackFrameSize;
    }

    @Override
    public void setTempStackFrameSize(int stackFrameSize) {
        if (this.tempStackFrameSize > 0 && stackFrameSize != this.tempStackFrameSize) {
            throw new FlowBuilderException("Attempt to Overwrite tempValue Frame size. current :" +
                    this.tempStackFrameSize + ", new :" + stackFrameSize);
        }
        this.tempStackFrameSize = stackFrameSize;
    }

    // Methods in CallableUnit interface

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Get all the Annotations associated with a BallerinaFunction.
     *
     * @return list of Annotations
     */
    @Override
    public Annotation[] getAnnotations() {
        return this.annotations;
    }

    /**
     * Get list of Arguments associated with the function definition.
     *
     * @return list of Arguments
     */
    public ParameterDef[] getParameterDefs() {
        return this.parameterDefs;
    }

    /**
     * Get all the variableDcls declared in the scope of BallerinaFunction.
     *
     * @return list of all BallerinaFunction scoped variableDcls
     */
    @Override
    public VariableDef[] getVariableDefs() {
        return null;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return this.resourceBody;
    }

    @Override
    public ParameterDef[] getReturnParameters() {
        return new ParameterDef[0];
    }

    @Override
    public BType[] getReturnParamTypes() {
        return null;
    }

    @Override
    public void setReturnParamTypes(BType[] returnParamTypes) {

    }

    @Override
    public BType[] getArgumentTypes() {
        return parameterTypes;
    }

    @Override
    public void setParameterTypes(BType[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }


    // Methods in BLangSymbol interface

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return this;
    }


    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.LOCAL;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    /**
     * {@code ResourceBuilder} is responsible for building a {@cdoe Resource} node.
     *
     * @since 0.8.0
     */
    public static class ResourceBuilder extends CallableUnitBuilder {
        private Resource resource;

        public ResourceBuilder(SymbolScope enclosingScope) {
            resource = new Resource(enclosingScope);
            currentScope = resource;
        }

        public Resource buildResource() {
            resource.location = this.location;
            resource.name = this.name;
            resource.pkgPath = this.pkgPath;
            resource.symbolName = new SymbolName(name, pkgPath);

            resource.annotations = this.annotationList.toArray(new Annotation[this.annotationList.size()]);
            resource.parameterDefs = this.parameterDefList.toArray(new ParameterDef[this.parameterDefList.size()]);
            resource.workers = this.workerList.toArray(new Worker[this.workerList.size()]);
            resource.resourceBody = this.body;
            return resource;
        }
    }
}
