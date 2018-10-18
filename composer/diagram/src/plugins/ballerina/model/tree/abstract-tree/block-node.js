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
import StatementNode from '../statement-node';

class AbstractBlockNode extends StatementNode {


    setStatements(newValue, silent, title) {
        const oldValue = this.statements;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.statements = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'statements',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStatements() {
        return this.statements;
    }


    addStatements(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.statements.push(node);
            index = this.statements.length;
        } else {
            this.statements.splice(i, 0, node);
        }
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Add ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeStatements(node, silent) {
        const index = this.getIndexOfStatements(node);
        this.removeStatementsByIndex(index, silent);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${node.kind}`,
                data: {
                    node,
                    index,
                },
            });
        }
    }

    removeStatementsByIndex(index, silent) {
        this.statements.splice(index, 1);
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-removed',
                title: `Removed ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceStatements(oldChild, newChild, silent) {
        const index = this.getIndexOfStatements(oldChild);
        this.statements[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    replaceStatementsByIndex(index, newChild, silent) {
        this.statements[index] = newChild;
        newChild.parent = this;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'child-added',
                title: `Change ${this.kind}`,
                data: {
                    node: this,
                    index,
                },
            });
        }
    }

    getIndexOfStatements(child) {
        return _.findIndex(this.statements, ['id', child.id]);
    }

    filterStatements(predicateFunction) {
        return _.filter(this.statements, predicateFunction);
    }


}

export default AbstractBlockNode;
