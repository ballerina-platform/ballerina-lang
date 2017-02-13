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

package org.wso2.ballerina.tooling.service.workspace.rest.datamodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeConvertor;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.CompilationUnit;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.StructDef;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.ConnectorInitExpr;
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MapStructInitKeyValueExpr;
import org.wso2.ballerina.core.model.expressions.ModExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.RefTypeInitExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.StructFieldAccessExpr;
import org.wso2.ballerina.core.model.expressions.StructInitExpr;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.TypeCastExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;

import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


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
                anImport.accept(this);
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
        this.tempJsonArrayRef.pop();

    }

    @Override
    public void visit(ImportPackage importPackage) {
        JsonObject importObj = new JsonObject();
        importObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.IMPORT_DEFINITION);
        importObj.addProperty(BLangJSONModelConstants.IMPORT_PACKAGE_NAME, importPackage.getName());
        importObj.addProperty(BLangJSONModelConstants.IMPORT_PACKAGE_PATH, importPackage.getPath());
        this.addPosition(importObj, importPackage.getNodeLocation());
        tempJsonArrayRef.peek().add(importObj);
    }

    @Override
    public void visit(Service service) {
        JsonObject serviceObj = new JsonObject();
        serviceObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.SERVICE_DEFINITION);
        serviceObj.addProperty(BLangJSONModelConstants.SERVICE_NAME, service.getSymbolName().getName());
        this.addPosition(serviceObj, service.getNodeLocation());
        tempJsonArrayRef.push(new JsonArray());
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
        tempJsonArrayRef.push(new JsonArray());
        if (service.getAnnotations() != null) {
            for (Annotation annotation : service.getAnnotations()) {
                annotation.accept(this);
            }
        }
        serviceObj.add(BLangJSONModelConstants.ANNOTATION_DEFINITIONS, this.tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
//        if (service.getConnectorDcls() != null) {
//            for (ConnectorDcl connectDcl : service.getConnectorDcls()) {
//                connectDcl.accept(this);
//            }
//        }
//        if (service.getVariableDefs() != null) {
//            for (VariableDef variableDef : service.getVariableDefs()) {
//                variableDef.accept(this);
//            }
//        }
        serviceObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(serviceObj);
    }

    @Override
    public void visit(BallerinaConnectorDef connector) {
        JsonObject jsonConnectObj = new JsonObject();
        this.addPosition(jsonConnectObj, connector.getNodeLocation());
        jsonConnectObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.CONNECTOR_DEFINITION);
        jsonConnectObj.addProperty(BLangJSONModelConstants.CONNECTOR_NAME, connector.getSymbolName().getName());
        tempJsonArrayRef.push(new JsonArray());
        tempJsonArrayRef.push(new JsonArray());
        if (connector.getAnnotations() != null) {
            for (Annotation annotation : connector.getAnnotations()) {
                annotation.accept(this);
            }
        }
        jsonConnectObj.add(BLangJSONModelConstants.ANNOTATION_DEFINITIONS, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        if (connector.getParameterDefs() != null) {
            for (ParameterDef parameterDef : connector.getParameterDefs()) {
                parameterDef.accept(this);
            }
        }
//        if (connector.getConnectorDcls() != null) {
//            for (ConnectorDcl connectDcl : connector.getConnectorDcls()) {
//                connectDcl.accept(this);
//            }
//        }
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
        tempJsonArrayRef.push(new JsonArray());
        tempJsonArrayRef.push(new JsonArray());
        if (resource.getResourceAnnotations() != null) {
            for (Annotation annotation : resource.getResourceAnnotations()) {
                annotation.accept(this);
            }
        }
        resourceObj.add(BLangJSONModelConstants.ANNOTATION_DEFINITIONS, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        if (resource.getParameterDefs() != null) {
            for (ParameterDef parameterDef : resource.getParameterDefs()) {
                parameterDef.accept(BLangJSONModelBuilder.this);
            }
        }
        if (resource.getWorkers() != null) {
            resource.getWorkers().forEach(new Consumer<Worker>() {
                @Override
                public void accept(Worker worker) {
                    worker.accept(BLangJSONModelBuilder.this);
                }
            });
        }
        if (resource.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : resource.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }
        if (resource.getVariableDefs() != null) {
            for (VariableDef variableDef : resource.getVariableDefs()) {
                variableDef.accept(BLangJSONModelBuilder.this);
            }
        }
        if(resource.getResourceBody() != null) {
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
        this.tempJsonArrayRef.push(new JsonArray());
        this.tempJsonArrayRef.push(new JsonArray());
        if (function.getAnnotations() != null) {
            for (Annotation annotation : function.getAnnotations()) {
                annotation.accept(this);
            }
        }
        jsonFunc.add(BLangJSONModelConstants.ANNOTATION_DEFINITIONS, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        if (function.getVariableDefs() != null) {
            for (VariableDef variableDef : function.getVariableDefs()) {
                variableDef.accept(BLangJSONModelBuilder.this);
            }
        }
        if (function.getParameterDefs() != null) {
            for (ParameterDef parameterDef : function.getParameterDefs()) {
                parameterDef.accept(this);
            }
        }
        if (function.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : function.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }
        JsonObject returnTypeObj = new JsonObject();
        returnTypeObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RETURN_TYPE);
        JsonArray returnTypeArray = new JsonArray();
        if (function.getReturnParameters() != null) {
            for (ParameterDef parameterDef : function.getReturnParameters()) {
                JsonObject typeObj = new JsonObject();
                typeObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RETURN_ARGUMENT);
                typeObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, parameterDef.getTypeName().toString());
                if (parameterDef.getName() != null) {
                    typeObj.addProperty(BLangJSONModelConstants.PARAMETER_NAME, parameterDef.getName().toString());
                }
                returnTypeArray.add(typeObj);
            }
        }
        returnTypeObj.add(BLangJSONModelConstants.CHILDREN, returnTypeArray);
        tempJsonArrayRef.peek().add(returnTypeObj);
        function.getCallableUnitBody().accept(this);
        jsonFunc.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonFunc);
    }

    @Override
    public void visit(BTypeConvertor typeConvertor) {

    }

    @Override
    public void visit(BallerinaAction action) {
        JsonObject jsonAction = new JsonObject();
        this.addPosition(jsonAction, action.getNodeLocation());
        jsonAction.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.ACTION_DEFINITION);
        jsonAction.addProperty(BLangJSONModelConstants.ACTION_NAME, action.getName());
        tempJsonArrayRef.push(new JsonArray());
        tempJsonArrayRef.push(new JsonArray());
        if (action.getAnnotations() != null) {
            for (Annotation annotation : action.getAnnotations()) {
                annotation.accept(this);
            }
        }
        jsonAction.add(BLangJSONModelConstants.ANNOTATION_DEFINITIONS, this.tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        if (action.getParameterDefs() != null) {
            for (ParameterDef parameterDef : action.getParameterDefs()) {
                parameterDef.accept(this);
            }
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

        JsonObject returnTypeObj = new JsonObject();
        returnTypeObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RETURN_TYPE);
        JsonArray returnTypeArray = new JsonArray();
        if (action.getReturnParameters() != null) {
            for (ParameterDef parameterDef : action.getReturnParameters()) {
                JsonObject typeObj = new JsonObject();
                typeObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.RETURN_ARGUMENT);
                typeObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, parameterDef.getTypeName().toString());
                if (parameterDef.getName() != null) {
                    typeObj.addProperty(BLangJSONModelConstants.PARAMETER_NAME, parameterDef.getName().toString());
                }
                returnTypeArray.add(typeObj);
            }
        }
        returnTypeObj.add(BLangJSONModelConstants.CHILDREN, returnTypeArray);
        tempJsonArrayRef.peek().add(returnTypeObj);

        action.getCallableUnitBody().accept(this);
        jsonAction.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonAction);
    }

    @Override
    public void visit(Worker worker) {
        JsonObject jsonWorker = new JsonObject();
        jsonWorker.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.WORKER_DEFINITION);
        this.addPosition(jsonWorker, worker.getNodeLocation());
        tempJsonArrayRef.push(new JsonArray());
        if (worker.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : worker.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }
        if (worker.getVariables() != null) {
            for (VariableDef variableDef : worker.getVariables()) {
                variableDef.accept(this);
            }
        }
        if (worker.getStatements() != null) {
            for (Statement statement : worker.getStatements()) {
                if (isExprAsString) {
                    JsonObject jsonObject = new JsonObject();
                    statement.accept(exprVisitor);
                    jsonObject.addProperty(BLangJSONModelConstants.STATEMENT,
                            exprVisitor.getBuffer().toString());
                    tempJsonArrayRef.peek().add(jsonObject);
                } else {
                    statement.accept(this);
                }
            }
        }
        jsonWorker.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonWorker);
    }

    @Override
    public void visit(Annotation annotation) {
        JsonObject jsonAnnotation = new JsonObject();
        jsonAnnotation.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.ANNOTATION_DEFINITION);
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_NAME, annotation.getName());
        jsonAnnotation.addProperty(BLangJSONModelConstants.ANNOTATION_VALUE, annotation.getValue());
        this.addPosition(jsonAnnotation, annotation.getNodeLocation());
        this.tempJsonArrayRef.push(new JsonArray());
        if (annotation.getKeyValuePairs() != null) {
            annotation.getKeyValuePairs().forEach(new BiConsumer<String, String>() {
                @Override
                public void accept(String k, String v) {
                    JsonObject pair = new JsonObject();
                    pair.addProperty(k, v);
                    tempJsonArrayRef.peek().add(pair);
                }
            });
        }
        jsonAnnotation.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        this.tempJsonArrayRef.peek().add(jsonAnnotation);
    }

    @Override
    public void visit(ParameterDef parameterDef) {
        JsonObject paramObj = new JsonObject();
        paramObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.PARAMETER_DEFINITION);
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_NAME, parameterDef.getName());
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, parameterDef.getTypeName().getSymbolName().getName());
        this.addPosition(paramObj, parameterDef.getNodeLocation());
        this.tempJsonArrayRef.push(new JsonArray());
        if (parameterDef.getAnnotations() != null) {
            for (Annotation annotation : parameterDef.getAnnotations()) {
                annotation.accept(this);
            }
        }
        paramObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        this.tempJsonArrayRef.peek().add(paramObj);
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
        JsonObject connectObj = new JsonObject();
        connectObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.CONNECTOR_DECLARATION);
        connectObj.addProperty(BLangJSONModelConstants.CONNECTOR_DCL_NAME, connectorDcl.getConnectorName().getName());
        connectObj.addProperty(BLangJSONModelConstants.CONNECTOR_DCL_PKG_NAME, connectorDcl.getConnectorName().getPkgPath());
        connectObj.addProperty(BLangJSONModelConstants.CONNECTOR_DCL_VARIABLE, connectorDcl.getVarName().getName());
        this.addPosition(connectObj, connectorDcl.getNodeLocation());
        this.tempJsonArrayRef.push(new JsonArray());
        if (connectorDcl.getArgExprs() != null) {
            for (Expression expression : connectorDcl.getArgExprs()) {
                expression.accept(this);
            }
        }
        connectObj.add(BLangJSONModelConstants.CHILDREN, this.tempJsonArrayRef.peek());
        this.tempJsonArrayRef.pop();
        this.tempJsonArrayRef.peek().add(connectObj);
    }

    @Override
    public void visit(VariableDef variableDef) {
        JsonObject variableDclObj = new JsonObject();
        this.addPosition(variableDclObj, variableDef.getNodeLocation());
        variableDclObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.VARIABLE_DEFINITION);
        variableDclObj.addProperty(BLangJSONModelConstants.VARIABLE_NAME, variableDef.getName());
        variableDclObj.addProperty(BLangJSONModelConstants.VARIABLE_TYPE, variableDef.getTypeName().toString());
        tempJsonArrayRef.peek().add(variableDclObj);
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
        assignmentStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.ASSIGNMENT_STATEMENT);
        tempJsonArrayRef.push(new JsonArray());

        JsonObject LExprObj = new JsonObject();
        LExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, "left_operand_expression");
        tempJsonArrayRef.push(new JsonArray());
        for (Expression expression : assignStmt.getLExprs()) {
            expression.accept(this);
        }
        LExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(LExprObj);

        JsonObject RExprObj = new JsonObject();
        RExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, "right_operand_expression");
        tempJsonArrayRef.push(new JsonArray());
        assignStmt.getRExpr().accept(this);
        RExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(RExprObj);

        assignmentStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(assignmentStmtObj);
    }

    @Override
    public void visit(CommentStmt commentStmt) {
        JsonObject commentStmtObj = new JsonObject();
        this.addPosition(commentStmtObj, commentStmt.getNodeLocation());
        commentStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.COMMENT_STATEMENT);
        commentStmtObj.addProperty(BLangJSONModelConstants.COMMENT_STRING, commentStmt.getComment());
        tempJsonArrayRef.peek().add(commentStmtObj);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        JsonObject ifElseStmtObj = new JsonObject();
        ifElseStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.IF_ELSE_STATEMENT);
        this.addPosition(ifElseStmtObj, ifElseStmt.getNodeLocation());
        tempJsonArrayRef.push(new JsonArray());
        if (ifElseStmt.getThenBody() != null) {
            tempJsonArrayRef.push(new JsonArray());

            JsonObject thenBodyObj = new JsonObject();
            thenBodyObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.IF_STATEMENT_THEN_BODY);
            tempJsonArrayRef.push(new JsonArray());
            ifElseStmt.getThenBody().accept(this);
            thenBodyObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
            tempJsonArrayRef.peek().add(thenBodyObj);

            JsonObject ifConditionObj = new JsonObject();
            ifConditionObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                    BLangJSONModelConstants.IF_STATEMENT_IF_CONDITION);
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
            ifElseStmt.getElseBody().accept(this);
            ifElseStmtObj.add(BLangJSONModelConstants.ELSE_STATEMENT, tempJsonArrayRef.peek());
            tempJsonArrayRef.pop();
        }
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(ifElseStmtObj);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        JsonObject whileStmtObj = new JsonObject();
        whileStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.WHILE_STATEMENT);
        this.addPosition(whileStmtObj, whileStmt.getNodeLocation());
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
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        JsonObject functionInvcStmtObj = new JsonObject();
        functionInvcStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.FUNCTION_INVOCATION_STATEMENT);
        this.addPosition(functionInvcStmtObj, functionInvocationStmt.getNodeLocation());
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
        tempJsonArrayRef.push(new JsonArray());
        actionInvocationStmt.getActionInvocationExpr().accept(this);
        actionInvocationStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(actionInvocationStmtObj);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        JsonObject replyStmtObj = new JsonObject();
        replyStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.REPLY_STATEMENT);
        this.addPosition(replyStmtObj, replyStmt.getNodeLocation());
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
        returnStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.RETURN_STATEMENT);
        this.addPosition(returnStmtObj, returnStmt.getNodeLocation());
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
        funcInvcObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.FUNCTION_INVOCATION_EXPRESSION);
        funcInvcObj.addProperty(BLangJSONModelConstants.PACKAGE_NAME, funcIExpr.getPackageName());
        funcInvcObj.addProperty(BLangJSONModelConstants.FUNCTIONS_NAME, funcIExpr.getName());
        this.addPosition(funcInvcObj, funcIExpr.getNodeLocation());
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
        actionInvcObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.ACTION_INVOCATION_EXPRESSION);
        actionInvcObj.addProperty(BLangJSONModelConstants.ACTION_NAME,
                actionIExpr.getName());
        actionInvcObj.addProperty(BLangJSONModelConstants.ACTION_PKG_NAME,
                actionIExpr.getPackageName());
        actionInvcObj.addProperty(BLangJSONModelConstants.ACTION_CONNECTOR_NAME,
                actionIExpr.getConnectorName());
        this.addPosition(actionInvcObj, actionIExpr.getNodeLocation());
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
        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_TYPE,
                basicLiteral.getTypeName().getName());
        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_VALUE,
                basicLiteral.getBValue().stringValue());
        this.addPosition(basicLiteralObj, basicLiteral.getNodeLocation());
        tempJsonArrayRef.peek().add(basicLiteralObj);
    }

    @Override
    public void visit(DivideExpr divideExpr) {
        JsonObject divideExprObj = new JsonObject();
        divideExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                                 BLangJSONModelConstants.DIVISION_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        divideExpr.getLExpr().accept(this);
        divideExpr.getRExpr().accept(this);
        divideExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        this.addPosition(divideExprObj, divideExpr.getNodeLocation());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(divideExprObj);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        JsonObject unaryExpr = new JsonObject();
        this.addPosition(unaryExpr, unaryExpression.getNodeLocation());
        unaryExpr.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.UNARY_EXPRESSION);
        unaryExpr.addProperty(BLangJSONModelConstants.EXPRESSION_OPERATOR,
                unaryExpression.getOperator().toString());
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
        addExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.ADD_EXPRESSION);
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
        minusExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.SUBTRACT_EXPRESSION);
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
        multiExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.MULTIPLY_EXPRESSION);
        this.addPosition(multiExprObj, multExpr.getNodeLocation());
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
        andExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.AND_EXPRESSION);
        this.addPosition(andExprObj, andExpr.getNodeLocation());
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
        orExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.OR_EXPRESSION);
        this.addPosition(orExprObj, orExpr.getNodeLocation());
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
        equalExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.EQUAL_EXPRESSION);
        this.addPosition(equalExprObj, equalExpr.getNodeLocation());
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
        greaterEqualExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.GREATER_EQUAL_EXPRESSION);
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
        greaterExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.GREATER_THAN_EXPRESSION);
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
        this.addPosition(lessEqualExprObj, lessEqualExpression.getNodeLocation());
        lessEqualExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.LESS_EQUAL_EXPRESSION);
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
        lessExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.LESS_THAN_EXPRESSION);
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
        variableRefObj.addProperty(BLangJSONModelConstants.VARIABLE_REFERENCE_TYPE,
                BLangJSONModelConstants.VARIABLE_REFERENCE_NAME);
        variableRefObj.addProperty(BLangJSONModelConstants.VARIABLE_REFERENCE_NAME,
                variableRefExpr.getSymbolName().getName());
        if (variableRefExpr.getVariableDef() != null) {
            JsonObject variableDef= new JsonObject();
            variableDef.addProperty(BLangJSONModelConstants.TYPE_NAME,
                    variableRefExpr.getVariableDef().getTypeName().getSymbolName().getName());
            variableDef.addProperty(BLangJSONModelConstants.PACKAGE_NAME,
                    variableRefExpr.getVariableDef().getTypeName().getPackageName());
            variableRefObj.add(BLangJSONModelConstants.VARIABLE_DEF_OPTIONS, variableDef);
        }
        tempJsonArrayRef.peek().add(variableRefObj);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        JsonObject typeCastEprObj = new JsonObject();
        this.addPosition(typeCastEprObj, typeCastExpression.getNodeLocation());
        typeCastEprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.TYPE_CASTING_EXPRESSION);

        JsonObject targetTypeObj = new JsonObject();
        targetTypeObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.TYPE_NAME);
        targetTypeObj.addProperty(BLangJSONModelConstants.TARGET_TYPE, typeCastExpression.getTargetType().toString());
        tempJsonArrayRef.push(new JsonArray());
        if(typeCastExpression.getArgExprs() != null) {
            typeCastExpression.getArgExprs()[0].accept(this);
        }
        tempJsonArrayRef.peek().add(targetTypeObj);
        typeCastEprObj.add(BLangJSONModelConstants.CHILDREN,tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(typeCastEprObj);
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        JsonObject arrayInitExprObj = new JsonObject();
        this.addPosition(arrayInitExprObj, arrayInitExpr.getNodeLocation());
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
        refTypeInitExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.REFERENCE_TYPE_INIT_EXPR);
        tempJsonArrayRef.peek().add(refTypeInitExprObj);
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        JsonObject backquoteExprObj = new JsonObject();
        this.addPosition(backquoteExprObj, backtickExpr.getNodeLocation());
        backquoteExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.BACK_QUOTE_EXPRESSION);
        backquoteExprObj.addProperty(BLangJSONModelConstants.BACK_QUOTE_ENCLOSED_STRING,
                backtickExpr.getTemplateStr());
        tempJsonArrayRef.peek().add(backquoteExprObj);
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        JsonObject instanceCreationExprObj = new JsonObject();
        this.addPosition(instanceCreationExprObj, instanceCreationExpr.getNodeLocation());
        instanceCreationExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.INSTANCE_CREATION_EXPRESSION);
        instanceCreationExprObj.addProperty(BLangJSONModelConstants.INSTANCE_CREATION_EXPRESSION_INSTANCE_TYPE ,
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
        JsonObject mapInitExprObj = new JsonObject();
        this.addPosition(mapInitExprObj, mapInitExpr.getNodeLocation());
        mapInitExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.MAP_INIT_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        if(mapInitExpr.getArgExprs() != null) {
            for(Expression expression : mapInitExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        mapInitExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(mapInitExprObj);
    }

    @Override
    public void visit(MapStructInitKeyValueExpr keyValueExpr) {
        JsonObject keyValueEprObj = new JsonObject();
        this.addPosition(keyValueEprObj, keyValueExpr.getNodeLocation());
        keyValueEprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.KEY_VALUE_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        JsonObject keyObject= new JsonObject();
        keyObject.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE, BLangJSONModelConstants.QUOTED_LITERAL_STRING);
        keyObject.addProperty(BLangJSONModelConstants.KEY_VALUE_EXPRESSION_KEY, keyValueExpr.getKey());
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
        connectorInitExprObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.CONNECTOR_INIT_EXPR);
        connectorInitExprObj.addProperty(BLangJSONModelConstants.CONNECTOR_NAME, connectorInitExpr.getTypeName().toString());
        tempJsonArrayRef.push(new JsonArray());
        if(connectorInitExpr.getArgExprs() != null) {
            for(Expression expression : connectorInitExpr.getArgExprs()) {
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
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.CONSTANT_DEFINITION);
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_BTYPE,
                constant.getTypeName().getName());
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_IDENTIFIER,
                constant.getName());
        constantDefinitionDefine.addProperty(BLangJSONModelConstants.CONSTANT_DEFINITION_VALUE,
                ((BasicLiteral)constant.getRhsExpr()).getBValue().stringValue());
        tempJsonArrayRef.peek().add(constantDefinitionDefine);
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        JsonObject arrayMapAccessExprObj = new JsonObject();
        this.addPosition(arrayMapAccessExprObj, arrayMapAccessExpr.getNodeLocation());
        arrayMapAccessExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                                   BLangJSONModelConstants.ARRAY_MAP_ACCESS_EXPRESSION);
        arrayMapAccessExprObj.addProperty(BLangJSONModelConstants.ARRAY_MAP_ACCESS_EXPRESSION_NAME,
                                   arrayMapAccessExpr.getSymbolName().getName());

        tempJsonArrayRef.push(new JsonArray());
        arrayMapAccessExpr.getIndexExpr().accept(this);
        arrayMapAccessExprObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(arrayMapAccessExprObj);
    }

    @Override
    public void visit(StructVarLocation structVarLocation) {
        // TODO
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        // TODO
    }

    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        // TODO
    }

    @Override
    public void visit(StructDef ballerinaStruct) {
        JsonObject structObj = new JsonObject();
        this.addPosition(structObj, ballerinaStruct.getNodeLocation());
        structObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.STRUCT_DEFINITION);
        structObj.addProperty(BLangJSONModelConstants.STRUCT_NAME, ballerinaStruct.getSymbolName().getName());
        tempJsonArrayRef.push(new JsonArray());
        if (ballerinaStruct.getFields() != null) {
            for (VariableDef variableDef : ballerinaStruct.getFields()) {
                variableDef.accept(this);
            }
        }
        structObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(structObj);
    }

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        JsonObject variableDefObj = new JsonObject();
        this.addPosition(variableDefObj, varDefStmt.getNodeLocation());
        variableDefObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.VARIABLE_DEFINITION_STATEMENT);
        tempJsonArrayRef.push(new JsonArray());
        JsonObject childrenObj = new JsonObject();

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
        
    }

    private void addPosition (JsonObject jsonObj, NodeLocation nodeLocation) {
        if (nodeLocation != null) {
            jsonObj.addProperty(BLangJSONModelConstants.FILE_NAME, nodeLocation.getFileName());
            jsonObj.addProperty(BLangJSONModelConstants.LINE_NUMBER, String.valueOf(nodeLocation.getLineNumber()));
        }
    }
}
