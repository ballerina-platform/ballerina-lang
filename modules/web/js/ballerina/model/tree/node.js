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
import EventChannel from 'event_channel';
import SimpleBBox from '../view/simple-bounding-box';

/**
 * Base of all tree nodes.
 *
 * @class Node
 */
class Node extends EventChannel {

    constructor() {
        super();
        // Set an id @TODO move to a path base ID.
        this.id = uuid();

        // Following will propergate tree modified event to the top.
        this.on('tree-modified', function (event) {
            if (!_.isNil(this.parent)) {
                this.parent.trigger('tree-modified', event);
            }
        });

        /**
         * View State Object to keep track of the model's view properties
         * @type {{bBox: SimpleBBox, components: {}, dimensionsSynced: boolean}}
         */
        this.viewState = {
            bBox: new SimpleBBox(),
            components: {},
            dimensionsSynced: false,
            hidden: false,
        };
    }

    /**
     *
     * @param {NodeVisitor} visitor
     */
    accept(visitor) {
        visitor.beginVisit(this);
        // eslint-disable-next-line guard-for-in
        for (const childName in this) {
            if (childName !== 'parent' && childName !== 'position' && childName !== 'ws') {
                const child = this[childName];
                if (child instanceof Node && child.kind) {
                    child.accept(visitor);
                } else if (child instanceof Array) {
                    for (let i = 0; i < child.length; i++) {
                        const childItem = child[i];
                        if (childItem instanceof Node && childItem.kind) {
                            childItem.accept(visitor);
                        }
                    }
                }
            }
        }
        visitor.endVisit(this);
    }

    /**
     * Get the source of the current Node.
     * @return {string} source.
     */
    getSource() {
        return Node.getSourceOf(this);
    }

    /**
     * Joins sources of a array of nodes with given delimiters.
     *
     * @private
     * @param {Node[]} arr Nodes to be joined.
     * @param {function(): string} separatorFunc White space generator function.
     * @param {string} separator
     * @param {boolean} suffixLast
     * @return {string}
     */
    static join(arr, separatorFunc, separator, suffixLast = false) {
        let str = '';
        for (let i = 0; i < arr.length; i++) {
            const node = arr[i];
            str += Node.getSourceOf(node);
            if (node.kind === 'Identifier') {
                str += separatorFunc();
            }
            if (separator && (suffixLast || i !== arr.length - 1)) {
                str += separator + separatorFunc();
            }
        }

        return str;
    }

    /**
     *
     * @private
     * @param {Node} node
     * @return {string}
     */
    static getSourceOf(node) {
        if (!node) {
            return '';
        }
        let i = 0;
        const ws = node.ws ? node.ws.map(wsObj => wsObj.ws) : [];

        function w() {
            const wsI = ws[i++];
            return wsI === undefined ? '' : wsI;
        }

        switch (node.kind) {

            // auto gen code - start
            case 'Variable':
                if (node.const && node.typeNode && node.name.value &&
                    node.initialExpression) {
                    return 'const' + w() + node.typeNode.getSource() +
                        node.name.value + w() + '=' + w() + node.initialExpression.getSource() +
                        ';' + w();
                }
                if (node.typeNode && node.name.value && node.initialExpression) {
                    return node.typeNode.getSource() + node.name.value +
                        w() + '=' + w() + node.initialExpression.getSource();
                }
                if (node.typeNode && node.name.value) {
                    return node.typeNode.getSource() + node.name.value +
                        w();
                } else {
                    return node.typeNode.getSource();
                }
            case 'ValueType':
                return node.typeKind + w();
            case 'Literal':
                return node.value + w();
            case 'Function':
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.returnParameters && node.returnParameters.length &&
                    node.body) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
                if (node.name.value && node.returnParameters &&
                    node.returnParameters.length && node.body) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.returnParameters && node.returnParameters.length) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '(' + w() + Node.join(node.returnParameters, w, ',') +
                        ')' + w() + '{' + w() + '}' + w();
                }
                if (node.name.value && node.parameters && node.parameters.length &&
                    node.body) {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + Node.join(node.parameters, w, ',') +
                        ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                } else {
                    return 'function' + w() + node.name.value + w() + '(' +
                        w() + ')' + w() + '{' + w() + node.body.getSource() +
                        '}' + w();
                }
            case 'Block':
                if (node.statements && node.statements.length) {
                    return Node.join(node.statements, w, '');
                } else {
                    return '';
                }
            case 'Assignment':
                return Node.join(node.variables, w, '') + '=' + w() +
                    node.expression.getSource() + ';' + w();
            case 'BinaryExpr':
                return node.leftExpression.getSource() + node.operatorKind +
                    w() + node.rightExpression.getSource();
            case 'SimpleVariableRef':
                return node.variableName.value + w();
            case 'VariableDef':
                return node.variable.getSource() + ';' + w();
            case 'Return':
                if (node.expressions && node.expressions.length) {
                    return 'return' + w() + Node.join(node.expressions, w, ';', true);
                } else {
                    return 'return' + w() + ';' + w();
                }
            case 'UserDefinedType':
                return node.typeName.value + w();
            case 'RecordLiteralExpr':
                return '{' + w() + '}' + w();
            case 'BuiltInRefType':
                return node.typeKind + w();
            case 'TypeCastExpr':
                return '(' + w() + node.typeNode.getSource() + ')' +
                    w() + node.expression.getSource();
            case 'ArrayLiteralExpr':
                return '[' + w() + Node.join(node.expressions, w, ',') +
                    ']' + w();
            case 'ArrayType':
                return node.elementType.getSource() + '[' + w() + ']' +
                    w();
            case 'Struct':
                return 'struct' + w() + node.name.value + w() + '{' +
                    w() + Node.join(node.fields, w, ';', true) +
                    '}' + w();
            case 'FieldBasedAccessExpr':
                return node.expression.getSource() + '.' + w() + node.fieldName.value +
                    w();
            case 'IndexBasedAccessExpr':
                return node.expression.getSource() + '[' + w() + node.index.getSource() +
                    ']' + w();
            case 'While':
                return 'while' + w() + '(' + w() + node.condition.getSource() +
                    ')' + w() + '{' + w() + node.body.getSource() +
                    '}' + w();
            case 'If':
                return 'if' + w() + '(' + w() + node.condition.getSource() +
                    ')' + w() + '{' + w() + node.body.getSource() +
                    '}' + w();
            case 'ExpressionStatement':
                return node.expression.getSource() + ';' + w();
            case 'Invocation':
                if (node.packageAlias) {
                    return node.packageAlias.value + w() + ':' + w() +
                        node.name.value + w() + '(' + w() + Node.join(node.argumentExpressions, w, '') +
                        ')' + w();
                } else {
                    return node.name.value + w() + '(' + w() + Node.join(node.argumentExpressions, w, '') +
                        ')' + w();
                }
            case 'UnaryExpr':
                return node.operatorKind + w() + node.expression.getSource();
            case 'Connector':
                return 'connector' + w() + node.name.value + w() +
                    '(' + w() + ')' + w() + Node.join(node.actions, w, '');
            case 'Action':
                return node.name.value + w() + '(' + w() + Node.join(node.parameters, w, ',') +
                    ')' + w() + '(' + w() + Node.join(node.returnParameters, w, '') +
                    ')' + w() + '{' + w() + node.body.getSource() +
                    '}' + w();
            // auto gen code - end

            case 'Identifier':
                return node.value;
            case 'CompilationUnit':
                return node.topLevelNodes.map(Node.getSourceOf).join('');
            case 'PackageDeclaration':
                return 'package' + w() + Node.join(node.packageName, w, '.') + ';' + w();
            case 'Import':
                return 'import' + w() + Node.join(node.packageName, w, '.') + ';' + w();
            default:
                return '';
        }
    }

    [Symbol.iterator]() {
        const children = [];
        for (const key of Object.keys(this)) {
            const prop = this[key];
            if (prop instanceof Node && key !== 'parent') {
                children.push([key, prop]);
            } else if (_.isArray(prop) && prop.length > 0 && prop[0] instanceof Node) {
                let i = 0;
                for (const propI of prop) {
                    children.push([key + '[' + i++ + ']', propI]);
                }
            }
        }
        let nextIndex = 0;

        return {
            next() {
                return nextIndex < children.length ?
                    { value: children[nextIndex++], done: false } :
                    { done: true };
            },
        };
    }

    entries() {
        const props = [];
        for (const key of _.pull(Object.keys(this), 'ws', 'viewState', 'id', '_events', 'parent', 'position', 'kind')) {
            const prop = this[key];
            if (_.isArray(prop)) {
                if (prop.length > 0 && !(prop[0] instanceof Node)) {
                    props.push([key, prop]);
                }
            } else if (!(prop instanceof Node)) {
                props.push([key, prop]);
            }
        }

        return props;
    }


    getID() {
        return this.id;
    }

    /**
     * Get the current file
     * @returns {object} - current file
     */
    getFile() {
        return this.file;
    }

    /**
     * Set the current file
     * @param {object} file - current file
     */
    setFile(file) {
        this.file = file;
    }

    /**
     * Checks if an string is valid as an identifier.
     * @param {string} identifier - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidIdentifier(identifier) {
        if (_.isUndefined(identifier)) {
            return false;
        } else if (/^[a-zA-Z_$][a-zA-Z0-9_]*$/.test(identifier)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a string is valid as a type.
     * @param {string} type - The string value.
     * @return {boolean} - True if valid, else false.
     */
    static isValidType(type) {
        if (_.isUndefined(type)) {
            return false;
        } else if (/^[a-zA-Z0-9:_]*\s*[[*\s\]<*\s*>a-zA-Z0-9_]*$/.test(type)) {
            return true;
        }
        return false;
    }
}


const uuid = function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
};

export default Node;
