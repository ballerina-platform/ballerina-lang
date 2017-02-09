/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['lodash', 'log','./ballerina-view','./../ast/block-statement', 'typeMapper'],
    function (_, log, BallerinaView,BlockStatement, TypeMapperRenderer) {

        var TypeMapperBlockStatementView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});

            if (_.isNil(this.getModel()) || !(this._model instanceof BlockStatement)) {
                log.error("Block Statement is undefined or is of different type." + this.getModel());
                throw "Block Statement is undefined or is of different type." + this.getModel();
            }

        };

        TypeMapperBlockStatementView.prototype = Object.create(BallerinaView.prototype);
        TypeMapperBlockStatementView.prototype.constructor = TypeMapperBlockStatementView;

        TypeMapperBlockStatementView.prototype.canVisitBlockStatementView = function (blockStatementView) {
            return true;
        };

        /**
         * Rendering the view of the Block Statement.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        TypeMapperBlockStatementView.prototype.render = function (diagramRenderingContext, mapper) {
            this._diagramRenderingContext = diagramRenderingContext;
            var self = this;

            if(!mapper) {
                mapper = new TypeMapperRenderer(self.onAttributesConnect, self.onAttributesDisConnect, this._parentView);
                this._parentView._typeMapper = mapper;
            }

            this._parentView.setOnConnectInstance(self.onAttributesConnect);
            this._parentView.setOnDisconnectInstance(self.onAttributesDisConnect);

            this._model.on('child-added', function (child) {
               this.visit(child)
            }, this)

        };

        /**
         * Calls the render method for a statements.
         * @param {statement} statement - The statement model.
         */
        TypeMapperBlockStatementView.prototype.visitStatement = function (statement) {
//            var self = this;
//            var typeMapperStatementView = new TypeMapperStatement({
//                model: statement, parentView: this
//            });

            //alert(7777);


        }


        /**
         * Receives attributes connected
         * @param connection object
         */
        TypeMapperBlockStatementView.prototype.onAttributesConnect = function (connection) {

            var assignmentStatementNode = connection.targetReference.getParent().
                returnConstructedAssignmentStatement("y","x",connection.sourceProperty,connection.targetProperty);
            var blockStatement = connection.targetReference.getParent().getBlockStatement();
            blockStatement.addChild(assignmentStatementNode);
//
//            connection.targetReference.getParent().addAssignmentStatement(assignmentStatementNode);

//            var assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
//            var leftOp = BallerinaASTFactory.createLeftOperandExpression();
//            var leftOperandExpression = "x." + connection.targetProperty;
//            leftOp.setLeftOperandExpressionString(leftOperandExpression);
//            var rightOp = BallerinaASTFactory.createRightOperandExpression();
//            var rightOperandExpression = "y." + connection.sourceProperty;
//            if (connection.isComplexMapping) {
//                rightOperandExpression = "(" + connection.complexMapperName + ":" + connection.targetType + ")" +
//                    rightOperandExpression;
//            }
//            rightOp.setRightOperandExpressionString(rightOperandExpression);
//            assignmentStmt.addChild(leftOp);
//            assignmentStmt.addChild(rightOp);
//
//            var index = _.findLastIndex(connection.targetReference.getParent().getChildren(), function (child) {
//                return BallerinaASTFactory.isVariableDeclaration(child);
//            });
//            connection.targetReference.getParent().addChild(assignmentStmt, index + 1);

        };

        /**
         * Receives the attributes disconnected
         * @param connection object
         */
        TypeMapperBlockStatementView.prototype.onAttributesDisConnect = function (connection) {

            connection.targetReference.getParent().removeAssignmentDefinition(connection.sourceProperty,
                connection.targetProperty);
        };






        return TypeMapperBlockStatementView;
});