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
import Node from '../node';

class AbstractSelectClauseNode extends Node {


    setSelectExpressions(newValue, silent, title) {
        const oldValue = this.selectExpressions;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.selectExpressions = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'selectExpressions',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getSelectExpressions() {
        return this.selectExpressions;
    }


    addSelectExpressions(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.selectExpressions.push(node);
            index = this.selectExpressions.length;
        } else {
            this.selectExpressions.splice(i, 0, node);
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

    removeSelectExpressions(node, silent) {
        const index = this.getIndexOfSelectExpressions(node);
        this.removeSelectExpressionsByIndex(index, silent);
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

    removeSelectExpressionsByIndex(index, silent) {
        this.selectExpressions.splice(index, 1);
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

    replaceSelectExpressions(oldChild, newChild, silent) {
        const index = this.getIndexOfSelectExpressions(oldChild);
        this.selectExpressions[index] = newChild;
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

    replaceSelectExpressionsByIndex(index, newChild, silent) {
        this.selectExpressions[index] = newChild;
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

    getIndexOfSelectExpressions(child) {
        return _.findIndex(this.selectExpressions, ['id', child.id]);
    }

    filterSelectExpressions(predicateFunction) {
        return _.filter(this.selectExpressions, predicateFunction);
    }


    setGroupBy(newValue, silent, title) {
        const oldValue = this.groupBy;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.groupBy = newValue;

        this.groupBy.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'groupBy',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getGroupBy() {
        return this.groupBy;
    }



    setHaving(newValue, silent, title) {
        const oldValue = this.having;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.having = newValue;

        this.having.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'having',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getHaving() {
        return this.having;
    }




    isSelectAll() {
        return this.selectAll;
    }

    setSelectAll(newValue, silent, title) {
        const oldValue = this.selectAll;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.selectAll = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'selectAll',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractSelectClauseNode;
