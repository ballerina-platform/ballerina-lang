/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.GlobalVarLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.composer.service.workspace.api.StringUtil;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.ConnectorDcl;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FieldAccessExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONFieldAccessExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionRollbackStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.values.BString;

import java.util.Stack;


/**
 * Serializes ballerina language object model to JSON based model.
 */
public class BLangJSONModelBuilder implements NodeVisitor {

    private JsonObject jsonObj;
    private Stack<JsonArray> tempJsonArrayRef = new Stack<>();
    private boolean isExprAsString = true;
    private BLangExpressionModelBuilder exprVisitor;

    public BLangJSONModelBuilder(JsonObject jsonObj) {
        this.exprVisitor = new BLangExpressionModelBuilder();
        this.jsonObj = jsonObj;
    }

    @Override
    public void visit(BLangProgram bLangProgram) {

    }

    @Override
    public void visit(BLangPackage bLangPackage) {

    }

    @Override
    public void visit(BallerinaFile bFile) {

        tempJsonArrayRef.push(new JsonArray());

        //package definitions
        JsonObject pkgDefine = new JsonObject();
        pkgDefine.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.PACKAGE_DEFINITION);
        pkgDefine.addProperty(BLangJSONModelConstants.PACKAGE_NAME, bFile.getPackagePath());
        tempJsonArrayRef.peek().add(pkgDefine);

        //import declarations
        if (bFile.getImportPackages() != null) {
            for (ImportPackage anImport : bFile.getImportPackages()) {
                if (!anImport.isImplicitImport()) {
                    anImport.accept(this);
                }
            }
        }

        //        ArrayList<PositionAwareNode> rootElements = new ArrayList<>();

        //        if (bFile.getConstants() != null && bFile.getConstants().length > 0) {
        //            for (Const constDefinition : bFile.getConstants()) {
        //                rootElements.add(constDefinition);
        //            }
        //        }
        //
        //        if (bFile.getServices() != null) {
        //            Service[] services = new Service[bFile.getServices().size()];
        //            bFile.getServices().toArray(services);
        //            for (Service service : services) {
        //                rootElements.add(service);
        //            }
        //        }
        //
        //        for (Function function : bFile.getFunctions()) {
        //            BallerinaFunction bFunction = (BallerinaFunction) function;
        //            rootElements.add(bFunction);
        //        }


        //        Collections.sort(rootElements, new Comparator<PositionAwareNode>() {
        //            @Override
        //            public int compare(PositionAwareNode o1, PositionAwareNode o2) {
        //                return Integer.compare(o1.getRelativePosition(), o2.getRelativePosition());
        //            }
        //        });

        //service definitions //connector definitions //function definition
        for (CompilationUnit node : bFile.getCompilationUnits()) {
            node.accept(this);
        }

        this.jsonObj.add(BLangJSONModelConstants.ROOT, tempJsonArrayRef.peek());
        this.addWhitespaceDescriptor(this.jsonObj, bFile.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.pop();

    }

    @Override
    public void visit(ImportPackage importPackage) {
        JsonObject importObj = new JsonObject();
        importObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.IMPORT_DEFINITION);
        importObj.addProperty(BLangJSONModelConstants.IMPORT_PACKAGE_NAME, importPackage.getName());
        importObj.addProperty(BLangJSONModelConstants.IMPORT_PACKAGE_PATH, importPackage.getPath());
        this.addPosition(importObj, importPackage.getNodeLocation());
        this.addWhitespaceDescriptor(importObj, importPackage.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(importObj);
    }

    @Override
    public void visit(Service service) {
        JsonObject serviceObj = new JsonObject();
        serviceObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.SERVICE_DEFINITION);
        serviceObj.addProperty(BLangJSONModelConstants.SERVICE_NAME, service.getSymbolName().getName());
        this.addPosition(serviceObj, service.getNodeLocation());
        this.addWhitespaceDescriptor(serviceObj, service.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
    
        if (service.getAnnotations() != null) {
            for (AnnotationAttachment annotation : service.getAnnotations()) {
                annotation.accept(this);
            }
        }
        
        if (service.getVariableDefStmts() != null) {
            for (VariableDefStmt variableDefStmt : service.getVariableDefStmts()) {
                variableDefStmt.accept(this);
            }
        }

        if (service.getResources() != null) {
            for (Resource resource : service.getResources()) {
                resource.accept(this);
            }
        }

        serviceObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(serviceObj);
    }

    @Override
    public void visit(BallerinaConnectorDef connector) {
        JsonObject jsonConnectObj = new JsonObject();
        this.addPosition(jsonConnectObj, connector.getNodeLocation());
        this.addWhitespaceDescriptor(jsonConnectObj, connector.getWhiteSpaceDescriptor());
        jsonConnectObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .CONNECTOR_DEFINITION);
        jsonConnectObj.addProperty(BLangJSONModelConstants.CONNECTOR_NAME, connector.getSymbolName().getName());
        tempJsonArrayRef.push(new JsonArray());

        if (connector.getAnnotations() != null) {
            for (AnnotationAttachment annotation : connector.getAnnotations()) {
                annotation.accept(this);
            }
        }

        if (connector.getParameterDefs() != null) {
            tempJsonArrayRef.push(new JsonArray());
            for (ParameterDef parameterDef : connector.getParameterDefs()) {
                parameterDef.accept(this);
            }
            JsonObject argsObj = new JsonObject();
            argsObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                BLangJSONModelConstants.ARGUMENT_PARAMETER_DEFINITIONS);
            argsObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(argsObj);
        }

        if (connector.getVariableDefStmts() != null) {
            for (VariableDefStmt variableDefStmt : connector.getVariableDefStmts()) {
                variableDefStmt.accept(this);
            }
        }
        if (connector.getActions() != null) {
            for (BallerinaAction action : connector.getActions()) {
                action.accept(this);
            }
        }
        jsonConnectObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonConnectObj);
    }

    @Override
    public void visit(Resource resource) {
        JsonObject resourceObj = new JsonObject();
        resourceObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RESOURCE_DEFINITION);
        resourceObj.addProperty(BLangJSONModelConstants.RESOURCE_NAME, resource.getName());
        this.addPosition(resourceObj, resource.getNodeLocation());
        this.addWhitespaceDescriptor(resourceObj, resource.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (resource.getResourceAnnotations() != null) {
            for (AnnotationAttachment annotation : resource.getResourceAnnotations()) {
                annotation.accept(this);
            }
        }

        if (resource.getParameterDefs() != null) {
            tempJsonArrayRef.push(new JsonArray());
            for (ParameterDef parameterDef : resource.getParameterDefs()) {
                parameterDef.accept(this);
            }
            JsonObject argsParamObj = new JsonObject();
            argsParamObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                     BLangJSONModelConstants.ARGUMENT_PARAMETER_DEFINITIONS);
            argsParamObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(argsParamObj);
        }

        if (resource.getVariableDefs() != null) {
            for (VariableDef variableDef : resource.getVariableDefs()) {
                variableDef.accept(this);
            }
        }

        if (resource.getWorkers() != null) {
            for (Worker worker : resource.getWorkers()) {
                worker.accept(this);
            }
        }

        if (resource.getResourceBody() != null) {
            resource.getResourceBody().accept(this);
        }

        resourceObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(resourceObj);
    }

    @Override
    public void visit(BallerinaFunction function) {
        JsonObject jsonFunc = new JsonObject();
        jsonFunc.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.FUNCTION_DEFINITION);
        jsonFunc.addProperty(BLangJSONModelConstants.FUNCTIONS_NAME, function.getName());
        jsonFunc.addProperty(BLangJSONModelConstants.IS_PUBLIC_FUNCTION, function.isPublic());
        this.addPosition(jsonFunc, function.getNodeLocation());
        this.addWhitespaceDescriptor(jsonFunc, function.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.push(new JsonArray());

        if (function.getAnnotations() != null) {
            for (AnnotationAttachment annotation : function.getAnnotations()) {
                annotation.accept(this);
            }
        }

        if (function.getVariableDefs() != null) {
            for (VariableDef variableDef : function.getVariableDefs()) {
                variableDef.accept(BLangJSONModelBuilder.this);
            }
        }

        if (function.getParameterDefs() != null) {
            tempJsonArrayRef.push(new JsonArray());
            for (ParameterDef parameterDef : function.getParameterDefs()) {
                parameterDef.accept(this);
            }
            JsonObject argsParamObj = new JsonObject();
            argsParamObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                BLangJSONModelConstants.ARGUMENT_PARAMETER_DEFINITIONS);
            argsParamObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(argsParamObj);
        }

        if (function.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : function.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }

        if (function.getReturnParameters() != null) {
            tempJsonArrayRef.push(new JsonArray());
            for (ParameterDef parameterDef : function.getReturnParameters()) {
                parameterDef.accept(this);
            }
            JsonObject returnParamObj = new JsonObject();
            returnParamObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                BLangJSONModelConstants.RETURN_PARAMETER_DEFINITIONS);
            returnParamObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(returnParamObj);
        }

        if (function.getWorkers() != null) {
            for (Worker worker : function.getWorkers()) {
                worker.accept(this);
            }
        }

        if (!function.isNative()) {
            function.getCallableUnitBody().accept(this);
        }
        jsonFunc.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonFunc);
    }

    @Override
    public void visit(BTypeMapper typeMapper) {
        JsonObject jsonTypeMapper = new JsonObject();
        jsonTypeMapper.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .TYPE_MAPPER_DEFINITION);
        jsonTypeMapper.addProperty(BLangJSONModelConstants.TYPE_MAPPER_NAME, typeMapper.getName());
        this.addWhitespaceDescriptor(jsonTypeMapper, typeMapper.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.push(new JsonArray());

        if (typeMapper.getAnnotations() != null) {
            for (AnnotationAttachment annotation : typeMapper.getAnnotations()) {
                annotation.accept(this);
            }
        }

        if (typeMapper.getVariableDefs() != null) {
            for (VariableDef variableDef : typeMapper.getVariableDefs()) {
                variableDef.accept(BLangJSONModelBuilder.this);
            }
        }
        if (typeMapper.getParameterDefs() != null) {
            for (ParameterDef parameterDef : typeMapper.getParameterDefs()) {
                parameterDef.accept(this);
            }
        }
        JsonObject returnTypeObj = new JsonObject();
        returnTypeObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RETURN_TYPE);
        if (typeMapper.getReturnParameters() != null) {
            for (ParameterDef parameterDef : typeMapper.getReturnParameters()) {
                JsonObject paramObj = new JsonObject();
                paramObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RETURN_TYPE);
                paramObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, parameterDef.getTypeName().getSymbolName
                        ().getName());
                this.addWhitespaceDescriptor(paramObj, parameterDef.getWhiteSpaceDescriptor());
                this.tempJsonArrayRef.push(new JsonArray());
                if (parameterDef.getAnnotations() != null) {
                    for (AnnotationAttachment annotation : parameterDef.getAnnotations()) {
                        annotation.accept(this);
                    }
                }
                paramObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
                this.tempJsonArrayRef.pop();
                this.tempJsonArrayRef.peek().add(paramObj);
            }
        }
        typeMapper.getCallableUnitBody().accept(this);
        jsonTypeMapper.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonTypeMapper);
    }

    @Override
    public void visit(BallerinaAction action) {
        JsonObject jsonAction = new JsonObject();
        this.addPosition(jsonAction, action.getNodeLocation());
        this.addWhitespaceDescriptor(jsonAction, action.getWhiteSpaceDescriptor());
        jsonAction.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.ACTION_DEFINITION);
        jsonAction.addProperty(BLangJSONModelConstants.ACTION_NAME, action.getName());
        tempJsonArrayRef.push(new JsonArray());

        if (action.getAnnotations() != null) {
            for (AnnotationAttachment annotation : action.getAnnotations()) {
                annotation.accept(this);
            }
        }

        if (action.getParameterDefs() != null) {
            tempJsonArrayRef.push(new JsonArray());
            for (ParameterDef parameterDef : action.getParameterDefs()) {
                parameterDef.accept(this);
            }
            JsonObject argsObj = new JsonObject();
            argsObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                BLangJSONModelConstants.ARGUMENT_PARAMETER_DEFINITIONS);
            argsObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(argsObj);
        }

        if (action.getVariableDefs() != null) {
            for (VariableDef variableDef : action.getVariableDefs()) {
                variableDef.accept(this);
            }
        }
        if (action.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : action.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }

        if (action.getReturnParameters() != null) {
            tempJsonArrayRef.push(new JsonArray());
            for (ParameterDef parameterDef : action.getReturnParameters()) {
                parameterDef.accept(this);
            }
            JsonObject argsObj = new JsonObject();
            argsObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                BLangJSONModelConstants.RETURN_PARAMETER_DEFINITIONS);
            argsObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(argsObj);
        }

        if (action.getWorkers() != null) {
            for (Worker worker : action.getWorkers()) {
                worker.accept(this);
            }
        }
        action.getCallableUnitBody().accept(this);
        jsonAction.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonAction);
    }

    @Override
    public void visit(Worker worker) {
        JsonObject jsonWorker = new JsonObject();
        jsonWorker.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.WORKER_DEFINITION);
        jsonWorker.addProperty(BLangJSONModelConstants.WORKER_NAME, worker.getName());
        this.addPosition(jsonWorker, worker.getNodeLocation());
        this.addWhitespaceDescriptor(jsonWorker, worker.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        tempJsonArrayRef.push(new JsonArray());
        tempJsonArrayRef.push(new JsonArray());
        if (worker.getParameterDefs() != null) {
            for (ParameterDef parameterDef : worker.getParameterDefs()) {
                parameterDef.accept(this);
            }
        }

        jsonWorker.add(BLangJSONModelConstants.PARAMETER_DEFINITION, this.tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        if (worker.getReturnParameters() != null) {
            for (ParameterDef parameterDef : worker.getReturnParameters()) {
                parameterDef.accept(this);
            }
        }
        jsonWorker.add(BLangJSONModelConstants.RETURN_TYPE, this.tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        if (worker.getCallableUnitBody() != null) {
            worker.getCallableUnitBody().accept(this);
        }
        jsonWorker.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonWorker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(AnnotationAttachment annotation) {
        JsonObject jsonAnnotation = new JsonObject();
        jsonAnnotation.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.ANNOTATION);
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_NAME, annotation.getName());
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_PACKAGE_NAME, annotation.getPkgName());
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_PACKAGE_PATH, annotation.getPkgPath());
        this.addPosition(jsonAnnotation, annotation.getNodeLocation());
        this.addWhitespaceDescriptor(jsonAnnotation, annotation.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        annotation.getAttributeNameValuePairs().forEach(this::visitAnnotationEntry);
        jsonAnnotation.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonAnnotation);
    }

    /**
     * Visitor for an annotation key value pair.
     * @param key The key of the annotation value pair.
     * @param value The value of the annotation value pair.
     */
    public void visitAnnotationEntry(String key, AnnotationAttributeValue value) {
        JsonObject jsonAnnotationEntry = new JsonObject();
        jsonAnnotationEntry.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.ANNOTATION_ENTRY);
        jsonAnnotationEntry.addProperty(BLangJSONModelConstants.ANNOTATION_ENTRY_KEY, key);
        this.addWhitespaceDescriptor(jsonAnnotationEntry, value.getWhiteSpaceDescriptor());
        if (value.getLiteralValue() != null) {
            if (value.getLiteralValue() instanceof BString) {
                jsonAnnotationEntry.addProperty(BLangJSONModelConstants.ANNOTATION_ENTRY_VALUE,
                        "\"" + value.getLiteralValue().stringValue() + "\"");
            } else {
                jsonAnnotationEntry.addProperty(BLangJSONModelConstants.ANNOTATION_ENTRY_VALUE,
                        value.getLiteralValue().stringValue());
            }
        } else if (value.getAnnotationValue() != null) {
            this.tempJsonArrayRef.push(new JsonArray());
            value.getAnnotationValue().accept(this);
            jsonAnnotationEntry.add(BLangJSONModelConstants.ANNOTATION_ENTRY_VALUE,
                    this.tempJsonArrayRef.peek().get(0));
            this.tempJsonArrayRef.pop();
        } else if (value.getValueArray() != null) {
            this.tempJsonArrayRef.push(new JsonArray());
            visitAnnotationEntryArray(value.getValueArray());
            jsonAnnotationEntry.add(BLangJSONModelConstants.ANNOTATION_ENTRY_VALUE,
                    this.tempJsonArrayRef.peek().get(0));
            this.tempJsonArrayRef.pop();
        }
        this.tempJsonArrayRef.peek().add(jsonAnnotationEntry);
    }

    /**
     * Visitor for an annotation value which is an array.
     * @param annotationEntryArray The annotation array to be visited.
     */
    public void visitAnnotationEntryArray(AnnotationAttributeValue[] annotationEntryArray) {
        JsonObject jsonAnnotationEntryArray = new JsonObject();
        jsonAnnotationEntryArray.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.ANNOTATION_ENTRY_ARRAY);
        tempJsonArrayRef.push(new JsonArray());
        for (AnnotationAttributeValue annotationAttributeValue : annotationEntryArray) {
            visitAnnotationEntry(StringUtils.EMPTY, annotationAttributeValue);
        }
        jsonAnnotationEntryArray.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonAnnotationEntryArray);
    }

    @Override
    public void visit(ParameterDef parameterDef) {
        JsonObject paramObj = new JsonObject();
        paramObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.PARAMETER_DEFINITION);
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_NAME, parameterDef.getName());
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, parameterDef.getTypeName().getSymbolName()
                .getName());
        this.addPosition(paramObj, parameterDef.getNodeLocation());
        this.addWhitespaceDescriptor(paramObj, parameterDef.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.push(new JsonArray());
        if (parameterDef.getAnnotations() != null) {
            for (AnnotationAttachment annotation : parameterDef.getAnnotations()) {
                annotation.accept(this);
            }
        }
        paramObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        this.tempJsonArrayRef.peek().add(paramObj);
    }

    @Override
    public void visit(VariableDef variableDef) {
        JsonObject variableDefObj = new JsonObject();
        this.addPosition(variableDefObj, variableDef.getNodeLocation());
        this.addWhitespaceDescriptor(variableDefObj, variableDef.getWhiteSpaceDescriptor());
        variableDefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .VARIABLE_DEFINITION);
        variableDefObj.addProperty(BLangJSONModelConstants.VARIABLE_NAME, variableDef.getName());
        variableDefObj.addProperty(BLangJSONModelConstants.VARIABLE_TYPE, variableDef.getTypeName().toString());
        variableDefObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, variableDef.getTypeName().getPackageName());
        tempJsonArrayRef.peek().add(variableDefObj);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        if (blockStmt.getStatements() != null) {
            for (Statement statement : blockStmt.getStatements()) {
                statement.accept(this);
            }
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        JsonObject assignmentStmtObj = new JsonObject();
        this.addPosition(assignmentStmtObj, assignStmt.getNodeLocation());
        this.addWhitespaceDescriptor(assignmentStmtObj, assignStmt.getWhiteSpaceDescriptor());
        assignmentStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants
                .ASSIGNMENT_STATEMENT);
        tempJsonArrayRef.push(new JsonArray());

        JsonObject lExprObj = new JsonObject();
        lExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.LEFT_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        for (Expression expression : assignStmt.getLExprs()) {
            expression.accept(this);
        }
        lExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(lExprObj);

        JsonObject rExprObj = new JsonObject();
        rExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.RIGHT_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        assignStmt.getRExpr().accept(this);
        rExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(rExprObj);

        assignmentStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(assignmentStmtObj);
    }

    @Override
    public void visit(CommentStmt commentStmt) {
        JsonObject commentStmtObj = new JsonObject();
        this.addPosition(commentStmtObj, commentStmt.getNodeLocation());
        this.addWhitespaceDescriptor(commentStmtObj, commentStmt.getWhiteSpaceDescriptor());
        commentStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.COMMENT_STATEMENT);
        commentStmtObj.addProperty(BLangJSONModelConstants.COMMENT_STRING, commentStmt.getComment());
        tempJsonArrayRef.peek().add(commentStmtObj);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        JsonObject ifElseStmtObj = new JsonObject();
        ifElseStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.IF_ELSE_STATEMENT);
        this.addPosition(ifElseStmtObj, ifElseStmt.getNodeLocation());
        this.addWhitespaceDescriptor(ifElseStmtObj, ifElseStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (ifElseStmt.getThenBody() != null) {
            tempJsonArrayRef.push(new JsonArray());

            JsonObject thenBodyObj = new JsonObject();
            thenBodyObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                    .IF_STATEMENT_THEN_BODY);
            this.addPosition(thenBodyObj, ifElseStmt.getThenBody().getNodeLocation());
            tempJsonArrayRef.push(new JsonArray());
            ifElseStmt.getThenBody().accept(this);
            thenBodyObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(thenBodyObj);

            JsonObject ifConditionObj = new JsonObject();
            ifConditionObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                    .IF_STATEMENT_IF_CONDITION);
            tempJsonArrayRef.push(new JsonArray());
            ifElseStmt.getCondition().accept(this);
            ifConditionObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(ifConditionObj);

            ifElseStmtObj.add(BLangJSONModelConstants.IF_STATEMENT, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        }
        if (ifElseStmt.getElseBody() != null) {
            tempJsonArrayRef.push(new JsonArray());
            this.addPosition(ifElseStmtObj, ifElseStmt.getThenBody().getNodeLocation());
            ifElseStmt.getElseBody().accept(this);
            ifElseStmtObj.add(BLangJSONModelConstants.ELSE_STATEMENT, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        }
        if (ifElseStmt.getElseIfBlocks().length > 0) {
            tempJsonArrayRef.push(new JsonArray());
            IfElseStmt.ElseIfBlock[] elseIfBlocks = ifElseStmt.getElseIfBlocks();
            for (IfElseStmt.ElseIfBlock elseIfBlock : elseIfBlocks) {
                JsonObject elseIfObj = new JsonObject();
                elseIfObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                        BLangJSONModelConstants.ELSE_IF_STATEMENT);
                this.addWhitespaceDescriptor(elseIfObj, elseIfBlock.getWhiteSpaceDescriptor());
                tempJsonArrayRef.push(new JsonArray());
                elseIfBlock.getElseIfBody().accept(this);
                elseIfObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
                tempJsonArrayRef.pop();
                tempJsonArrayRef.peek().add(elseIfObj);
            }
            ifElseStmtObj.add(BLangJSONModelConstants.ELSE_IF_BLOCKS, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        }
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(ifElseStmtObj);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        JsonObject whileStmtObj = new JsonObject();
        whileStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.WHILE_STATEMENT);
        this.addPosition(whileStmtObj, whileStmt.getNodeLocation());
        this.addWhitespaceDescriptor(whileStmtObj, whileStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        whileStmt.getCondition().accept(this);
        if (whileStmt.getBody() != null) {
            whileStmt.getBody().accept(this);
        }
        whileStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(whileStmtObj);
    }

    @Override
    public void visit(TransformStmt transformStmt) {
        JsonObject transformStmtObj = new JsonObject();
        transformStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants
                .TRANSFORM_STATEMENT);
        this.addPosition(transformStmtObj, transformStmt.getNodeLocation());
        this.addWhitespaceDescriptor(transformStmtObj, transformStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());

        tempJsonArrayRef.push(new JsonArray());
        for (Expression expression : transformStmt.getInputExprs()) {
            expression.accept(this);
        }
        transformStmtObj.add(BLangJSONModelConstants.TRANSFORM_INPUT, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();

        tempJsonArrayRef.push(new JsonArray());
        for (Expression expression : transformStmt.getOutputExprs()) {
            expression.accept(this);
        }
        transformStmtObj.add(BLangJSONModelConstants.TRANSFORM_OUTPUT, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();

        if (transformStmt.getBody() != null) {
            transformStmt.getBody().accept(this);
        }

        transformStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(transformStmtObj);
    }

    @Override
    public void visit(TransactionRollbackStmt transactionRollbackStmt) {

    }

    @Override
    public void visit(AbortStmt abortStmt) {

    }

    @Override
    public void visit(BreakStmt breakStmt) {
        JsonObject breakObject = new JsonObject();
        breakObject.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.BREAK_STATEMENT);
        this.addPosition(breakObject, breakStmt.getNodeLocation());
        this.addWhitespaceDescriptor(breakObject, breakStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(breakObject);
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        JsonObject tryCatchStmtObj = new JsonObject();
        tryCatchStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants
                .TRY_CATCH_STATEMENT);
        this.addPosition(tryCatchStmtObj, tryCatchStmt.getNodeLocation());
        this.addWhitespaceDescriptor(tryCatchStmtObj, tryCatchStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());

        if (tryCatchStmt.getTryBlock() != null) {
            tempJsonArrayRef.push(new JsonArray());

            JsonObject tryBlockObj = new JsonObject();
            tryBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.TRY_BLOCK);
            this.addPosition(tryBlockObj, tryCatchStmt.getTryBlock().getNodeLocation());
            tempJsonArrayRef.push(new JsonArray());
            tryCatchStmt.getTryBlock().accept(this);
            tryBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(tryBlockObj);

            JsonArray tryStatement = tempJsonArrayRef.peek();
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().addAll(tryStatement);
        }

        TryCatchStmt.CatchBlock[] catchBlocks = tryCatchStmt.getCatchBlocks();
        if (catchBlocks.length > 0 && catchBlocks[0] != null) {
            tempJsonArrayRef.push(new JsonArray());
            JsonObject catchBlockObj = new JsonObject();
            catchBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.CATCH_BLOCK);

            this.addPosition(catchBlockObj, catchBlocks[0].getCatchBlockStmt().getNodeLocation());
            tempJsonArrayRef.push(new JsonArray());
            catchBlocks[0].getCatchBlockStmt().accept(this);
            catchBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(catchBlockObj);

            JsonArray catchStatement = tempJsonArrayRef.peek();
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().addAll(catchStatement);
        }

        tryCatchStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(tryCatchStmtObj);
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        JsonObject throwStmtObj = new JsonObject();
        throwStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.THROW_STATEMENT);
        this.addPosition(throwStmtObj, throwStmt.getNodeLocation());
        this.addWhitespaceDescriptor(throwStmtObj, throwStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (throwStmt.getExpr() != null) {
            throwStmt.getExpr().accept(this);
        }
        throwStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(throwStmtObj);
    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        JsonObject functionInvcStmtObj = new JsonObject();
        functionInvcStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants
                .FUNCTION_INVOCATION_STATEMENT);
        this.addPosition(functionInvcStmtObj, functionInvocationStmt.getNodeLocation());
        this.addWhitespaceDescriptor(functionInvcStmtObj, functionInvocationStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
        functionInvcStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(functionInvcStmtObj);
    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {
        JsonObject actionInvocationStmtObj = new JsonObject();
        actionInvocationStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                                            BLangJSONModelConstants.ACTION_INVOCATION_STATEMENT);
        this.addWhitespaceDescriptor(actionInvocationStmtObj, actionInvocationStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        actionInvocationStmt.getActionInvocationExpr().accept(this);
        actionInvocationStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(actionInvocationStmtObj);
    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        JsonObject workerInvokeStmtObj = new JsonObject();
        workerInvokeStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                                        BLangJSONModelConstants.WORKER_INVOCATION_STATEMENT);
        // TODO: change this
        String targetWorkerName = workerInvocationStmt.getWorkerDataChannel().getChannelName().split("->")[1];
        workerInvokeStmtObj.addProperty(BLangJSONModelConstants.WORKER_NAME, targetWorkerName);
        this.addWhitespaceDescriptor(workerInvokeStmtObj, workerInvocationStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        Expression[] expressions = workerInvocationStmt.getExpressionList();
        for (Expression expression : expressions) {
            JsonObject workerInvokeExpressionObj = new JsonObject();
            workerInvokeExpressionObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.WORKER_INVOKE_EXPRESSION);
            tempJsonArrayRef.push(new JsonArray());
            expression.accept(this);
            workerInvokeExpressionObj.add(BLangJSONModelConstants.EXPRESSION, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(workerInvokeExpressionObj);
        }
        workerInvokeStmtObj.add(BLangJSONModelConstants.EXPRESSION_LIST, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(workerInvokeStmtObj);
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        JsonObject workerReplyStmtObj = new JsonObject();
        workerReplyStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants
                .WORKER_REPLY_STATEMENT);
        workerReplyStmtObj.addProperty(BLangJSONModelConstants.WORKER_NAME, workerReplyStmt.getWorkerName());
        this.addWhitespaceDescriptor(workerReplyStmtObj, workerReplyStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());

        Expression[] expressions = workerReplyStmt.getExpressionList();
        for (Expression expression : expressions) {
            JsonObject workerReplyExpressionObj = new JsonObject();
            workerReplyExpressionObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.WORKER_REPLY_EXPRESSION);
            tempJsonArrayRef.push(new JsonArray());
            expression.accept(this);
            workerReplyExpressionObj.add(BLangJSONModelConstants.EXPRESSION, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(workerReplyExpressionObj);
        }

        workerReplyStmtObj.add(BLangJSONModelConstants.EXPRESSION_LIST, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(workerReplyStmtObj);
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {

    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        JsonObject replyStmtObj = new JsonObject();
        replyStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.REPLY_STATEMENT);
        this.addPosition(replyStmtObj, replyStmt.getNodeLocation());
        this.addWhitespaceDescriptor(replyStmtObj, replyStmt.getWhiteSpaceDescriptor());
        if (isExprAsString) {
            replyStmt.accept(exprVisitor);
            String stmtExpression = exprVisitor.getBuffer().toString();
            replyStmtObj.addProperty(BLangJSONModelConstants.EXPRESSION, stmtExpression);
        }
        tempJsonArrayRef.push(new JsonArray());
        replyStmt.getReplyExpr().accept(this);
        replyStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(replyStmtObj);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        JsonObject returnStmtObj = new JsonObject();
        returnStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.RETURN_STATEMENT);
        this.addPosition(returnStmtObj, returnStmt.getNodeLocation());
        this.addWhitespaceDescriptor(returnStmtObj, returnStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (returnStmt.getExprs() != null) {
            for (Expression expression : returnStmt.getExprs()) {
                expression.accept(this);
            }
        }
        returnStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(returnStmtObj);
    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        JsonObject funcInvcObj = new JsonObject();
        funcInvcObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .FUNCTION_INVOCATION_EXPRESSION);
        funcInvcObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, funcIExpr.getPackageName());
        funcInvcObj.addProperty(BLangJSONModelConstants.PACKAGE_PATH, funcIExpr.getPackagePath());
        funcInvcObj.addProperty(BLangJSONModelConstants.FUNCTIONS_NAME, funcIExpr.getName());
        this.addPosition(funcInvcObj, funcIExpr.getNodeLocation());
        this.addWhitespaceDescriptor(funcInvcObj, funcIExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (funcIExpr.getArgExprs() != null) {
            for (Expression expression : funcIExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        funcInvcObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(funcInvcObj);
    }

    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        JsonObject actionInvcObj = new JsonObject();
        actionInvcObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .ACTION_INVOCATION_EXPRESSION);
        actionInvcObj.addProperty(BLangJSONModelConstants.ACTION_NAME, actionIExpr.getName());
        actionInvcObj.addProperty(BLangJSONModelConstants.ACTION_PKG_NAME, actionIExpr.getPackageName());
        actionInvcObj.addProperty(BLangJSONModelConstants.ACTION_CONNECTOR_NAME, actionIExpr.getConnectorName());
        this.addPosition(actionInvcObj, actionIExpr.getNodeLocation());
        this.addWhitespaceDescriptor(actionInvcObj, actionIExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (actionIExpr.getArgExprs() != null) {
            for (Expression expression : actionIExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        actionInvcObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(actionInvcObj);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        JsonObject basicLiteralObj = new JsonObject();
        basicLiteralObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                                    BLangJSONModelConstants.BASIC_LITERAL_EXPRESSION);
        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_TYPE, basicLiteral.getTypeName().getName());
        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_VALUE, basicLiteral.getBValue().stringValue
                ());
        this.addPosition(basicLiteralObj, basicLiteral.getNodeLocation());
        this.addWhitespaceDescriptor(basicLiteralObj, basicLiteral.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(basicLiteralObj);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        JsonObject divideExprObj = new JsonObject();
        divideExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.DIVISION_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        divideExpr.getLExpr().accept(this);
        divideExpr.getRExpr().accept(this);
        divideExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        this.addPosition(divideExprObj, divideExpr.getNodeLocation());
        this.addWhitespaceDescriptor(divideExprObj, divideExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(divideExprObj);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        JsonObject unaryExpr = new JsonObject();
        this.addPosition(unaryExpr, unaryExpression.getNodeLocation());
        this.addWhitespaceDescriptor(unaryExpr, unaryExpression.getWhiteSpaceDescriptor());
        unaryExpr.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.UNARY_EXPRESSION);
        unaryExpr.addProperty(BLangJSONModelConstants.EXPRESSION_OPERATOR, unaryExpression.getOperator().toString());
        tempJsonArrayRef.push(new JsonArray());
        unaryExpression.getRExpr().accept(this);
        unaryExpr.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(unaryExpr);
    }

    @Override
    public void visit(AddExpression addExpr) {
        JsonObject addExprObj = new JsonObject();
        this.addPosition(addExprObj, addExpr.getNodeLocation());
        this.addWhitespaceDescriptor(addExprObj, addExpr.getWhiteSpaceDescriptor());
        addExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.ADD_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        addExpr.getLExpr().accept(this);
        addExpr.getRExpr().accept(this);
        addExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(addExprObj);
    }

    @Override
    public void visit(SubtractExpression subExpr) {
        JsonObject minusExprObj = new JsonObject();
        this.addPosition(minusExprObj, subExpr.getNodeLocation());
        this.addWhitespaceDescriptor(minusExprObj, subExpr.getWhiteSpaceDescriptor());
        minusExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.SUBTRACT_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        subExpr.getLExpr().accept(this);
        subExpr.getRExpr().accept(this);
        minusExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(minusExprObj);
    }

    @Override
    public void visit(MultExpression multExpr) {
        JsonObject multiExprObj = new JsonObject();
        multiExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.MULTIPLY_EXPRESSION);
        this.addPosition(multiExprObj, multExpr.getNodeLocation());
        this.addWhitespaceDescriptor(multiExprObj, multExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        multExpr.getLExpr().accept(this);
        multExpr.getRExpr().accept(this);
        multiExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(multiExprObj);
    }

    @Override
    public void visit(AndExpression andExpr) {
        JsonObject andExprObj = new JsonObject();
        andExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.AND_EXPRESSION);
        this.addPosition(andExprObj, andExpr.getNodeLocation());
        this.addWhitespaceDescriptor(andExprObj, andExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        andExpr.getLExpr().accept(this);
        andExpr.getRExpr().accept(this);
        andExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(andExprObj);
    }

    @Override
    public void visit(OrExpression orExpr) {
        JsonObject orExprObj = new JsonObject();
        orExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.OR_EXPRESSION);
        this.addPosition(orExprObj, orExpr.getNodeLocation());
        this.addWhitespaceDescriptor(orExprObj, orExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        orExpr.getLExpr().accept(this);
        orExpr.getRExpr().accept(this);
        orExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(orExprObj);
    }

    @Override
    public void visit(EqualExpression equalExpr) {
        JsonObject equalExprObj = new JsonObject();
        equalExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.EQUAL_EXPRESSION);
        this.addPosition(equalExprObj, equalExpr.getNodeLocation());
        this.addWhitespaceDescriptor(equalExprObj, equalExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        equalExpr.getLExpr().accept(this);
        equalExpr.getRExpr().accept(this);
        equalExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(equalExprObj);
    }

    @Override
    public void visit(NotEqualExpression notEqualExpression) {
        JsonObject notequalExprObj = new JsonObject();
        this.addPosition(notequalExprObj, notEqualExpression.getNodeLocation());
        this.addWhitespaceDescriptor(notequalExprObj, notEqualExpression.getWhiteSpaceDescriptor());
        notequalExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                                    BLangJSONModelConstants.NOT_EQUAL_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        notEqualExpression.getLExpr().accept(this);
        notEqualExpression.getRExpr().accept(this);
        notequalExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(notequalExprObj);
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpression) {
        JsonObject greaterEqualExprObj = new JsonObject();
        this.addPosition(greaterEqualExprObj, greaterEqualExpression.getNodeLocation());
        this.addWhitespaceDescriptor(greaterEqualExprObj, greaterEqualExpression.getWhiteSpaceDescriptor());
        greaterEqualExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .GREATER_EQUAL_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        greaterEqualExpression.getLExpr().accept(this);
        greaterEqualExpression.getRExpr().accept(this);
        greaterEqualExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(greaterEqualExprObj);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        JsonObject greaterExprObj = new JsonObject();
        this.addPosition(greaterExprObj, greaterThanExpression.getNodeLocation());
        this.addWhitespaceDescriptor(greaterExprObj, greaterThanExpression.getWhiteSpaceDescriptor());
        greaterExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .GREATER_THAN_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        greaterThanExpression.getLExpr().accept(this);
        greaterThanExpression.getRExpr().accept(this);
        greaterExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(greaterExprObj);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpression) {
        JsonObject lessEqualExprObj = new JsonObject();
        this.addWhitespaceDescriptor(lessEqualExprObj, lessEqualExpression.getWhiteSpaceDescriptor());
        this.addPosition(lessEqualExprObj, lessEqualExpression.getNodeLocation());
        lessEqualExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .LESS_EQUAL_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        lessEqualExpression.getLExpr().accept(this);
        lessEqualExpression.getRExpr().accept(this);
        lessEqualExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(lessEqualExprObj);
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        JsonObject lessExprObj = new JsonObject();
        this.addPosition(lessExprObj, lessThanExpression.getNodeLocation());
        this.addWhitespaceDescriptor(lessExprObj, lessThanExpression.getWhiteSpaceDescriptor());
        lessExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.LESS_THAN_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        lessThanExpression.getLExpr().accept(this);
        lessThanExpression.getRExpr().accept(this);
        lessExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(lessExprObj);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        JsonObject variableRefObj = new JsonObject();
        this.addPosition(variableRefObj, variableRefExpr.getNodeLocation());
        this.addWhitespaceDescriptor(variableRefObj, variableRefExpr.getWhiteSpaceDescriptor());
        variableRefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                   BLangJSONModelConstants.VARIABLE_REFERENCE_EXPRESSION);
        variableRefObj.addProperty(BLangJSONModelConstants.VARIABLE_REFERENCE_NAME,
                                   variableRefExpr.getSymbolName().getName());
        variableRefObj.addProperty(BLangJSONModelConstants.VARIABLE_NAME, variableRefExpr.getSymbolName().getName());
        if (variableRefExpr.getVariableDef() != null) {
            tempJsonArrayRef.push(new JsonArray());
            variableRefExpr.getVariableDef().accept(this);
            variableRefObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        }
        tempJsonArrayRef.peek().add(variableRefObj);

    }

    @Override
    public void visit(NullLiteral nullLiteral) {

    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        JsonObject typeCastEprObj = new JsonObject();
        this.addPosition(typeCastEprObj, typeCastExpression.getNodeLocation());
        this.addWhitespaceDescriptor(typeCastEprObj, typeCastExpression.getWhiteSpaceDescriptor());
        typeCastEprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .TYPE_CAST_EXPRESSION);
        typeCastEprObj.addProperty(BLangJSONModelConstants.TARGET_TYPE, typeCastExpression.getTypeName().toString());

        tempJsonArrayRef.push(new JsonArray());
        typeCastExpression.getRExpr().accept(this);
        typeCastEprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(typeCastEprObj);
    }

    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {

    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        JsonObject arrayInitExprObj = new JsonObject();
        this.addPosition(arrayInitExprObj, arrayInitExpr.getNodeLocation());
        this.addWhitespaceDescriptor(arrayInitExprObj, arrayInitExpr.getWhiteSpaceDescriptor());
        arrayInitExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.ARRAY_INIT_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        if (arrayInitExpr.getArgExprs() != null) {
            for (Expression expression : arrayInitExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        arrayInitExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(arrayInitExprObj);
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        JsonObject refTypeInitExprObj = new JsonObject();
        refTypeInitExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .REFERENCE_TYPE_INIT_EXPR);
        this.addWhitespaceDescriptor(refTypeInitExprObj, refTypeInitExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (refTypeInitExpr.getArgExprs() != null) {
            for (Expression expression : refTypeInitExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        refTypeInitExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(refTypeInitExprObj);
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        JsonObject backtickExprObj = new JsonObject();
        this.addPosition(backtickExprObj, backtickExpr.getNodeLocation());
        this.addWhitespaceDescriptor(backtickExprObj, backtickExpr.getWhiteSpaceDescriptor());
        backtickExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                                    BLangJSONModelConstants.BACK_TICK_EXPRESSION);
        backtickExprObj.addProperty(BLangJSONModelConstants.BACK_TICK_ENCLOSED_STRING, backtickExpr.getTemplateStr());
        tempJsonArrayRef.peek().add(backtickExprObj);
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        JsonObject instanceCreationExprObj = new JsonObject();
        this.addPosition(instanceCreationExprObj, instanceCreationExpr.getNodeLocation());
        this.addWhitespaceDescriptor(instanceCreationExprObj, instanceCreationExpr.getWhiteSpaceDescriptor());
        instanceCreationExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .INSTANCE_CREATION_EXPRESSION);
        instanceCreationExprObj.addProperty(BLangJSONModelConstants.INSTANCE_CREATION_EXPRESSION_INSTANCE_TYPE,
                instanceCreationExpr.getType().toString());
        tempJsonArrayRef.push(new JsonArray());
        instanceCreationExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(instanceCreationExprObj);
    }

    @Override
    public void visit(MainInvoker mainInvoker) {
        //TODO
    }

    @Override
    public void visit(ResourceInvocationExpr resourceInvokerExpr) {
        //TODO
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        //TODO MapInitExpr should be removed from core/model. We can remove this method when it is done
    }

    @Override
    public void visit(KeyValueExpr keyValueExpr) {
        JsonObject keyValueEprObj = new JsonObject();
        this.addPosition(keyValueEprObj, keyValueExpr.getNodeLocation());
        this.addWhitespaceDescriptor(keyValueEprObj, keyValueExpr.getWhiteSpaceDescriptor());
        keyValueEprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .KEY_VALUE_EXPRESSION);
        //adding key expression
        tempJsonArrayRef.push(new JsonArray());
        keyValueExpr.getKeyExpr().accept(this);
        JsonArray keyObject = tempJsonArrayRef.pop();
        //adding value expression
        tempJsonArrayRef.push(new JsonArray());
        keyValueExpr.getValueExpr().accept(this);
        tempJsonArrayRef.peek().add(keyObject);
        keyValueEprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(keyValueEprObj);
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        JsonObject connectorInitExprObj = new JsonObject();
        this.addPosition(connectorInitExprObj, connectorInitExpr.getNodeLocation());
        this.addWhitespaceDescriptor(connectorInitExprObj, connectorInitExpr.getWhiteSpaceDescriptor());
        connectorInitExprObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                         BLangJSONModelConstants.CONNECTOR_INIT_EXPR);
        connectorInitExprObj.addProperty(BLangJSONModelConstants.CONNECTOR_NAME,
                                         connectorInitExpr.getTypeName().toString());
        tempJsonArrayRef.push(new JsonArray());
        if (connectorInitExpr.getArgExprs() != null) {
            for (Expression expression : connectorInitExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        connectorInitExprObj.add(BLangJSONModelConstants.ARGUMENTS, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(connectorInitExprObj);
    }

    @Override
    public void visit(ConstantLocation constantLocation) {
        //TODO
    }

    @Override
    public void visit(StackVarLocation stackVarLocation) {
        //TODO
    }

    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {
        //TODO
    }

    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {
        //TODO
    }

    @Override
    public void visit(ConstDef constant) {
        JsonObject constantDefinitionDefine = new JsonObject();
        this.addPosition(constantDefinitionDefine, constant.getNodeLocation());
        this.addWhitespaceDescriptor(constantDefinitionDefine, constant.getWhiteSpaceDescriptor());
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .CONSTANT_DEFINITION);
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_BTYPE,
                constant.getTypeName().getName());
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_IDENTIFIER,
                constant.getName());
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_VALUE,
                ((BasicLiteral) constant.getRhsExpr()).getBValue().stringValue());
        tempJsonArrayRef.peek().add(constantDefinitionDefine);
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        JsonObject arrayMapAccessExprObj = new JsonObject();
        this.addPosition(arrayMapAccessExprObj, arrayMapAccessExpr.getNodeLocation());
        this.addWhitespaceDescriptor(arrayMapAccessExprObj, arrayMapAccessExpr.getWhiteSpaceDescriptor());
        arrayMapAccessExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                                          BLangJSONModelConstants.ARRAY_MAP_ACCESS_EXPRESSION);
        arrayMapAccessExprObj.addProperty(BLangJSONModelConstants.ARRAY_MAP_ACCESS_EXPRESSION_NAME,
                arrayMapAccessExpr.getSymbolName().getName());

        tempJsonArrayRef.push(new JsonArray());

        Expression[] indexExprs = arrayMapAccessExpr.getIndexExprs();
        for (Expression indexExpr : indexExprs) {
            indexExpr.accept(this);
        }
        arrayMapAccessExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(arrayMapAccessExprObj);
    }

    @Override
    public void visit(StructVarLocation structVarLocation) {
        // TODO
    }

    @Override
    public void visit(WorkerVarLocation workerVarLocation) {

    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        // TODO
    }

    @Override
    public void visit(FieldAccessExpr fieldAccessExpr) {
        JsonObject fieldAccessObj = new JsonObject();
        fieldAccessObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .FIELD_ACCESS_EXPRESSION);
        fieldAccessObj.addProperty(BLangJSONModelConstants.IS_ARRAY_EXPRESSION,
                                   fieldAccessExpr.isArrayIndexExpr());
        this.addWhitespaceDescriptor(fieldAccessObj, fieldAccessExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (fieldAccessExpr.getVarRef() != null) {
            fieldAccessExpr.getVarRef().accept(this);
        }
        if (fieldAccessExpr.getFieldExpr() != null) {
            fieldAccessExpr.getFieldExpr().accept(this);
        }
        fieldAccessObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(fieldAccessObj);
    }

    @Override
    public void visit(StructDef ballerinaStruct) {
        JsonObject structObj = new JsonObject();
        this.addPosition(structObj, ballerinaStruct.getNodeLocation());
        this.addWhitespaceDescriptor(structObj, ballerinaStruct.getWhiteSpaceDescriptor());
        structObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.STRUCT_DEFINITION);
        structObj.addProperty(BLangJSONModelConstants.STRUCT_NAME, ballerinaStruct.getSymbolName().getName());
        tempJsonArrayRef.push(new JsonArray());

        if (ballerinaStruct.getAnnotations() != null) {
            for (AnnotationAttachment annotation : ballerinaStruct.getAnnotations()) {
                annotation.accept(this);
            }
        }
        if (ballerinaStruct.getFieldDefStmts() != null) {
            for (VariableDefStmt variableDefStmt : ballerinaStruct.getFieldDefStmts()) {
                variableDefStmt.accept(this);
            }
        }
        structObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(structObj);
    }

    @Override
    public void visit(AnnotationAttributeDef annotationAttributeDef) {
        JsonObject annotationAttributeDefObj = new JsonObject();
        this.addPosition(annotationAttributeDefObj, annotationAttributeDef.getNodeLocation());
        this.addWhitespaceDescriptor(annotationAttributeDefObj, annotationAttributeDef.getWhiteSpaceDescriptor());
        annotationAttributeDefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                              BLangJSONModelConstants.ANNOTATION_ATTRIBUTE_DEFINITION);
        annotationAttributeDefObj
                .addProperty(BLangJSONModelConstants.ANNOTATION_ATTRIBUTE_NAME, annotationAttributeDef.getName());
        annotationAttributeDefObj.addProperty(BLangJSONModelConstants.ANNOTATION_ATTRIBUTE_TYPE,
                                              annotationAttributeDef.getTypeName().getSymbolName().getName());
        annotationAttributeDefObj.addProperty(BLangJSONModelConstants.ANNOTATION_ATTRIBUTE_PACKAGE_PATH,
                                              annotationAttributeDef.getPackagePath());

        tempJsonArrayRef.push(new JsonArray());
        if (annotationAttributeDef.getAttributeValue() != null) {
            annotationAttributeDef.getAttributeValue().accept(this);
        }
        annotationAttributeDefObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(annotationAttributeDefObj);
    }

    @Override
    public void visit(AnnotationDef annotationDef) {
        JsonObject annotationDefObj = new JsonObject();
        this.addPosition(annotationDefObj, annotationDef.getNodeLocation());
        this.addWhitespaceDescriptor(annotationDefObj, annotationDef.getWhiteSpaceDescriptor());
        annotationDefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                     BLangJSONModelConstants.ANNOTATION_DEFINITION);
        annotationDefObj.addProperty(BLangJSONModelConstants.ANNOTATION_NAME,
                                            annotationDef.getSymbolName().getName());
        if (annotationDef.getAttachmentPoints().length > 0) {
            annotationDefObj.addProperty(BLangJSONModelConstants.ANNOTATION_ATTACHMENT_POINTS, StringUtil
                    .join(annotationDef.getAttachmentPoints(), ","));
        }

        tempJsonArrayRef.push(new JsonArray());
        if (annotationDef.getAnnotations() != null) {
            for (AnnotationAttachment annotationAttachment : annotationDef.getAnnotations()) {
                annotationAttachment.accept(this);
            }
        }
        annotationDefObj.add(BLangJSONModelConstants.ANNOTATION_ATTACHMENTS, this.tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();

        tempJsonArrayRef.push(new JsonArray());
        if (annotationDef.getAttributeDefs() != null) {
            for (AnnotationAttributeDef annotationAttribute : annotationDef.getAttributeDefs()) {
                annotationAttribute.accept(this);
            }
        }
        annotationDefObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(annotationDefObj);
    }

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        JsonObject variableDefObj = new JsonObject();
        this.addPosition(variableDefObj, varDefStmt.getNodeLocation());
        this.addWhitespaceDescriptor(variableDefObj, varDefStmt.getWhiteSpaceDescriptor());
        variableDefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                                   BLangJSONModelConstants.VARIABLE_DEFINITION_STATEMENT);
        tempJsonArrayRef.push(new JsonArray());

        // Visit the left expression
        if (varDefStmt.getLExpr() != null) {
            varDefStmt.getLExpr().accept(this);
        }
        // Visit the right expression
        if (varDefStmt.getRExpr() != null) {
            varDefStmt.getRExpr().accept(this);
        }
        variableDefObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(variableDefObj);
    }

    @Override
    public void visit(ModExpression modExpression) {
        JsonObject modExprObj = new JsonObject();
        modExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.MOD_EXPRESSION);
        this.addWhitespaceDescriptor(modExprObj, modExpression.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        modExpression.getLExpr().accept(this);
        modExpression.getRExpr().accept(this);
        modExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(modExprObj);
    }
    
    @Override
    public void visit(GlobalVariableDef globalVariableDef) {

    }

    @Override
    public void visit(GlobalVarLocation globalVarLocation) {

    }

    //    public void visit(WorkerVarLocation workerVarLocation){
    //
    //    }

    private void addPosition(JsonObject jsonObj, NodeLocation nodeLocation) {
        if (nodeLocation != null) {
            jsonObj.addProperty(BLangJSONModelConstants.LINE_NUMBER, String.valueOf(nodeLocation.getLineNumber()));
        }
    }

    private void addWhitespaceDescriptor(JsonObject jsonObj, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        if (whiteSpaceDescriptor != null) {
            JsonObject wsDescriptor = whiteSpaceDescriptorToJson(whiteSpaceDescriptor);
            jsonObj.add(BLangJSONModelConstants.WHITESPACE_DESCRIPTOR, wsDescriptor);
        }
    }

    private JsonObject whiteSpaceDescriptorToJson(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        JsonObject wsDescriptor = new JsonObject();
        JsonObject regions = new JsonObject();
        whiteSpaceDescriptor.getWhiteSpaceRegions().forEach(((regionID, whitespace) -> {
            regions.addProperty(regionID.toString(), whitespace);
        }));
        wsDescriptor.add(BLangJSONModelConstants.WHITESPACE_REGIONS, regions);
        if (whiteSpaceDescriptor.getChildDescriptors().size() > 0) {
            JsonObject children = new JsonObject();
            whiteSpaceDescriptor.getChildDescriptors().forEach((childID, child) -> {
                children.add(childID, whiteSpaceDescriptorToJson(child));
            });
            wsDescriptor.add(BLangJSONModelConstants.CHILD_DESCRIPTORS, children);
        }
        return wsDescriptor;
    }


    @Override
    public void visit(JSONFieldAccessExpr jsonFieldAccessExpr) {

    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {

    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {

    }

}
