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
define(['lodash', 'log','./ballerina-view','ballerina/ast/ballerina-ast-factory', 'typeMapper','./../ast/statement'],
    function (_, log,BallerinaView, BallerinaASTFactory, TypeMapper,Statement) {

        var TypeMapperStatementView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
            this._typeMapperRenderer = _.get(args, 'typeMapperRenderer');
            this._sourceInfo = _.get(args, 'sourceInfo', {});
            this._targetInfo = _.get(args, 'targetInfo', {});

            if (_.isNil(this.getModel()) || !(this._model instanceof Statement)) {
                log.error("Type Mapper Statement is undefined or is of different type." + this.getModel());
                throw "Type Mapper Statement is undefined or is of different type." + this.getModel();
            }
        };

        TypeMapperStatementView.prototype = Object.create(BallerinaView.prototype);
        TypeMapperStatementView.prototype.constructor = TypeMapperStatementView;

        TypeMapperStatementView.prototype.canVisitTypeMapperStatementView = function (typeMapperStatementView) {
            return true;
        };

        /**
         * Rendering the view of the Statement.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        TypeMapperStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var self = this;
            var connection = self.getConnectionSchema(self.getModel());
            this.getTypeMapperRenderer().addConnection(connection);

            this._model.on('child-added', function (child) {
                this.visit(child)
            }, this)

        };

        /**
         * returns the call back function to be called when a connection is drawn
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        /**
         * set the call back function for connecting a source and a target
         * @param onConnectInstance
         */
        TypeMapperStatementView.prototype.setOnConnectInstance = function (onConnectInstance) {
            if (!_.isNil(onConnectInstance)) {
                this._onConnectInstance = onConnectInstance;
            } else {
                log.error('Invalid onConnectInstance [' + onConnectInstance + '] Provided');
                throw 'Invalid onConnectInstance [' + onConnectInstance + '] Provided';
            }
        };

        /**
         * returns the call back function to be called when a connection is removed
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        /**
         * set the call back function for disconnecting a source and a target
         * @param onDisconnectInstance
         */
        TypeMapperStatementView.prototype.setOnDisconnectInstance = function (onDisconnectInstance) {
            if (!_.isNil(onDisconnectInstance)) {
                this._onDisconnectInstance = onDisconnectInstance;
            } else {
                log.error('Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided');
                throw 'Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided';
            }
        };

        /**
         * returns the type mapper renderer
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getTypeMapperRenderer = function () {
            return this._typeMapperRenderer;
        };

        /**
         * returns the constructed connection schema to be drawn
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getConnectionSchema = function (assignmentStatement) {

            var self = this;
            var sourceStructSchema = self.getSourceInfo().sourceStruct;
            var sourceStructName = self.getSourceInfo().sourceStructName;
            var sourcePropertyType;
            var targetStructSchema = self.getTargetInfo().targetStruct;
            var targetStructName = self.getTargetInfo().targetStructName;
            var targetPropertyType;


            var leftOperandExpression = _.find(this.getModel().getChildren(), function (child) {
                return BallerinaASTFactory.isLeftOperandExpression(child);
            });

            var sourceStructFieldAccessExpression = _.find(leftOperandExpression.getChildren(), function (child) {
                return BallerinaASTFactory.isStructFieldAccessExpression(child);
            });

            var sourcefieldExpression = _.find(sourceStructFieldAccessExpression.getChildren(), function (child) {
                return BallerinaASTFactory.isFieldExpression(child);
            });

            var sourceVariableReferenceExpression = _.find(sourcefieldExpression.getChildren(), function (child) {
                return BallerinaASTFactory.isVariableReferenceExpression(child);
            });

            var sourcePropertyName = sourceVariableReferenceExpression.getVariableReferenceName()[0];

            _.each(sourceStructSchema.getAttributesArray().properties, function(property) {
                if(property.name == sourcePropertyName){
                    sourcePropertyType = property.type;
                    return false;
                }
            });

            var rightOperandExpression = _.find(this.getModel().getChildren(), function (child) {
                return BallerinaASTFactory.isRightOperandExpression(child);
            });

            var targetStructFieldAccessExpression = _.find(rightOperandExpression.getChildren(), function (child) {
                return BallerinaASTFactory.isStructFieldAccessExpression(child);
            });

            var targetfieldExpression = _.find(targetStructFieldAccessExpression.getChildren(), function (child) {
                return BallerinaASTFactory.isFieldExpression(child);
            });

            var targetVariableReferenceExpression = _.find(targetfieldExpression.getChildren(), function (child) {
                return BallerinaASTFactory.isVariableReferenceExpression(child);
            });

            var targetPropertyName = targetVariableReferenceExpression.getVariableReferenceName()[0];

            _.each(targetStructSchema.getAttributesArray().properties, function(property) {
                if(property.name == targetPropertyName){
                    targetPropertyType = property.type;
                    return false;
                }
            });

            var connectionSchema = {};
            connectionSchema["sourceStruct"] = sourceStructName;
            connectionSchema["sourceProperty"] = sourcePropertyName;
            connectionSchema["sourceType"] = sourcePropertyType;
            connectionSchema["targetStruct"] = targetStructName;
            connectionSchema["targetProperty"] = targetPropertyName;
            connectionSchema["targetType"] = targetPropertyType;

            return connectionSchema;
        };

        /**
         * returns the parent view
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getParentView = function () {
            return this._parentView;
        };

        /**
         * returns the source info
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getSourceInfo = function () {
            return this._sourceInfo;
        };

        /**
         * returns the source info
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getTargetInfo = function () {
            return this._targetInfo;
        };

        /**
         * returns the model
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getModel = function () {
            return this._model;
        };

        /**
         * returns the type mapper renderer
         * @returns {object}
         */
        TypeMapperStatementView.prototype.getTypeMapperRenderer = function () {
            return this._typeMapperRenderer;
        };

        return TypeMapperStatementView;
});