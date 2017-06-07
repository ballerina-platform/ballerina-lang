/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';
import ASTNode from './node';
import CommonUtils from '../utils/common-utils';

/**
 * Defines a connector declaration AST node.
 * Default source will be as follows : http:HTTPConnector ep = new http:HTTPConnector("http://localhost:9090");
 * @param {Object} options - arguments for a connector declaration.
 * @param {ASTNode[]} Child nodes: a variable ref expr as LHS and a connector init expr as RHS
 * @constructor
 * @augments ASTNode
 */
class ConnectorDeclaration extends ASTNode {
    constructor(options) {
        super('ConnectorDeclaration');
        this.children = _.get(options, 'childrenFactory', () => {return [];}).call();
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: ' ',
            3: '',
            4: '\n'
        };
    }

    setConnectorVariable(connectorVariable, options) {
        let varRef = this.getChildrenOfType(this.getFactory().isVariableReferenceExpression)[0];
        if (!_.isNil(varRef)) {
            varRef.setVariableName(connectorVariable, options);
        }
    }

    setConnectorType(type, options) {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            connectorInitExpr.getConnectorName().setTypeName(type, options);
        }
    }

    setConnectorPkgName(pkgName, options) {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            connectorInitExpr.getConnectorName().setPackageName(pkgName, options);
        }
    }

    setFullPackageName(fullPkgName, options) {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            connectorInitExpr.getConnectorName().setFullPackageName(fullPkgName, options);
        }
    }

    /**
     * Set connector details from the given expression.
     *
     * @param {string} expression Expression entered by user.
     * @param {object} options
     * */
    setConnectorExpression(expression, options) {
        if (!_.isNil(expression) && expression !== "") {
            var firstAssignmentIndex = expression.indexOf("=");
            var leftSide = expression.slice(0, firstAssignmentIndex);
            var rightSide = expression.slice((firstAssignmentIndex + 1), expression.length);

            if (leftSide) {
                leftSide = leftSide.trim();
                this.setConnectorPkgName(leftSide.includes(":") ?
                    leftSide.split(":", 1)[0]
                    : "");

                this.setConnectorType(leftSide.includes(":") ?
                    leftSide.split(":", 2)[1].split(" ", 1)[0]
                    : (leftSide.indexOf(" ") !== (leftSide.length - 1) ? leftSide.split(" ", 1)[0] : ""));

                this.setConnectorVariable(leftSide.includes(":") ?
                    leftSide.split(":", 2)[1].split(" ", 2)[1]
                    : (leftSide.indexOf(" ") !== (leftSide.length - 1) ? leftSide.split(" ", 2)[1] : ""));
            }
            if (rightSide) {
                rightSide = rightSide.trim();
                let args = this.getArgsFromString(rightSide.includes("(") ?
                    rightSide.split("(", 2)[1].slice(0, (rightSide.split("(", 2)[1].length - 1))
                    : "", options);
                this.setArguments(args);
            }
        }
    }

    getStringFromArgs(args) {
        return _.join(args.map((arg) => { return arg.getExpressionString(); }), ', ');
    }

    getArgsFromString(argList) {
        let stringArgs = _.split(argList, ',');
        return stringArgs.map((arg) => {
            return this.getFactory().createVariableReferenceExpression({variableName: arg.trim()})
        });
    }

    setArguments(args) {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            connectorInitExpr.setArgs(args);
        }
    }

    getArguments() {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            return connectorInitExpr.getArgs();
        }
    }

    getConnectorVariable() {
        let varRef = this.getChildrenOfType(this.getFactory().isVariableReferenceExpression)[0];
        if (!_.isNil(varRef)) {
            return varRef.getVariableName();
        }
    }

    getConnectorType() {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            return connectorInitExpr.getConnectorName().getTypeName();
        }
    }

    getConnectorPkgName() {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            return connectorInitExpr.getConnectorName().getPackageName();
        }
    }

    getFullPackageName() {
        let connectorInitExpr = this.getChildrenOfType(this.getFactory().isConnectorInitExpression)[0]
        if (!_.isNil(connectorInitExpr)) {
            return connectorInitExpr.getConnectorName().getFullPackageName();
        }
    }

    /**
     * This will return connector expression
     *
     * @return {string} expression
     * */
    getConnectorExpression() {
        return this.generateExpression();
    }

    /**
     * initialize ConnectorDeclaration from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        var self = this;
        if (!_.isNil(jsonNode.whitespace_descriptor)) {
            this.whiteSpace.currentDescriptor = jsonNode.whitespace_descriptor;
            this.whiteSpace.useDefault = false;
        }
        _.each(jsonNode.children, function (childNode) {
            let child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Generate Expression to Show on the edit textbox.
     *
     * @param {object} self Connector declaration
     * @return {string} expression
     * */
    generateExpression () {
        var expression = "";
        if (!this.shouldSkipPackageName(this.getConnectorPkgName())) {
            expression += this.getConnectorPkgName() + ":";
        }

        if (!_.isNil(this.getConnectorType())) {
            expression += this.getConnectorType() + " ";
        }

        if (!_.isNil(this.getConnectorVariable())) {
            expression += this.getConnectorVariable() + this.getWSRegion(1) + "=" + this.getWSRegion(2);
        }

        expression += "create ";

        if (!this.shouldSkipPackageName(this.getConnectorPkgName())) {
            expression += this.getConnectorPkgName() + ":";
        }

        if (!_.isNil(this.getConnectorType())) {
            expression += this.getConnectorType() + " ";
        }

        expression += "(";
        if (!_.isEmpty(this.getArguments())) {
            expression += this.getStringFromArgs(this.getArguments());
        }
        expression += ")";
        return expression;
    }

    shouldSkipPackageName(packageName) {
        return _.isUndefined(packageName) || _.isNil(packageName) ||
            _.isEqual(packageName, 'Current Package') || _.isEqual(packageName, '');
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        var self = this;
        var uniqueIDGenObject = {
            node: this,
            attributes: [{
                defaultValue: "endpoint",
                setter: this.setConnectorVariable,
                getter: this.getConnectorVariable,
                parents: [{
                    // resource/service definition.
                    node: this.parent,
                    getChildrenFunc: this.parent.getConnectionDeclarations,
                    getter: this.getConnectorVariable
                }]
            }]
        };

        if (this.getFactory().isResourceDefinition(this.parent)) {
            uniqueIDGenObject.attributes[0].parents.push({
                // service definition
                node: this.parent.parent,
                getChildrenFunc: this.parent.getConnectionDeclarations,
                getter: this.getConnectorVariable
            });
        } else if (this.getFactory().isServiceDefinition(this.parent)) {
            var resourceDefinitions = this.parent.getResourceDefinitions();
            _.forEach(resourceDefinitions, function (resourceDefinition) {
                uniqueIDGenObject.attributes[0].parents.push({
                    // resource definition
                    node: resourceDefinition,
                    getChildrenFunc: self.parent.getConnectionDeclarations,
                    getter: self.getConnectorVariable
                });
            });
        } else if (this.getFactory().isConnectorAction(this.parent)) {
            uniqueIDGenObject.attributes[0].parents.push({
                // connector definition
                node: this.parent.parent,
                getChildrenFunc: this.parent.getConnectionDeclarations,
                getter: this.getConnectorVariable
            });
        } else if (this.getFactory().isConnectorDefinition(this.parent)) {
            var connectorActions = this.parent.getConnectorActionDefinitions();
            _.forEach(connectorActions, function (connectionAction) {
                uniqueIDGenObject.attributes[0].parents.push({
                    // connector action definition
                    node: connectionAction,
                    getChildrenFunc: self.parent.getConnectionDeclarations,
                    getter: self.getConnectorVariable
                });
            });
        }

        CommonUtils.generateUniqueIdentifier(uniqueIDGenObject);
    }
}

export default ConnectorDeclaration;
