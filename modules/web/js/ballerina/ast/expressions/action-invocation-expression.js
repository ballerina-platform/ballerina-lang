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
import Expression from './expression';
import FragmentUtils from '../../utils/fragment-utils';

/**
 * Class to represent a action invocation to ballerina.
 * @param args
 * @constructor
 */
class ActionInvocationExpression extends Expression {
    constructor(args) {
        super('ActionInvocationExpression');
        this._actionName = _.get(args, 'action', undefined);
        this._actionPackageName = _.get(args, 'actionPackageName', undefined);
        this._actionConnectorName = _.get(args, 'actionConnectorName', undefined);
        this._arguments = _.get(args, 'arguments', []);
        this._connector = _.get(args, 'connector');
        this._connectorExpr = _.get(args, 'connectorExpr');
        this._connectorName = _.get(args, 'connectorName');
        this._fullPackageName = _.get(args, 'fullPackageName', undefined);
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: '',
            3: '',
            4: '',
            5: '',
        };
        this.whiteSpace.defaultDescriptor.children = {
            nameRef: {
                0: ' ',
                1: '',
                2: '',
                3: '',
            },
        };

        this.type = 'ActionInvocationExpression';
    }

    /**
     * Set the full package name
     * @param {String} fullPkgName full package name
     * @param {Object} options
     * */
    setFullPackageName(fullPkgName, options) {
        this.setAttribute('_fullPackageName', fullPkgName, options);
    }

    /**
     * Get the full package name
     * @return {String} full package name
     * */
    getFullPackageName() {
        return this._fullPackageName;
    }

    /**
     * Set action name
     * @param {string} actionName
     */
    setActionName(actionName, options) {
        this.setAttribute('_actionName', actionName, options);
    }

    /**
     * Get action name
     * @returns {string}
     */
    getActionName() {
        return this._actionName;
    }

    /**
     * Set Action package name
     * @param {string} actionPackageName
     */
    setActionPackageName(actionPackageName, options) {
        this.setAttribute('_actionPackageName', actionPackageName, options);
    }

    /**
     * Get Action Package Name
     * @returns {string}
     */
    getActionPackageName() {
        return this._actionPackageName;
    }

    /**
     * Set Action Connector name
     * @param {string} actionConnectorName
     */
    setActionConnectorName(actionConnectorName, options) {
        this.setAttribute('_actionConnectorName', actionConnectorName, options);
    }

    /**
     * Get action connector Name
     * @returns {string}
     */
    getActionConnectorName() {
        return this._actionConnectorName;
    }

    getConnector() {
        return this._connector;
    }

    setConnector(connector, options) {
        this.setAttribute('_connector', connector, options);
    }

    /**
     * initialize ActionInvocationExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        this.setActionName(jsonNode.action_name, { doSilently: true });
        this.setActionPackageName(jsonNode.action_pkg_name, { doSilently: true });
        this.setActionConnectorName(jsonNode.action_connector_name, { doSilently: true });
        const connector = this.getInvocationConnector(this.getActionConnectorName());
        this.setConnector(connector, { doSilently: true });

        _.each(jsonNode.children, (argNode) => {
            const arg = this.getFactory().createFromJson(argNode);
            arg.initFromJson(argNode);
            this.addArgument(arg);
        });
    }

    addArgument(argument) {
        argument.setParent(this);
        this._arguments.push(argument);
    }

    getArguments() {
        return this._arguments;
    }

    getInvocationConnector(connectorVariable) {
        let parent = this.getParent();
        const factory = this.getFactory();

        if (_.isNil(parent.getParent())) {
            return undefined;
        }
        // Iteratively we find the most atomic parent node which can hold a connector
        // ATM those are [FunctionDefinition, ResourceDefinition, ConnectorAction]
        while (!factory.isBallerinaAstRoot(parent)) {
            if (factory.isResourceDefinition(parent) || factory.isFunctionDefinition(parent)
                || factory.isConnectorAction(parent)) {
                break;
            }
            parent = parent.getParent();
        }

        return parent.getConnectorByName(connectorVariable);
    }

    /**
     * Get the expression string
     * @returns {string} expression string
     * @override
     */
    getExpressionString() {
        let argsString = '';
        const args = this.getArguments();

        for (let itr = 0; itr < args.length; itr++) {
            // TODO: we need to refactor this along with the action invocation argument types as well
            if (this.getFactory().isExpression(args[itr])) {
                argsString += args[itr].getExpressionString();
            } else if (this.getFactory().isResourceParameter(args[itr])) {
                argsString += args[itr].getParameterAsString();
            }

            if (itr !== args.length - 1) {
                argsString += ', ';
            }
        }

        return this.getActionConnectorName() + this.getWSRegion(1) + '.' + this.getWSRegion(2) + this.getActionName()
            + this.getWSRegion(3) + '(' + this.getWSRegion(4) + argsString + ')' + this.getWSRegion(5);
    }

    /**
     * Set the expression model from the given expression string
     * @param {string} expression
     * @param {function} callback
     * @override
     */
    setExpressionFromString(expression, callback) {
        if (!_.isNil(expression)) {
            const fragment = FragmentUtils.createExpressionFragment(expression);
            const parsedJson = FragmentUtils.parseFragment(fragment);

            if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
                && _.isEqual(parsedJson.type, 'action_invocation_statement')) {
                this.initFromJson(parsedJson);

                // Manually firing the tree-modified event here.
                // TODO: need a proper fix to avoid breaking the undo-redo
                this.trigger('tree-modified', {
                    origin: this,
                    type: 'custom',
                    title: 'Modify Invocation Expression',
                    context: this,
                });

                if (_.isFunction(callback)) {
                    callback({ isValid: true });
                }
            } else if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }

    messageDrawTargetAllowed(target) {
        // TODO this needs to be refactord.
        return (this.getFactory().isConnectorDeclaration(target) || (this.getFactory().isAssignmentStatement(target) &&
            this.getFactory().isConnectorInitExpression(target.getChildren()[1]))) &&
            (
                // check if target parent is a service.
                this.getFactory().isServiceDefinition(target.getParent()) ||
                // if the target has a top level parent we will check if the id's are equal.
                this.getTopLevelParent().getID() === target.getTopLevelParent().getID()
            );
    }
}

export default ActionInvocationExpression;
