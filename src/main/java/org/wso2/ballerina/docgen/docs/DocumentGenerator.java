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
package org.wso2.ballerina.docgen.docs;

import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.LocalVarLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.SymScope;
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
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.DivideExpr;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
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
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
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
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.docgen.docs.model.BallerinaActionDoc;
import org.wso2.ballerina.docgen.docs.model.BallerinaConnectorDoc;
import org.wso2.ballerina.docgen.docs.model.BallerinaFunctionDoc;
import org.wso2.ballerina.docgen.docs.model.BallerinaPackageDoc;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocUtils;

/**
 * {@code DocumentGenerator} generates documents for a Ballerina program
 *
 * @since 0.8.0
 */
public class DocumentGenerator implements NodeVisitor {
    private String currentPkg;
    private BallerinaPackageDoc ballerinaDoc;

    public DocumentGenerator(BallerinaFile bFile, SymScope globalScope) {
        currentPkg = bFile.getPackageName();
        ballerinaDoc = BallerinaDocDataHolder.getInstance().getBallerinaDocsMap().get(currentPkg);
        if (ballerinaDoc == null) {
            ballerinaDoc = new BallerinaPackageDoc(currentPkg);
            BallerinaDocDataHolder.getInstance().getBallerinaDocsMap().put(currentPkg, ballerinaDoc);
        }
    }

    @Override
    public void visit(BallerinaFile bFile) {
        for (ImportPackage importPkg : bFile.getImportPackages()) {
            importPkg.accept(this);
        }

        // Analyze and allocate static memory locations for constants
        for (Const constant : bFile.getConstants()) {
            constant.accept(this);
        }

        for (BallerinaConnector connector : bFile.getConnectorList()) {
            connector.accept(this);
        }

        for (Service service : bFile.getServices()) {
            service.accept(this);
        }

        for (Function function : bFile.getFunctions()) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.accept(this);
        }

    }

    @Override
    public void visit(ImportPackage importPkg) {
    }

    @Override
    public void visit(Const constant) {
    }

    @Override
    public void visit(Service service) {
    }

    @Override
    public void visit(BallerinaConnector connector) {
        BallerinaConnectorDoc doc = new BallerinaConnectorDoc(connector);
        BallerinaDocDataHolder.getInstance().getBallerinaDocsMap().get(currentPkg)
                .addBallerinaConnectorDoc(doc);

        StringBuilder s = new StringBuilder(connector.getConnectorName() + " (");
        for (Parameter p : connector.getParameters()) {
            s.append(BallerinaDocUtils.getType(p.getType()) + " " + p.getName() + ",");
        }
        doc.setSignature(s.substring(0, s.length() - 1).concat(")"));

        for (BallerinaAction action : connector.getActions()) {
            BallerinaActionDoc actionDoc = new BallerinaActionDoc(action);

            s = new StringBuilder(connector.getConnectorName() + " (");
            for (Parameter p : connector.getParameters()) {
                s.append(BallerinaDocUtils.getType(p.getType()) + " " + p.getName() + ",");
            }
            actionDoc.setSignature(s.substring(0, s.length() - 1).concat(")"));

            s = new StringBuilder();
            for (Parameter p : action.getReturnParameters()) {
                s.append(BallerinaDocUtils.getType(p.getType()) + ",");
            }
            actionDoc.setReturnTypes(s.length() == 0 ? "" : s.substring(0, s.length() - 1));

            for (Annotation annotation : action.getAnnotations()) {
                if (annotation.getName().equalsIgnoreCase("param")) {
                    actionDoc.getParameters().add(annotation.getValue());
                } else if (annotation.getName().equalsIgnoreCase("description")) {
                    actionDoc.setDescription(annotation.getValue());
                } else if (annotation.getName().equalsIgnoreCase("return")) {
                    actionDoc.getReturnParams().add(annotation.getValue());
                } else if (annotation.getName().equalsIgnoreCase("throws")) {
                    actionDoc.getThrownExceptions().add(annotation.getValue());
                }
            }
            doc.addAction(actionDoc);
        }

        for (Annotation annotation : connector.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("param")) {
                doc.getParameters().add(annotation.getValue());
            } else if (annotation.getName().equalsIgnoreCase("description")) {
                doc.setDescription(annotation.getValue());
            }
        }
    }

    @Override
    public void visit(Resource resource) {
    }

    @Override
    public void visit(BallerinaFunction function) {
        BallerinaFunctionDoc doc = new BallerinaFunctionDoc();
        BallerinaDocDataHolder.getInstance().getBallerinaDocsMap().get(currentPkg).addBallerinaFunctionDoc(doc);

        StringBuilder s = new StringBuilder(function.getFunctionName() + " (");
        for (Parameter parameter : function.getParameters()) {
            s.append(BallerinaDocUtils.getType(parameter.getType()) + " " + parameter.getName() + ",");
        }
        doc.setSignature(s.substring(0, s.length() - 1).concat(")"));

        s = new StringBuilder();
        for (Parameter parameter : function.getReturnParameters()) {
            s.append(BallerinaDocUtils.getType(parameter.getType()) + ",");
        }
        doc.setReturnType(s.length() == 0 ? "" : s.substring(0, s.length() - 1));

        for (Annotation annotation : function.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("param")) {
                doc.getParameters().add(annotation.getValue());
            } else if (annotation.getName().equalsIgnoreCase("description")) {
                doc.setDescription(annotation.getValue());
            } else if (annotation.getName().equalsIgnoreCase("return")) {
                doc.getReturnParams().add(annotation.getValue());
            } else if (annotation.getName().equalsIgnoreCase("throws")) {
                doc.getThrownExceptions().add(annotation.getValue());
            }
        }
    }

    @Override
    public void visit(BallerinaAction action) {
    }

    @Override
    public void visit(Worker worker) {

    }

    @Override
    public void visit(Annotation annotation) {

    }

    @Override
    public void visit(Parameter parameter) {
    }

    @Override
    public void visit(VariableDcl variableDcl) {
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
    }

    @Override
    public void visit(AssignStmt assignStmt) {
    }

    @Override
    public void visit(BlockStmt blockStmt) {
    }

    @Override
    public void visit(CommentStmt commentStmt) {
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
    }

    @Override
    public void visit(WhileStmt whileStmt) {
    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
    }

    @Override
    public void visit(AddExpression arg0) {
    }

    @Override
    public void visit(AndExpression arg0) {
    }

    @Override
    public void visit(BasicLiteral arg0) {
    }

    @Override
    public void visit(DivideExpr arg0) {
    }

    @Override
    public void visit(EqualExpression arg0) {
    }

    @Override
    public void visit(FunctionInvocationExpr arg0) {
    }

    @Override
    public void visit(ActionInvocationExpr arg0) {
    }

    @Override
    public void visit(GreaterEqualExpression arg0) {
    }

    @Override
    public void visit(GreaterThanExpression arg0) {
    }

    @Override
    public void visit(LessEqualExpression arg0) {
    }

    @Override
    public void visit(LessThanExpression arg0) {
    }

    @Override
    public void visit(MultExpression arg0) {
    }

    @Override
    public void visit(InstanceCreationExpr arg0) {
    }

    @Override
    public void visit(NotEqualExpression arg0) {
    }

    @Override
    public void visit(OrExpression arg0) {
    }

    @Override
    public void visit(SubtractExpression arg0) {
    }

    @Override
    public void visit(UnaryExpression arg0) {
    }

    @Override
    public void visit(ArrayMapAccessExpr arg0) {
    }

    @Override
    public void visit(ArrayInitExpr arg0) {
    }

    @Override
    public void visit(MapInitExpr arg0) {
    }

    @Override
    public void visit(KeyValueExpression arg0) {
    }

    @Override
    public void visit(BacktickExpr arg0) {
    }

    @Override
    public void visit(VariableRefExpr arg0) {
    }

    @Override
    public void visit(LocalVarLocation arg0) {
    }

    @Override
    public void visit(ServiceVarLocation arg0) {
    }

    @Override
    public void visit(ConnectorVarLocation arg0) {
    }

    @Override
    public void visit(ConstantLocation arg0) {
    }

    @Override
    public void visit(ResourceInvocationExpr arg0) {
    }

    @Override
    public void visit(MainInvoker arg0) {
    }

}
