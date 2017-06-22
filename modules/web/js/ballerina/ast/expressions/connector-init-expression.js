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
import _ from 'lodash';
import Expression from './expression';
import FragmentUtils from './../../utils/fragment-utils';

/**
 * Constructor for ConnectorInitExpression
 */
class ConnectorInitExpression extends Expression {

    /**
     * Constructor for Connector init expression
     * @param {Object} args - Arguments to create the ConnectorInitExpression
     * @constructor
     */
    constructor(args) {
        super('ConnectorInitExpression');
        this._arguments = _.get(args, 'arguments', []);
        this._connectorName = _.get(args, 'connectorName');
    }

    /**
     * set connector name
     * @param {string} connectorName - connector name
     * @param {object} opts - set attribute options
     */
    setConnectorName(connectorName, opts) {
        this.setAttribute('_connectorName', connectorName, opts);
    }

    /**
     * get connector name
     * @return {string} connector name
     */
    getConnectorName() {
        return this._connectorName;
    }

    /**
     * set args
     * @param {Expression[]} args - arguments
     * @param {object} opts - set attribute options
     * @returns {void}
     */
    setArgs(args, opts) {
        this.setAttribute('_arguments', args, opts);
    }

    /**
     * get args
     * @return {Expression[]} arguments
     */
    getArgs() {
        return this._arguments;
    }

    /**
     * initialize ConnectorInitExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0;
        const self = this;
        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
        const argExprs = [];
        _.each(jsonNode.arguments, (argNode) => {
            const expr = self.getFactory().createFromJson(argNode);
            argExprs.push(expr);
            expr.initFromJson(argNode);
        });
        let connectorName;
        if (!_.isNil(jsonNode.connector_name)) {
            connectorName = self.getFactory().createFromJson(jsonNode.connector_name);
            connectorName.initFromJson(jsonNode.connector_name);
        }
        this.setConnectorName(connectorName, { doSilently: true });
        this.setArgs(argExprs, { doSilently: true });
    }

    /**
     * Set expression from the expression string
     * @param {string} expressionString - expression string from which the expression is re initialized
     * @param {function} callback - callback function
     * @override
     */
    setExpressionFromString(expressionString, callback) {
        const fragment = FragmentUtils.createExpressionFragment(expressionString);
        const parsedJson = FragmentUtils.parseFragment(fragment);

        if ((!_.has(parsedJson, 'error') || !_.has(parsedJson, 'syntax_errors'))
            && _.isEqual(parsedJson.type, 'connector_init_expr')) {
            this.initFromJson(parsedJson);

            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            this.trigger('tree-modified', {
                origin: this,
                type: 'custom',
                title: 'Modify ConnectorInit Expression',
                context: this,
            });

            if (_.isFunction(callback)) {
                callback({ isValid: true });
            }
        } else if (_.isFunction(callback)) {
            callback({ isValid: false, response: parsedJson });
        }
    }

    /**
     * Get the expression string
     * @return {string} - expression string
     */
    getExpressionString() {
        let expr = 'create' + this.getWSRegion(1)
            + this.getConnectorName().toString()
            + this.getWSRegion(2) + '(';
        this.getArgs().forEach((arg, index) => {
            expr += (index !== 0 && arg.whiteSpace.useDefault) ? ' ' : '';
            expr += arg.getExpressionString();
            if (index < this.getArgs().length - 1) {
                expr += ',';
            }
        });
        return expr + ')';
    }
}

export default ConnectorInitExpression;
