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
import org.wso2.ballerina.core.interpreter.LocalVarLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.PositionAwareNode;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BackquoteExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.invokers.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.BType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Serializes ballerina language object model to JSON based model.
 */
public class BLangJSONModelBuilder implements NodeVisitor {

    private JsonObject jsonObj;
    private Stack<JsonArray> tempJsonArrayRef = new Stack<>();

    public BLangJSONModelBuilder(JsonObject jsonObj) {
        this.jsonObj = jsonObj;
    }

    @Override
    public void visit(BallerinaFile bFile) {

        tempJsonArrayRef.push(new JsonArray());

        //package definitions
        JsonObject pkgDefine = new JsonObject();
        pkgDefine.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.PACKAGE_DEFINITION);
        pkgDefine.addProperty(BLangJSONModelConstants.PACKAGE_NAME, bFile.getPackageName());
        tempJsonArrayRef.peek().add(pkgDefine);

        //import declarations
        if (bFile.getImportPackages() != null) {
            for (ImportPackage anImport : bFile.getImportPackages()) {
                anImport.accept(this);
            }
        }

        ArrayList<PositionAwareNode> rootElements = new ArrayList<>();

        if (bFile.getServices() != null) {
            Service[] services = new Service[bFile.getServices().size()];
            bFile.getServices().toArray(services);
            for (Service service : services) {
                rootElements.add(service);
            }
        }

        if (bFile.getFunctions() != null) {
            for (Function function : bFile.getFunctions().values()) {
                BallerinaFunction bFunction = (BallerinaFunction) function;
                rootElements.add(bFunction);
            }
        }

        if (bFile.getConnectors() != null) {
            for (BallerinaConnector connector : bFile.getConnectors()) {
                connector.accept(this);
            }
        }

        Collections.sort(rootElements, new Comparator<PositionAwareNode>() {
            @Override
            public int compare(PositionAwareNode o1, PositionAwareNode o2) {
                return Integer.compare(o1.getRelativePosition(), o2.getRelativePosition());
            }
        });

        //service definitions //connector definitions //function definition
        for (PositionAwareNode node : rootElements) {
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
        tempJsonArrayRef.peek().add(importObj);
    }

    @Override
    public void visit(Service service) {
        JsonObject serviceObj = new JsonObject();
        serviceObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.SERVICE_DEFINITION);
        serviceObj.addProperty(BLangJSONModelConstants.SERVICE_NAME, service.getSymbolName().getName());
        tempJsonArrayRef.push(new JsonArray());
        if (service.getResources() != null) {
            for (Resource resource : service.getResources()) {
                resource.accept(this);
            }
        }
        if (service.getAnnotations() != null) {
            for (Annotation annotation : service.getAnnotations()) {
                annotation.accept(this);
            }
        }
        if (service.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : service.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }
        if (service.getVariableDcls() != null) {
            for (VariableDcl variableDcl : service.getVariableDcls()) {
                variableDcl.accept(this);
            }
        }
        serviceObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(serviceObj);
    }

    @Override
    public void visit(BallerinaConnector connector) {
        JsonObject jsonConnectObj = new JsonObject();
        jsonConnectObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.CONNECTOR_DEFINITION);
        jsonConnectObj.addProperty(BLangJSONModelConstants.CONNECTOR_NAME, connector.getConnectorName().getName());
        tempJsonArrayRef.push(new JsonArray());
        if (connector.getAnnotations() != null) {
            for (Annotation annotation : connector.getAnnotations()) {
                annotation.accept(this);
            }
        }
        if (connector.getArguments() != null) {
            for (Parameter parameter : connector.getArguments()) {
                parameter.accept(this);
            }
        }
        if (connector.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : connector.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }
        if (connector.getVariables() != null) {
            for (VariableDcl variableDcl : connector.getVariables()) {
                variableDcl.accept(this);
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
        tempJsonArrayRef.push(new JsonArray());
        if (resource.getResourceAnnotations() != null) {
            for (Annotation annotation : resource.getResourceAnnotations()) {
                annotation.accept(this);
            }
        }
        if (resource.getParameters() != null) {
            for (Parameter parameter : resource.getParameters()) {
                parameter.accept(BLangJSONModelBuilder.this);
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
        if (resource.getVariableDcls() != null) {
            for (VariableDcl variableDcl : resource.getVariableDcls()) {
                variableDcl.accept(BLangJSONModelBuilder.this);
            }
        }
        resource.getResourceBody().accept(this);
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
        this.tempJsonArrayRef.push(new JsonArray());
        if (function.getAnnotations() != null) {
            for (Annotation annotation : function.getAnnotations()) {
                annotation.accept(this);
            }
        }
        if (function.getVariableDcls() != null) {
            for (VariableDcl variableDcl : function.getVariableDcls()) {
                variableDcl.accept(BLangJSONModelBuilder.this);
            }
        }
        if (function.getParameters() != null) {
            for (Parameter parameter : function.getParameters()) {
                parameter.accept(this);
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
        if (function.getReturnTypes() != null) {
            for (BType type : function.getReturnTypes()) {
                returnTypeArray.add(type.toString());
            }
        }
        returnTypeObj.add(BLangJSONModelConstants.CHILDREN, returnTypeArray);
        tempJsonArrayRef.peek().add(returnTypeObj);
        function.getFunctionBody().accept(this);
        jsonFunc.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonFunc);
    }

    @Override
    public void visit(BallerinaAction action) {
        JsonObject jsonAction = new JsonObject();
        jsonAction.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.ACTION_DEFINITION);
        jsonAction.addProperty(BLangJSONModelConstants.ACTION_NAME, action.getName());
        tempJsonArrayRef.push(new JsonArray());
        if (action.getAnnotations() != null) {
            for (Annotation annotation : action.getAnnotations()) {
                annotation.accept(this);
            }
        }
        if (action.getParameters() != null) {
            for (Parameter parameter : action.getParameters()) {
                parameter.accept(this);
            }
        }
        if (action.getVariableDcls() != null) {
            for (VariableDcl variableDcl : action.getVariableDcls()) {
                variableDcl.accept(this);
            }
        }
        jsonAction.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(jsonAction);
    }

    @Override
    public void visit(Worker worker) {
        JsonObject jsonWorker = new JsonObject();
        jsonWorker.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.WORKER_DEFINITION);
        tempJsonArrayRef.push(new JsonArray());
        if (worker.getConnectorDcls() != null) {
            for (ConnectorDcl connectDcl : worker.getConnectorDcls()) {
                connectDcl.accept(this);
            }
        }
        if (worker.getVariables() != null) {
            for (VariableDcl variableDcl : worker.getVariables()) {
                variableDcl.accept(this);
            }
        }
        if (worker.getStatements() != null) {
            for (Statement statement : worker.getStatements()) {
                statement.accept(this);
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
    public void visit(Parameter parameter) {
        JsonObject paramObj = new JsonObject();
        paramObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.PARAMETER_DEFINITION);
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_NAME, parameter.getName().getName());
        paramObj.addProperty(BLangJSONModelConstants.PARAMETER_TYPE, parameter.getType().toString());
        this.tempJsonArrayRef.push(new JsonArray());
        if (parameter.getAnnotations() != null) {
            for (Annotation annotation : parameter.getAnnotations()) {
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
        connectObj.addProperty(BLangJSONModelConstants.CONNECTOR_DCL_VARIABLE, connectorDcl.getVarName().getName());
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
    public void visit(VariableDcl variableDcl) {
        JsonObject variableDclObj = new JsonObject();
        variableDclObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE,
                BLangJSONModelConstants.VARIABLE_DECLARATION);
        variableDclObj.addProperty(BLangJSONModelConstants.VARIABLE_NAME, variableDcl.getName().getName());
        variableDclObj.addProperty(BLangJSONModelConstants.VARIABLE_TYPE, variableDcl.getType().toString());
        tempJsonArrayRef.peek().add(variableDclObj);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        JsonObject blockStmtObj = new JsonObject();
        blockStmtObj.addProperty(BLangJSONModelConstants.DEFINITION_TYPE, BLangJSONModelConstants.BLOCK_STATEMENT);
        tempJsonArrayRef.push(new JsonArray());
        if (blockStmt.getStatements() != null) {
            for (Statement statement : blockStmt.getStatements()) {
                statement.accept(this);
            }
        }
        blockStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(blockStmtObj);
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        JsonObject assignmentStmtObj = new JsonObject();
        assignmentStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.ASSIGNMENT_STATEMENT);
        tempJsonArrayRef.push(new JsonArray());
        assignStmt.getLExpr().accept(this);
        assignStmt.getRExpr().accept(this);
        assignmentStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(assignmentStmtObj);
    }

    @Override
    public void visit(CommentStmt commentStmt) {
        JsonObject commentStmtObj = new JsonObject();
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
        tempJsonArrayRef.push(new JsonArray());
        ifElseStmt.getCondition().accept(this);
        if (ifElseStmt.getThenBody() != null) {
            ifElseStmt.getThenBody().accept(this);
        }
        if (ifElseStmt.getElseBody() != null) {
            ifElseStmt.getElseBody().accept(this);
        }
        ifElseStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(ifElseStmtObj);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        JsonObject whileStmtObj = new JsonObject();
        whileStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.WHILE_STATEMENT);
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
        tempJsonArrayRef.push(new JsonArray());
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
        functionInvcStmtObj.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(functionInvcStmtObj);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        JsonObject replyStmtObj = new JsonObject();
        replyStmtObj.addProperty(BLangJSONModelConstants.STATEMENT_TYPE,
                BLangJSONModelConstants.REPLY_STATEMENT);
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
        funcInvcObj.addProperty(BLangJSONModelConstants.FUNCTIONS_NAME,
                funcIExpr.getFunctionName().getName());
        tempJsonArrayRef.push(new JsonArray());
        if (funcIExpr.getExprs() != null) {
            for (Expression expression : funcIExpr.getExprs()) {
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
                actionIExpr.getActionName().getName());
        tempJsonArrayRef.push(new JsonArray());
        if (actionIExpr.getExprs() != null) {
            for (Expression expression : actionIExpr.getExprs()) {
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
                basicLiteral.getType().toString());
        basicLiteralObj.addProperty(BLangJSONModelConstants.BASIC_LITERAL_VALUE,
                basicLiteral.getBValue().stringValue());
        tempJsonArrayRef.peek().add(basicLiteralObj);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        JsonObject unaryExpr = new JsonObject();
        unaryExpr.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.UNARY_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        unaryExpression.getRExpr().accept(this);
        unaryExpr.add(BLangJSONModelConstants.CHILDREN, tempJsonArrayRef.peek());
        tempJsonArrayRef.pop();
        tempJsonArrayRef.peek().add(unaryExpr);
    }

    @Override
    public void visit(AddExpression addExpr) {
        JsonObject addExprObj = new JsonObject();
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
        variableRefObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.VARIABLE_REFERENCE_EXPRESSION);
        variableRefObj.addProperty(BLangJSONModelConstants.VARIABLE_REFERENCE_NAME,
                variableRefExpr.getSymbolName().getName());
        tempJsonArrayRef.peek().add(variableRefObj);
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        JsonObject arrayInitExprObj = new JsonObject();
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
    public void visit(BackquoteExpr backquoteExpr) {
        JsonObject backquoteExprObj = new JsonObject();
        backquoteExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.BACK_QUOTE_EXPRESSION);
        backquoteExprObj.addProperty(BLangJSONModelConstants.BACK_QUOTE_ENCLOSED_STRING,
                backquoteExpr.getTemplateStr());
        tempJsonArrayRef.peek().add(backquoteExprObj);
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        JsonObject instanceCreationExprObj = new JsonObject();
        instanceCreationExprObj.addProperty(BLangJSONModelConstants.EXPRESSION_TYPE,
                BLangJSONModelConstants.INSTANCE_CREATION_EXPRESSION);
        tempJsonArrayRef.push(new JsonArray());
        instanceCreationExpr.getRExpr().accept(this);
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
        //TODO
    }

    @Override
    public void visit(ConstantLocation constantLocation) {
        //TODO
    }

    @Override
    public void visit(LocalVarLocation localVarLocation) {
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
    public void visit(Const constant) {
        //TODO
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        //TODO
    }

    @Override
    public void visit(KeyValueExpression arrayMapAccessExpr) {
        //TODO
    }

}
