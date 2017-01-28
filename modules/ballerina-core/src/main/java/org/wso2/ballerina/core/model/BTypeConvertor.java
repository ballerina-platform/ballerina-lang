/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.statements.BlockStmt;

/**
 * Class to represent TypeConvertor written in ballerina
 */
public class BTypeConvertor implements TypeConvertor, CompilationUnit {

    private SymbolName symbolName;
    private String typeConverterName;
    private Position typeConverterLocation;

    private Annotation[] annotations;
    private Parameter[] parameters;
    private VariableDcl[] variableDcls;
    private Parameter[] returnParams;
    private BlockStmt typeConverterBody;

    private boolean publicFunc;
    private int stackFrameSize;

    public BTypeConvertor(SymbolName name,
                          Position position,
                          Boolean isPublic,
                          Annotation[] annotations,
                          Parameter[] parameters,
                          Parameter[] returnParams,
                          VariableDcl[] variableDcls,
                          BlockStmt typeConverterBody) {

        this.symbolName = name;
        this.typeConverterName = symbolName.getName();
        this.typeConverterLocation = position;
        this.publicFunc = isPublic;
        this.annotations = annotations;
        this.parameters = parameters;
        this.returnParams = returnParams;
        this.variableDcls = variableDcls;
        this.typeConverterBody = typeConverterBody;
    }

    public BTypeConvertor(SymbolName name,
                          Position position,
                          Boolean isPublic,
                          Annotation[] annotations,
                          Parameter[] parameters,
                          Parameter[] returnParams,
                          BlockStmt typeConverterBody) {

        this.symbolName = name;
        this.typeConverterName = symbolName.getName();
        this.typeConverterLocation = position;
        this.publicFunc = isPublic;
        this.annotations = annotations;
        this.parameters = parameters;
        this.returnParams = returnParams;
        this.typeConverterBody = typeConverterBody;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return symbolName.getName();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackageName() {
        return symbolName.getPkgName();
    }

    /**
     * Get the typeConvertor Identifier
     *
     * @return typeConvertor identifier
     */
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public void setSymbolName(SymbolName symbolName) {
        this.symbolName = symbolName;
    }

    /**
     * Get all the Annotations associated with a BallerinatypeConverter
     *
     * @return list of Annotations
     */
    public Annotation[] getAnnotations() {
        return annotations;
    }

    /**
     * Get list of Arguments associated with the typeConvertor definition
     *
     * @return list of Arguments
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * Get all the variableDcls declared in the scope of BallerinaTypeConvertor
     *
     * @return list of all BallerinaTypeConvertor scoped variableDcls
     */
    public VariableDcl[] getVariableDcls() {
        return variableDcls;
    }

    public Parameter[] getReturnParameters() {
        return returnParams;
    }

    @Override
    public String getTypeConverterName() {
        return null;
    }

    /**
     * Check whether typeConvertor is public, which means typeConvertor is visible outside the package
     *
     * @return whether typeConvertor is public
     */
    public boolean isPublic() {
        return publicFunc;
    }

    /**
     * Mark typeConvertor as public
     */
    public void makePublic() {
        publicFunc = true;
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }


    public void setStackFrameSize(int stackFrameSize) {
        this.stackFrameSize = stackFrameSize;
    }

    @Override
    public BlockStmt getCallableUnitBody() {
        return this.typeConverterBody;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getLocation() {
        return typeConverterLocation;
    }
}
