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

    getSource() {
        return Node.getSourceOf(this);
    }

    static getSourceOf(node) {
        if (!node) {
            return '';
        }
        let i = 0;
        let s;
        const ws = node.ws ? node.ws.map(wsObj => wsObj.ws) : [];

        function w() {
            const wsI = ws[i++];
            return wsI === undefined ? ' ' : wsI;
        }

        switch (node.kind) {
            case 'Identifier':
                return node.value + w();
            case 'Literal':
                return node.value + w();
            case 'ValueType':
                return node.typeKind + w();
            case 'UserDefinedType':
                return node.typeName.value + w();
            case 'VariableDef':
                return node.variable.getSource() + ';' + w();
            case 'SimpleVariableRef':
                return node.variableName.value + w();
            case 'ExpressionStatement':
                return node.expression.getSource() + ';' + w();
            case 'CompilationUnit':
                return node.topLevelNodes.map(Node.getSourceOf).join('');
            case 'PackageDeclaration':
                return node.packageName.map(Node.getSourceOf).join('');
            case 'ArrayType':
                return node.elementType.getSource() + '[' + w() + ']' + w();
            case 'Block':
                return '{' + w() + node.statements.map(Node.getSourceOf).join('') + '}' + w();
            case 'Assignment':
                return node.variables.map(Node.getSourceOf).join('') + '=' + w() + node.expression.getSource()
                    + ';' + w();
            case 'Return':
                return 'return' + w() + node.expressions.map(Node.getSourceOf).join(',') + ';' + w();
            case 'BinaryExpr':
                return node.leftExpression.getSource() + node.operatorKind + w() + node.rightExpression.getSource();
            case 'IndexBasedAccessExpr' :
                return node.expression.getSource() + node.index.getSource();
            case 'Variable':
                return node.typeNode.getSource() + node.name.value + w() + (node.initialExpression ? '=' +
                        w() + node.initialExpression.getSource() : '');
            case 'Invocation':
                return (node.packageAlias.value ? node.packageAlias.value + w() + ':' : '') + w() +
                    node.name.value + w() + '(' + w() +
                    node.argumentExpressions.map(Node.getSourceOf).join('') + ')' + w();
            case 'Function':
                s = 'function' + w() + node.name.value + w() + '(' + w();
                for (let j = 0; j < node.parameters.length; j++) {
                    s += node.parameters[j].getSource();
                    if (j !== node.parameters.length - 1) {
                        s += ',' + w();
                    }
                }
                s += ')' + w() + '(' + w();
                for (let j = 0; j < node.returnParameters.length; j++) {
                    s += node.returnParameters[j].getSource();
                    if (j !== node.returnParameters.length - 1) {
                        s += ',' + w();
                    }
                }
                s += ')' + w() + node.body.getSource();
                return s;
            default:
                return '';
        }
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
