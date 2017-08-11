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
import org.ballerinalang.composer.service.workspace.api.StringUtil;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttachmentPoint;
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
import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SimpleVariableDef;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LambdaExpression;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.StringTemplateLiteral;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.XMLCommentLiteral;
import org.ballerinalang.model.expressions.XMLElementLiteral;
import org.ballerinalang.model.expressions.XMLLiteral;
import org.ballerinalang.model.expressions.XMLPILiteral;
import org.ballerinalang.model.expressions.XMLQNameExpr;
import org.ballerinalang.model.expressions.XMLSequenceLiteral;
import org.ballerinalang.model.expressions.XMLTextLiteral;
import org.ballerinalang.model.expressions.variablerefs.FieldBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.IndexBasedVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.SimpleVarRefExpr;
import org.ballerinalang.model.expressions.variablerefs.XMLAttributesRefExpr;
import org.ballerinalang.model.statements.AbortStmt;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ContinueStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.NamespaceDeclarationStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.ConstraintTypeName;
import org.ballerinalang.model.types.FunctionTypeName;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
            if (node instanceof BallerinaFunction) {
                BallerinaFunction function = (BallerinaFunction) node;
                if (function.isLambda()) {
                    continue;
                }
            }
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
        importObj.addProperty(BLangJSONModelConstants.IMPORT_AS_NAME, importPackage.getAsName());
        this.addPosition(importObj, importPackage.getNodeLocation());
        this.addWhitespaceDescriptor(importObj, importPackage.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(importObj);
    }

    @Override
    public void visit(Service service) {
        JsonObject serviceObj = new JsonObject();
        serviceObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.SERVICE_DEFINITION);
        serviceObj.addProperty(BLangJSONModelConstants.SERVICE_PROTOCOL_PKG_NAME, service.getProtocolPkgName());
        serviceObj.addProperty(BLangJSONModelConstants.SERVICE_PROTOCOL_PKG_PATH, service.getPackagePath());
        serviceObj.addProperty(BLangJSONModelConstants.SERVICE_NAME, service.getSymbolName().getName());
        this.addPosition(serviceObj, service.getNodeLocation());
        this.addWhitespaceDescriptor(serviceObj, service.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());

        if (service.getAnnotations() != null) {
            sortAnnotationList(service.getAnnotations());
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
            sortAnnotationList(connector.getAnnotations());
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
        jsonFunc.addProperty(BLangJSONModelConstants.IS_LAMBDA_FUNCTION, function.isLambda());
        jsonFunc.addProperty(BLangJSONModelConstants.HAS_RETURNS_KEYWORD, function.hasReturnsKeyword());
        this.addPosition(jsonFunc, function.getNodeLocation());
        this.addWhitespaceDescriptor(jsonFunc, function.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.push(new JsonArray());

        if (function.getAnnotations() != null) {
            sortAnnotationList(function.getAnnotations());
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
        jsonFunc.addProperty(BLangJSONModelConstants.IS_NATIVE, function.isNative());
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
            sortAnnotationList(typeMapper.getAnnotations());
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
                paramObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE,
                        generateTypeSting(parameterDef.getTypeName()));
                this.addWhitespaceDescriptor(paramObj, parameterDef.getWhiteSpaceDescriptor());
                this.tempJsonArrayRef.push(new JsonArray());
                if (parameterDef.getAnnotations() != null) {
                    sortAnnotationList(typeMapper.getAnnotations());
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
            sortAnnotationList(action.getAnnotations());
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
    public void visit(AnnotationAttachment annotationAttachment) {
        JsonObject jsonAnnotation = new JsonObject();
        jsonAnnotation.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.ANNOTATION_ATTACHMENT);
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_ATTACHMENT_NAME, annotationAttachment.getName());
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_ATTACHMENT_PACKAGE_NAME,
                annotationAttachment.getPkgName());
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_ATTACHMENT_FULL_PACKAGE_NAME,
                annotationAttachment.getPkgPath());
        this.addPosition(jsonAnnotation, annotationAttachment.getNodeLocation());
        this.addWhitespaceDescriptor(jsonAnnotation, annotationAttachment.getWhiteSpaceDescriptor());

        tempJsonArrayRef.push(new JsonArray());
        // sort the attribute map according to source positions
        Stream<Map.Entry<String, AnnotationAttributeValue>> sortedAttributes =
                annotationAttachment.getAttributeNameValuePairs().entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue((o1, o2) ->
                                    compareNodeLocations(o1.getNodeLocation(), o2.getNodeLocation())));
        sortedAttributes.collect(Collectors.toList()).forEach(this::visitAnnotationAttribute);
        jsonAnnotation.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();

        tempJsonArrayRef.peek().add(jsonAnnotation);
    }

    /**
     * Visits an annotation attribute in an annotation.
     *
     * @param annotationAttribute The attribute.
     */
    public void visitAnnotationAttribute(Map.Entry<String, AnnotationAttributeValue> annotationAttribute) {
        JsonObject jsonAnnotationAttribute = new JsonObject();
        jsonAnnotationAttribute.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.ANNOTATION_ATTRIBUTE);
        jsonAnnotationAttribute.addProperty(BLangJSONModelConstants.ANNOTATION_ATTRIBUTE_PAIR_KEY,
                annotationAttribute.getKey());

        this.addWhitespaceDescriptor(jsonAnnotationAttribute, annotationAttribute.getValue().getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        this.visitAnnotationAttributeValue(annotationAttribute.getValue());
        jsonAnnotationAttribute.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();

        tempJsonArrayRef.peek().add(jsonAnnotationAttribute);

    }

    /**
     * Visits the value of an annotation attribute pair.
     *
     * @param attributeValue The value.
     * @return A json representation of the value.
     */
    public JsonObject visitAnnotationAttributeValue(AnnotationAttributeValue attributeValue) {
        JsonObject attributeValueObj = new JsonObject();
        attributeValueObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.ANNOTATION_ATTRIBUTE_VALUE);
        this.addPosition(attributeValueObj, attributeValue.getNodeLocation());
        this.addWhitespaceDescriptor(attributeValueObj, attributeValue.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.push(new JsonArray());

        if (attributeValue.getLiteralValue() != null) {
            this.visitBValue(attributeValue.getLiteralValue());
        } else if (attributeValue.getAnnotationValue() != null) {
            attributeValue.getAnnotationValue().accept(this);
        } else if (attributeValue.getValueArray() != null) {
            Arrays.stream(attributeValue.getValueArray()).forEach(this::visitAnnotationAttributeValue);
        }

        attributeValueObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        this.tempJsonArrayRef.peek().add(attributeValueObj);
        return attributeValueObj;
    }

    /**
     * Gets the string value of a BValue.
     *
     * @param bValue The BValue
     */
    private void visitBValue(BValue bValue) {
        JsonObject bValueObj = new JsonObject();
        bValueObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.BVALUE);

        if (null != bValue.getType().getName()) {
            bValueObj.addProperty(BLangJSONModelConstants.BVALUE_TYPE, bValue.getType().getName());
        }

        if (null != bValue.stringValue()) {
            bValueObj.addProperty(BLangJSONModelConstants.BVALUE_STRING_VALUE, bValue.stringValue());
        }

        tempJsonArrayRef.peek().add(bValueObj);
    }

    @Override
    public void visit(ParameterDef parameterDef) {
        JsonObject paramObj = new JsonObject();
        paramObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.PARAMETER_DEFINITION);
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_NAME, parameterDef.getName());

        SimpleTypeName typeName = parameterDef.getTypeName();

        if (typeName instanceof ConstraintTypeName) {
            SimpleTypeName constraint = ((ConstraintTypeName) typeName).getConstraint();
            JsonObject constraintObj = new JsonObject();
            constraintObj.addProperty(BLangJSONModelConstants.TYPE_CONSTRAINT, constraint.getName());
            if (constraint.getPackageName() != null) {
                constraintObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, constraint.getPackageName());
            }
            paramObj.add(BLangJSONModelConstants.TYPE_CONSTRAINT, constraintObj);
        }

        paramObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, typeName.getPackageName());
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, generateTypeSting(parameterDef.getTypeName()));
        if (typeName instanceof FunctionTypeName) {
            paramObj.addProperty(BLangJSONModelConstants.IS_ARRAY_TYPE, false);
            paramObj.addProperty(BLangJSONModelConstants.DIMENSIONS, 0);
        } else {
            paramObj.addProperty(BLangJSONModelConstants.IS_ARRAY_TYPE, typeName.isArrayType());
            paramObj.addProperty(BLangJSONModelConstants.DIMENSIONS, typeName.getDimensions());
        }
        this.addPosition(paramObj, parameterDef.getNodeLocation());
        this.addWhitespaceDescriptor(paramObj, parameterDef.getWhiteSpaceDescriptor());
        this.tempJsonArrayRef.push(new JsonArray());
        if (parameterDef.getAnnotations() != null) {
            sortAnnotationList(parameterDef.getAnnotations());
            for (AnnotationAttachment annotation : parameterDef.getAnnotations()) {
                annotation.accept(this);
            }
        }
        paramObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        this.tempJsonArrayRef.peek().add(paramObj);
    }

    @Override
    public void visit(SimpleVariableDef variableDef) {
        JsonObject variableDefObj = new JsonObject();
        this.addPosition(variableDefObj, variableDef.getNodeLocation());
        this.addWhitespaceDescriptor(variableDefObj, variableDef.getWhiteSpaceDescriptor());
        variableDefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .VARIABLE_DEFINITION);
        variableDefObj.addProperty(BLangJSONModelConstants.VARIABLE_NAME, variableDef.getIdentifier().getName());
        variableDefObj.addProperty(BLangJSONModelConstants.IS_IDENTIFIER_LITERAL,
                variableDef.getIdentifier().isLiteral());
        SimpleTypeName typeName = variableDef.getTypeName();
        variableDefObj.addProperty(BLangJSONModelConstants.VARIABLE_TYPE, generateTypeSting(typeName));
        variableDefObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, typeName.getPackageName());
        variableDefObj.addProperty(BLangJSONModelConstants.IS_ARRAY_TYPE, typeName.isArrayType());
        variableDefObj.addProperty(BLangJSONModelConstants.DIMENSIONS, typeName.getDimensions());
        if (typeName instanceof ConstraintTypeName) {
            SimpleTypeName constraint = ((ConstraintTypeName) typeName).getConstraint();
            JsonObject constraintObj = new JsonObject();
            constraintObj.addProperty(BLangJSONModelConstants.TYPE_CONSTRAINT, constraint.getName());
            if (constraint.getPackageName() != null) {
                constraintObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, constraint.getPackageName());
            }
            variableDefObj.add(BLangJSONModelConstants.TYPE_CONSTRAINT, constraintObj);
        }
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

        assignmentStmtObj.addProperty(BLangJSONModelConstants.IS_DECLARED_WITH_VAR, assignStmt.isDeclaredWithVar());

        tempJsonArrayRef.push(new JsonArray());

        JsonObject lExprObj = new JsonObject();
        lExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.VARIABLE_REFERENCE_LIST);
        tempJsonArrayRef.push(new JsonArray());
        for (Expression expression : assignStmt.getLExprs()) {
            expression.accept(this);
        }
        lExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(lExprObj);

        // Visit the right expression
        if (assignStmt.getRExpr() != null) {
            assignStmt.getRExpr().accept(this);
        }
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
        // create if statement
        if (ifElseStmt.getThenBody() != null) {
            JsonObject ifStmtObj = new JsonObject();
            ifStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.IF_STATEMENT);
            this.addPosition(ifStmtObj, ifElseStmt.getNodeLocation());
            this.addWhitespaceDescriptor(ifStmtObj, ifElseStmt.getWhiteSpaceDescriptor()
                    .getChildDescriptor(BLangModelBuilder.IF_CLAUSE));
            tempJsonArrayRef.push(new JsonArray());
            ifElseStmt.getCondition().accept(this);
            // add condition expr of if
            ifStmtObj.add(BLangJSONModelConstants.CONDITION, tempJsonArrayRef.peek().get(0));
            tempJsonArrayRef.pop();
            tempJsonArrayRef.push(new JsonArray());
            ifElseStmt.getThenBody().accept(this);
            // add children of if block
            ifStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            // add if statement to parent if-else stmt
            ifElseStmtObj.add(BLangJSONModelConstants.IF_STATEMENT, ifStmtObj);
        }
        // create else statement
        if (ifElseStmt.getElseBody() != null) {
            JsonObject elseStmtObj = new JsonObject();
            this.addPosition(elseStmtObj, ifElseStmt.getElseBody().getNodeLocation());
            this.addWhitespaceDescriptor(elseStmtObj, ifElseStmt.getWhiteSpaceDescriptor()
                    .getChildDescriptor(BLangModelBuilder.ELSE_CLAUSE));
            elseStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.ELSE_STATEMENT);
            tempJsonArrayRef.push(new JsonArray());
            ifElseStmt.getElseBody().accept(this);
            // add children of else block
            elseStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            // add else statement to parent if-else stmt
            ifElseStmtObj.add(BLangJSONModelConstants.ELSE_STATEMENT, elseStmtObj);
        }
        // create else-if statements
        if (ifElseStmt.getElseIfBlocks().length > 0) {
            tempJsonArrayRef.push(new JsonArray());
            IfElseStmt.ElseIfBlock[] elseIfBlocks = ifElseStmt.getElseIfBlocks();
            for (IfElseStmt.ElseIfBlock elseIfBlock : elseIfBlocks) {
                JsonObject elseIfStmtObj = new JsonObject();
                elseIfStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                        BLangJSONModelConstants.ELSE_IF_STATEMENT);
                this.addPosition(elseIfStmtObj, elseIfBlock.getNodeLocation());
                this.addWhitespaceDescriptor(elseIfStmtObj, elseIfBlock.getWhiteSpaceDescriptor());
                tempJsonArrayRef.push(new JsonArray());
                elseIfBlock.getElseIfCondition().accept(this);
                // add else if condition expr
                elseIfStmtObj.add(BLangJSONModelConstants.CONDITION, tempJsonArrayRef.peek().get(0));
                tempJsonArrayRef.pop();
                tempJsonArrayRef.push(new JsonArray());
                elseIfBlock.getElseIfBody().accept(this);
                // add children of else if block
                elseIfStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
                tempJsonArrayRef.pop();
                tempJsonArrayRef.peek().add(elseIfStmtObj);
            }
            // add else if blocks to parent if-else stmt
            ifElseStmtObj.add(BLangJSONModelConstants.ELSE_IF_STATEMENTS, tempJsonArrayRef.peek());
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
        whileStmtObj.add(BLangJSONModelConstants.CONDITION, tempJsonArrayRef.peek().get(0));
        tempJsonArrayRef.pop();
        tempJsonArrayRef.push(new JsonArray());
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
    public void visit(TransactionStmt transactionStmt) {
        JsonObject transactionAbortedStmtObj = new JsonObject();
        transactionAbortedStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.TRANSACTION_ABORTED_STATEMENT);
        this.addPosition(transactionAbortedStmtObj, transactionStmt.getNodeLocation());
        this.addWhitespaceDescriptor(transactionAbortedStmtObj, transactionStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());

        if (transactionStmt.getTransactionBlock() != null) {
            tempJsonArrayRef.push(new JsonArray());

            JsonObject transactionBlockObj = new JsonObject();
            transactionBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.TRANSACTION_STATEMENT);
            this.addPosition(transactionBlockObj, transactionStmt.getTransactionBlock().getNodeLocation());
            this.addWhitespaceDescriptor(transactionBlockObj, transactionStmt.getWhiteSpaceDescriptor());
            tempJsonArrayRef.push(new JsonArray());
            transactionStmt.getTransactionBlock().accept(this);
            transactionBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(transactionBlockObj);

            JsonArray transactionStatement = tempJsonArrayRef.peek();
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().addAll(transactionStatement);
        }

        if (transactionStmt.getAbortedBlock() != null) {
            tempJsonArrayRef.push(new JsonArray());

            JsonObject abortedBlockObj = new JsonObject();
            abortedBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.ABORTED_STATEMENT);
            this.addPosition(abortedBlockObj,
                    transactionStmt.getAbortedBlock().getAbortedBlockStmt().getNodeLocation());
            this.addWhitespaceDescriptor(abortedBlockObj, transactionStmt.getWhiteSpaceDescriptor()
                    .getChildDescriptor(BLangJSONModelConstants.ABORTED_CLAUSE));
            tempJsonArrayRef.push(new JsonArray());
            transactionStmt.getAbortedBlock().getAbortedBlockStmt().accept(this);
            abortedBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(abortedBlockObj);

            JsonArray abortedStatement = tempJsonArrayRef.peek();
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().addAll(abortedStatement);
        }

        if (transactionStmt.getCommittedBlock() != null) {
            tempJsonArrayRef.push(new JsonArray());
            JsonObject committedBlockObj = new JsonObject();

            committedBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.COMMITTED_STATEMENT);
            this.addPosition(committedBlockObj,
                    transactionStmt.getCommittedBlock().getCommittedBlockStmt().getNodeLocation());
            this.addWhitespaceDescriptor(committedBlockObj, transactionStmt.getWhiteSpaceDescriptor()
                    .getChildDescriptor(BLangJSONModelConstants.COMMITTED_CLAUSE));
            tempJsonArrayRef.push(new JsonArray());
            transactionStmt.getCommittedBlock().getCommittedBlockStmt().accept(this);
            committedBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(committedBlockObj);

            JsonArray committedStatement = tempJsonArrayRef.peek();
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().addAll(committedStatement);
        }

        transactionAbortedStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(transactionAbortedStmtObj);
    }

    @Override
    public void visit(AbortStmt abortStmt) {
        JsonObject abortStatementObj = new JsonObject();
        abortStatementObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.ABORT_STATEMENT);
        this.addPosition(abortStatementObj, abortStmt.getNodeLocation());
        this.addWhitespaceDescriptor(abortStatementObj, abortStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(abortStatementObj);
    }

    @Override
    public void visit(NamespaceDeclarationStmt namespaceDeclarationStmt) {
        JsonObject namespaceDeclarationStmtObj = new JsonObject();
        namespaceDeclarationStmtObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.NAMESPACE_DECLARATION_STATEMENT);
        this.addPosition(namespaceDeclarationStmtObj, namespaceDeclarationStmt.getNodeLocation());
        this.addWhitespaceDescriptor(namespaceDeclarationStmtObj, namespaceDeclarationStmt.getWhiteSpaceDescriptor());

        tempJsonArrayRef.push(new JsonArray());
        namespaceDeclarationStmt.getNamespaceDclr().accept(this);
        namespaceDeclarationStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(namespaceDeclarationStmtObj);
    }

    @Override
    public void visit(NamespaceDeclaration namespaceDclr) {
        JsonObject namespaceDeclarationObj = new JsonObject();
        this.addPosition(namespaceDeclarationObj, namespaceDclr.getNodeLocation());
        this.addWhitespaceDescriptor(namespaceDeclarationObj, namespaceDclr.getWhiteSpaceDescriptor());
        namespaceDeclarationObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.NAMESPACE_DECLARATION);
        namespaceDeclarationObj.addProperty(BLangJSONModelConstants.NAMESPACE_IDENTIFIER,
                namespaceDclr.getIdentifier().getName());
        namespaceDeclarationObj.addProperty(BLangJSONModelConstants.NAMESPACE_NAME,
                namespaceDclr.getName());
        namespaceDeclarationObj.addProperty(BLangJSONModelConstants.NAMESPACE_URI,
                namespaceDclr.getNamespaceUri());
        namespaceDeclarationObj.addProperty(BLangJSONModelConstants.NAMESPACE_PACKAGE_PATH,
                namespaceDclr.getPackagePath());
        tempJsonArrayRef.peek().add(namespaceDeclarationObj);
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
    public void visit(ContinueStmt continueStmt) {
        JsonObject continueObject = new JsonObject();
        continueObject.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.CONTINUE_STATEMENT);
        this.addPosition(continueObject, continueStmt.getNodeLocation());
        this.addWhitespaceDescriptor(continueObject, continueStmt.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(continueObject);
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        JsonObject tryCatchStmtObj = new JsonObject();
        tryCatchStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants
                .TRY_CATCH_STATEMENT);
        this.addPosition(tryCatchStmtObj, tryCatchStmt.getNodeLocation());
        this.addWhitespaceDescriptor(tryCatchStmtObj, tryCatchStmt.getWhiteSpaceDescriptor());

        if (tryCatchStmt.getTryBlock() != null) {
            JsonObject tryBlockObj = new JsonObject();
            tryBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.TRY_BLOCK);
            this.addPosition(tryBlockObj, tryCatchStmt.getTryBlock().getNodeLocation());
            this.addWhitespaceDescriptor(tryBlockObj, tryCatchStmt.getWhiteSpaceDescriptor()
                    .getChildDescriptor("TryClause"));
            tempJsonArrayRef.push(new JsonArray());
            tryCatchStmt.getTryBlock().accept(this);
            tryBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tryCatchStmtObj.add(BLangJSONModelConstants.TRY_BLOCK, tryBlockObj);
        }

        if (tryCatchStmt.getCatchBlocks().length > 0) {
            tempJsonArrayRef.push(new JsonArray());
            TryCatchStmt.CatchBlock[] catchBlocks = tryCatchStmt.getCatchBlocks();
            for (TryCatchStmt.CatchBlock catchBlock : catchBlocks) {
                JsonObject catchBlockObj = new JsonObject();
                catchBlockObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                        BLangJSONModelConstants.CATCH_BLOCK);
                this.addPosition(catchBlockObj, catchBlock.getCatchBlockStmt().getNodeLocation());
                this.addWhitespaceDescriptor(catchBlockObj, catchBlock.getWhiteSpaceDescriptor());
                tempJsonArrayRef.push(new JsonArray());
                catchBlock.getParameterDef().accept(this);
                catchBlockObj.add(BLangJSONModelConstants.PARAMETER_DEFINITION, tempJsonArrayRef.peek());
                tempJsonArrayRef.pop();
                tempJsonArrayRef.push(new JsonArray());
                catchBlock.getCatchBlockStmt().accept(this);
                catchBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
                tempJsonArrayRef.pop();
                tempJsonArrayRef.peek().add(catchBlockObj);
            }
            // add else catch to parent try-catch
            tryCatchStmtObj.add(BLangJSONModelConstants.CATCH_BLOCKS, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        }

        if (tryCatchStmt.getFinallyBlock() != null) {
            JsonObject finallyBlockObj = new JsonObject();
            finallyBlockObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.FINALLY_BLOCK);
            this.addPosition(finallyBlockObj, tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().getNodeLocation());
            this.addWhitespaceDescriptor(finallyBlockObj, tryCatchStmt.getWhiteSpaceDescriptor()
                    .getChildDescriptor("FinallyClause"));
            tempJsonArrayRef.push(new JsonArray());
            tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
            finallyBlockObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tryCatchStmtObj.add(BLangJSONModelConstants.FINALLY_BLOCK, finallyBlockObj);
        }

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
        String targetWorkerName = workerInvocationStmt.getName();
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
        JsonObject forkJoinStmtObj = new JsonObject();
        forkJoinStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.FORK_JOIN_STATEMENT);
        this.addPosition(forkJoinStmtObj, forkJoinStmt.getNodeLocation());
        WhiteSpaceDescriptor whiteSpaceDescriptor = forkJoinStmt.getWhiteSpaceDescriptor();
        addWhitespaceDescriptor(forkJoinStmtObj, whiteSpaceDescriptor, false);
        JsonArray children = new JsonArray();
        tempJsonArrayRef.push(children);
        Worker[] workers = forkJoinStmt.getWorkers();
        for (Worker worker : workers) {
            worker.accept(this);
        }

        ForkJoinStmt.Join join = forkJoinStmt.getJoin();
        String joinType = join.getJoinType();
        if (joinType != null) {
            JsonObject joinStmtObj = new JsonObject();
            joinStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE, BLangJSONModelConstants.JOIN_STATEMENT);
            WhiteSpaceDescriptor joinWS = whiteSpaceDescriptor.getChildDescriptor(BLangModelBuilder.JOIN_CLAUSE);
            joinWS.addChildDescriptor(BLangModelBuilder.JOIN_CONDITION,
                    whiteSpaceDescriptor.getChildDescriptor(BLangModelBuilder.JOIN_CONDITION));
            WhiteSpaceDescriptor joinWorkersWS = whiteSpaceDescriptor.getChildDescriptor(
                    BLangModelBuilder.JOIN_WORKERS);
            if (joinWorkersWS != null) {
                joinWS.addChildDescriptor(BLangModelBuilder.JOIN_WORKERS, joinWorkersWS);
            }
            addWhitespaceDescriptor(joinStmtObj, joinWS);
            joinStmtObj.addProperty(BLangJSONModelConstants.JOIN_TYPE, joinType);
            joinStmtObj.addProperty(BLangJSONModelConstants.JOIN_COUNT, join.getJoinCount());
            JsonArray joinWorkers = new JsonArray();
            for (String worker : join.getJoinWorkers()) {
                joinWorkers.add(worker);
            }
            joinStmtObj.add(BLangJSONModelConstants.JOIN_WORKERS, joinWorkers);

            this.addPosition(joinStmtObj, join.getNodeLocation());

            tempJsonArrayRef.push(new JsonArray());

            tempJsonArrayRef.push(new JsonArray());
            join.getJoinResult().accept(this);
            JsonArray param = this.tempJsonArrayRef.peek();
            joinStmtObj.add(BLangJSONModelConstants.JOIN_PARAMETER, param.get(0));
            tempJsonArrayRef.pop();

            join.getJoinBlock().accept(this);
            joinStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();

            tempJsonArrayRef.peek().add(joinStmtObj);
        }

        ForkJoinStmt.Timeout timeout = forkJoinStmt.getTimeout();
        Expression timeoutExpression = timeout.getTimeoutExpression();
        if (timeoutExpression != null) {
            JsonObject timeoutStmtObj = new JsonObject();

            tempJsonArrayRef.push(new JsonArray());
            timeoutExpression.accept(this);
            JsonArray timeoutExpressionArr = this.tempJsonArrayRef.peek();
            timeoutStmtObj.add(BLangJSONModelConstants.EXPRESSION, timeoutExpressionArr.get(0));
            tempJsonArrayRef.pop();

            tempJsonArrayRef.push(new JsonArray());
            timeout.getTimeoutResult().accept(this);
            JsonArray param = this.tempJsonArrayRef.peek();
            timeoutStmtObj.add(BLangJSONModelConstants.TIMEOUT_PARAMETER, param.get(0));
            tempJsonArrayRef.pop();

            timeoutStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                    BLangJSONModelConstants.TIMEOUT_STATEMENT);

            addWhitespaceDescriptor(timeoutStmtObj,
                    whiteSpaceDescriptor.getChildDescriptor(BLangModelBuilder.TIMEOUT_CLAUSE));

            this.addPosition(timeoutStmtObj, timeout.getNodeLocation());
            tempJsonArrayRef.push(new JsonArray());
            Statement timeoutBlock = timeout.getTimeoutBlock();
            if (timeoutBlock != null) {
                timeoutBlock.accept(this);
            }

            timeoutStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(timeoutStmtObj);
        }

        forkJoinStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(forkJoinStmtObj);
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

        String basicLiteralValue;
        if ((basicLiteral.getTypeName().getName().equals("float") ||
                basicLiteral.getTypeName().getName().equals("double")) &&
                basicLiteral.getBValue().stringValue().contains("E")) {
            // float or the double has been represented in the scientific notation. We need to convert it back to
            // the normal representation
            String[] tokens = basicLiteral.getBValue().stringValue().split("E");
            int decPointIndex = tokens[0].indexOf('.');
            String tempStringRepresentation = tokens[0].substring(0, decPointIndex) +
                    tokens[0].substring(decPointIndex + 1);
            int newDecPointIndex = decPointIndex + Integer.parseInt(tokens[1]);
            basicLiteralValue = tempStringRepresentation.substring(0, newDecPointIndex) + "." +
                    tempStringRepresentation.substring(newDecPointIndex);
        } else {
            basicLiteralValue = basicLiteral.getBValue().stringValue();
        }

        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_TYPE, basicLiteral.getTypeName().getName());
        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_VALUE, basicLiteralValue);
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
    public void visit(NullLiteral nullLiteral) {
        JsonObject nullLiteralObj = new JsonObject();
        nullLiteralObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.NULL_LITERAL_EXPRESSION);
        this.addPosition(nullLiteralObj, nullLiteral.getNodeLocation());
        this.addWhitespaceDescriptor(nullLiteralObj, nullLiteral.getWhiteSpaceDescriptor());
        tempJsonArrayRef.peek().add(nullLiteralObj);
    }

    @Override
    public void visit(XMLLiteral xmlLiteral) {

    }

    @Override
    public void visit(XMLElementLiteral xmlElementLiteral) {

    }

    @Override
    public void visit(XMLCommentLiteral xmlCommentLiteral) {

    }

    @Override
    public void visit(XMLTextLiteral xmlTextLiteral) {

    }

    @Override
    public void visit(XMLPILiteral xmlpiLiteral) {

    }

    @Override
    public void visit(XMLSequenceLiteral xmlSequenceLiteral) {

    }

    @Override
    public void visit(LambdaExpression lambdaExpr) {
        JsonObject lambdaExprObj = new JsonObject();
        lambdaExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, "lambda_function_expression");
        tempJsonArrayRef.push(new JsonArray());
        lambdaExpr.getFunction().accept(this);
        lambdaExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.pop());
        tempJsonArrayRef.peek().add(lambdaExprObj);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        JsonObject typeCastEprObj = new JsonObject();
        this.addPosition(typeCastEprObj, typeCastExpression.getNodeLocation());
        this.addWhitespaceDescriptor(typeCastEprObj, typeCastExpression.getWhiteSpaceDescriptor());
        typeCastEprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .TYPE_CAST_EXPRESSION);
        typeCastEprObj.add(BLangJSONModelConstants.TARGET_TYPE,
                simpleTypeNameToJson(typeCastExpression.getTypeName()));
        typeCastEprObj.addProperty(BLangJSONModelConstants.IS_ARRAY_TYPE,
                typeCastExpression.getTypeName().isArrayType());
        typeCastEprObj.addProperty(BLangJSONModelConstants.DIMENSIONS,
                typeCastExpression.getTypeName().getDimensions());

        tempJsonArrayRef.push(new JsonArray());
        typeCastExpression.getRExpr().accept(this);
        typeCastEprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(typeCastEprObj);
    }

    @Override
    public void visit(TypeConversionExpr typeConversionExpr) {
        JsonObject typeConversionExprObj = new JsonObject();
        this.addPosition(typeConversionExprObj, typeConversionExpr.getNodeLocation());
        this.addWhitespaceDescriptor(typeConversionExprObj, typeConversionExpr.getWhiteSpaceDescriptor());
        typeConversionExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .TYPE_CONVERSION_EXPRESSION);
        typeConversionExprObj.add(BLangJSONModelConstants.TARGET_TYPE,
                simpleTypeNameToJson(typeConversionExpr.getTypeName()));

        tempJsonArrayRef.push(new JsonArray());
        typeConversionExpr.getRExpr().accept(this);
        typeConversionExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(typeConversionExprObj);
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

        tempJsonArrayRef.push(new JsonArray());
        //adding key expression
        keyValueExpr.getKeyExpr().accept(this);
        //adding value expression
        keyValueExpr.getValueExpr().accept(this);
        keyValueEprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(keyValueEprObj);
    }

    @Override
    public void visit(SimpleVarRefExpr simpleVarRefExpr) {
        JsonObject simpleVarRefExprObj = new JsonObject();
        this.addPosition(simpleVarRefExprObj, simpleVarRefExpr.getNodeLocation());
        this.addWhitespaceDescriptor(simpleVarRefExprObj, simpleVarRefExpr.getWhiteSpaceDescriptor());
        simpleVarRefExprObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.SIMPLE_VARIABLE_REFERENCE_EXPRESSION);
        simpleVarRefExprObj.addProperty(BLangJSONModelConstants.VARIABLE_REFERENCE_NAME,
                simpleVarRefExpr.getSymbolName().getName());
        simpleVarRefExprObj.addProperty(BLangJSONModelConstants.VARIABLE_NAME,
                simpleVarRefExpr.getSymbolName().getName());
        simpleVarRefExprObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, simpleVarRefExpr.getPkgName());
        if (simpleVarRefExpr.getVariableDef() != null) {
            tempJsonArrayRef.push(new JsonArray());
            simpleVarRefExpr.getVariableDef().accept(this);
            simpleVarRefExprObj.addProperty(BLangJSONModelConstants.IS_IDENTIFIER_LITERAL,
                    simpleVarRefExpr.getVariableDef().getIdentifier().isLiteral());
            simpleVarRefExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        } else if (StringUtils.containsWhitespace(simpleVarRefExpr.getSymbolName().getName())) {
            simpleVarRefExprObj.addProperty(BLangJSONModelConstants.IS_IDENTIFIER_LITERAL, true);
        }
        tempJsonArrayRef.peek().add(simpleVarRefExprObj);
    }

    @Override
    public void visit(FieldBasedVarRefExpr fieldBasedVarRefExpr) {
        JsonObject fieldBasedVarRefExprObj = new JsonObject();
        fieldBasedVarRefExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .FIELD_BASED_VAR_REF_EXPRESSION);
        this.addWhitespaceDescriptor(fieldBasedVarRefExprObj, fieldBasedVarRefExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (fieldBasedVarRefExpr.getVarRefExpr() != null) {
            fieldBasedVarRefExpr.getVarRefExpr().accept(this);
        }
        if (fieldBasedVarRefExpr.getFieldName() != null) {
            fieldBasedVarRefExprObj.addProperty(BLangJSONModelConstants.FIELD_NAME,
                    fieldBasedVarRefExpr.getFieldName());
        }
        fieldBasedVarRefExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(fieldBasedVarRefExprObj);
    }

    @Override
    public void visit(IndexBasedVarRefExpr indexBasedVarRefExpr) {
        JsonObject indexBasedVarRefExprObj = new JsonObject();
        indexBasedVarRefExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants
                .INDEX_BASED_VAR_REF_EXPRESSION);
        this.addWhitespaceDescriptor(indexBasedVarRefExprObj, indexBasedVarRefExpr.getWhiteSpaceDescriptor());
        tempJsonArrayRef.push(new JsonArray());
        if (indexBasedVarRefExpr.getVarRefExpr() != null) {
            indexBasedVarRefExpr.getVarRefExpr().accept(this);
        }
        if (indexBasedVarRefExpr.getIndexExpr() != null) {
            indexBasedVarRefExpr.getIndexExpr().accept(this);
        }
        indexBasedVarRefExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(indexBasedVarRefExprObj);
    }

    @Override
    public void visit(XMLAttributesRefExpr xmlAttributesRefExpr) {
        JsonObject xmlAttributeRefExprObj = new JsonObject();

        this.addWhitespaceDescriptor(xmlAttributeRefExprObj, xmlAttributesRefExpr.getWhiteSpaceDescriptor());
        this.addPosition(xmlAttributeRefExprObj, xmlAttributesRefExpr.getNodeLocation());

        xmlAttributeRefExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.XML_ATTRIBUTE_REF_EXPR);
        xmlAttributeRefExprObj.addProperty(BLangJSONModelConstants.XML_ATTRIBUTE_REF_IS_LHS_EXPR,
                xmlAttributesRefExpr.isLHSExpr());

        tempJsonArrayRef.push(new JsonArray());
        if (xmlAttributesRefExpr.getVarRefExpr() != null) {
            xmlAttributesRefExpr.getVarRefExpr().accept(this);
        }

        if (xmlAttributesRefExpr.getIndexExpr() != null) {
            xmlAttributesRefExpr.getIndexExpr().accept(this);
        } else if (xmlAttributesRefExpr.getRExpr() != null) {
            xmlAttributesRefExpr.getRExpr().accept(this);
        }

        xmlAttributeRefExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(xmlAttributeRefExprObj);
    }

    @Override
    public void visit(XMLQNameExpr xmlQNameRefExpr) {
        JsonObject xmlQNameExprObj = new JsonObject();
        xmlQNameExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.XML_QNAME_EXPRESSION);
        this.addPosition(xmlQNameExprObj, xmlQNameRefExpr.getNodeLocation());
        this.addWhitespaceDescriptor(xmlQNameExprObj, xmlQNameRefExpr.getWhiteSpaceDescriptor());
        xmlQNameExprObj.addProperty(BLangJSONModelConstants.XML_QNAME_LOCALNAME,
                xmlQNameRefExpr.getLocalname());
        xmlQNameExprObj.addProperty(BLangJSONModelConstants.XML_QNAME_PREFIX,
                xmlQNameRefExpr.getPrefix());
//        xmlQNameExprObj.addProperty(BLangJSONModelConstants.XML_QNAME_URI,
//                xmlQNameRefExpr.getNamepsaceUri());
        xmlQNameExprObj.addProperty(BLangJSONModelConstants.XML_QNAME_IS_LHS,
                xmlQNameRefExpr.isLHSExpr());
        xmlQNameExprObj.addProperty(BLangJSONModelConstants.XML_QNAME_IS_USED_IN_XML,
                xmlQNameRefExpr.isUsedInXML());
        tempJsonArrayRef.peek().add(xmlQNameExprObj);
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        JsonObject connectorInitExprObj = new JsonObject();
        this.addPosition(connectorInitExprObj, connectorInitExpr.getNodeLocation());
        this.addWhitespaceDescriptor(connectorInitExprObj, connectorInitExpr.getWhiteSpaceDescriptor());
        connectorInitExprObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.CONNECTOR_INIT_EXPR);
        connectorInitExprObj.add(BLangJSONModelConstants.CONNECTOR_NAME,
                simpleTypeNameToJson(connectorInitExpr.getTypeName()));
        if (connectorInitExpr.getParentConnectorInitExpr() != null) {
            tempJsonArrayRef.push(new JsonArray());
            connectorInitExpr.getParentConnectorInitExpr().accept(this);
            connectorInitExprObj.add(BLangJSONModelConstants.PARENT_CONNECTOR_INIT_EXPR,
                    tempJsonArrayRef.peek().get(0));
            tempJsonArrayRef.pop();
        }
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
    public void visit(ConstDef constantDefinition) {
        JsonObject constantDefinitionObj = new JsonObject();
        this.addPosition(constantDefinitionObj, constantDefinition.getNodeLocation());
        this.addWhitespaceDescriptor(constantDefinitionObj, constantDefinition.getWhiteSpaceDescriptor());
        constantDefinitionObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .CONSTANT_DEFINITION);
        tempJsonArrayRef.push(new JsonArray());

        constantDefinitionObj.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_BTYPE,
                constantDefinition.getTypeName().getName());
        constantDefinitionObj.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_IDENTIFIER,
                constantDefinition.getName());

        VariableDefStmt varDefStmt = constantDefinition.getVariableDefStmt();

        if (varDefStmt.getRExpr() != null) {
            varDefStmt.getRExpr().accept(this);
        }
        constantDefinitionObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());

        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(constantDefinitionObj);
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        // TODO
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
            sortAnnotationList(ballerinaStruct.getAnnotations());
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
            AnnotationAttachmentPoint[] attachmentPointsList = annotationDef.getAttachmentPoints();
            ArrayList<String> attachmentPoints = new ArrayList<String>();
            for (AnnotationAttachmentPoint annotationAttachmentPoint : attachmentPointsList) {
                attachmentPoints.add(annotationAttachmentPoint.getAttachmentPoint().getValue());
            }
            String[] attachmentPointsString = new String[attachmentPoints.size()];
            attachmentPointsString = attachmentPoints.toArray(attachmentPointsString);
            annotationDefObj.addProperty(BLangJSONModelConstants.ANNOTATION_ATTACHMENT_POINTS, StringUtil
                    .join(attachmentPointsString, ","));
        }

        tempJsonArrayRef.push(new JsonArray());
        if (annotationDef.getAnnotations() != null) {
            sortAnnotationList(annotationDef.getAnnotations());
            for (AnnotationAttachment annotationAttachment : annotationDef.getAnnotations()) {
                annotationAttachment.accept(this);
            }
        }

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
        JsonObject globalVarDef = new JsonObject();
        this.addPosition(globalVarDef, globalVariableDef.getNodeLocation());
        this.addWhitespaceDescriptor(globalVarDef, globalVariableDef.getWhiteSpaceDescriptor());
        globalVarDef.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants
                .GLOBAL_VARIABLE_DEFINITION);
        tempJsonArrayRef.push(new JsonArray());

        globalVarDef.addProperty(BLangJSONModelConstants.GLOBAL_VARIABLE_DEFINITION_BTYPE,
                globalVariableDef.getTypeName().getName());
        globalVarDef.addProperty(BLangJSONModelConstants.GLOBAL_VARIABLE_DEFINITION_IDENTIFIER,
                globalVariableDef.getIdentifier().getName());
        globalVarDef.addProperty(BLangJSONModelConstants.IS_ARRAY_TYPE,
                globalVariableDef.getTypeName().isArrayType());
        globalVarDef.addProperty(BLangJSONModelConstants.PACKAGE_NAME,
                globalVariableDef.getTypeName().getPackageName());

        VariableDefStmt varDefStmt = globalVariableDef.getVariableDefStmt();

        if (varDefStmt.getRExpr() != null) {
            varDefStmt.getRExpr().accept(this);
        }
        globalVarDef.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());

        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(globalVarDef);
    }

    private void addPosition(JsonObject jsonObj, NodeLocation nodeLocation) {
        if (nodeLocation != null) {
            jsonObj.addProperty(BLangJSONModelConstants.LINE_NUMBER, String.valueOf(nodeLocation.getLineNumber()));

            JsonObject position = new JsonObject();
            position.addProperty(BLangJSONModelConstants.START_LINE, nodeLocation.startLineNumber);
            position.addProperty(BLangJSONModelConstants.START_OFFSET, nodeLocation.startColumn);
            position.addProperty(BLangJSONModelConstants.STOP_LINE, nodeLocation.stopLineNumber);
            position.addProperty(BLangJSONModelConstants.STOP_OFFSET, nodeLocation.startColumn);

            jsonObj.add(BLangJSONModelConstants.POSITION_INFO, position);
        }
    }

    private void addWhitespaceDescriptor(JsonObject jsonObj, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        addWhitespaceDescriptor(jsonObj, whiteSpaceDescriptor, true);
    }

    private void addWhitespaceDescriptor(JsonObject jsonObj, WhiteSpaceDescriptor whiteSpaceDescriptor
            , boolean addChildren) {
        if (whiteSpaceDescriptor != null) {
            JsonObject wsDescriptor = whiteSpaceDescriptorToJson(whiteSpaceDescriptor, addChildren);
            jsonObj.add(BLangJSONModelConstants.WHITESPACE_DESCRIPTOR, wsDescriptor);
        }
    }

    private JsonObject simpleTypeNameToJson(SimpleTypeName simpleTypeName) {
        JsonObject simpleTypeNameObj = new JsonObject();
        simpleTypeNameObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.SIMPLE_TYPE_NAME);
        this.addWhitespaceDescriptor(simpleTypeNameObj, simpleTypeName.getWhiteSpaceDescriptor());
        simpleTypeNameObj.addProperty(BLangJSONModelConstants.TYPE_NAME, simpleTypeName.getName());
        simpleTypeNameObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, simpleTypeName.getPackageName());
        return simpleTypeNameObj;
    }

    private JsonObject whiteSpaceDescriptorToJson(WhiteSpaceDescriptor whiteSpaceDescriptor, boolean addChildren) {
        JsonObject wsDescriptor = new JsonObject();
        JsonObject regions = new JsonObject();
        whiteSpaceDescriptor.getWhiteSpaceRegions().forEach(((regionID, whitespace) -> {
            regions.addProperty(regionID.toString(), whitespace);
        }));
        wsDescriptor.add(BLangJSONModelConstants.WHITESPACE_REGIONS, regions);
        if (addChildren && whiteSpaceDescriptor.getChildDescriptors().size() > 0) {
            JsonObject children = new JsonObject();
            whiteSpaceDescriptor.getChildDescriptors().forEach((childID, child) -> {
                children.add(childID, whiteSpaceDescriptorToJson(child, true));
            });
            wsDescriptor.add(BLangJSONModelConstants.CHILD_DESCRIPTORS, children);
        }
        return wsDescriptor;
    }

    private void sortAnnotationList(AnnotationAttachment[] annotations) {
        Arrays.sort(annotations, (o1, o2) -> compareNodeLocations(o1.getNodeLocation(), o2.getNodeLocation()));
    }

    private int compareNodeLocations(NodeLocation l1, NodeLocation l2) {
        if (l1.getLineNumber() == l2.getLineNumber()) {
            return Integer.compare(l1.startColumn, l2.startColumn);
        } else {
            return Integer.compare(l1.getLineNumber(), l2.getLineNumber());
        }
    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {

    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {

    }

    @Override
    public void visit(StringTemplateLiteral stringTemplateLiteral) {

    }

    private String generateTypeSting(SimpleTypeName typename) {
        if (typename instanceof FunctionTypeName) {
            StringBuilder sb = new StringBuilder();
            generateTypeSting(typename, sb);
            return sb.toString();
        } else {
            return typename.getName();
        }
    }

    private void generateTypeSting(SimpleTypeName typename, StringBuilder sb) {
        if (typename instanceof FunctionTypeName) {
            sb.append("function ");
            FunctionTypeName functionTypeName = (FunctionTypeName) typename;

            getParamList(sb, functionTypeName.getParamTypes(), functionTypeName.getParamFieldNames());
            if (functionTypeName.hasReturnsKeyword()) {
                sb.append(" returns ");
            }

            SimpleTypeName[] returnParamsTypes = functionTypeName.getReturnParamsTypes();
            if (returnParamsTypes.length > 0) {
                getParamList(sb, returnParamsTypes, functionTypeName.getReturnParamFieldNames());
            }

            if (functionTypeName.isArrayType()) {
                sb.append("[]");
            }
        } else {
            if (typename.getPackageName() != null) {
                sb.append(typename.getPackageName());
                sb.append(":");
            }
            sb.append(typename.getName());
        }
    }

    private void getParamList(StringBuilder sb, SimpleTypeName[] paramTypes, String[] paramFieldNames) {
        sb.append("( ");
        for (int i = 0; i < paramTypes.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            SimpleTypeName childType = paramTypes[i];
            generateTypeSting(childType, sb);
            sb.append(" ");
            if (paramFieldNames.length == paramTypes.length) {
                sb.append(paramFieldNames[i]);
            }
        }
        sb.append(")");
    }

}
