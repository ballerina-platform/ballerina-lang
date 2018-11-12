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

class AbstractPackageDeclarationNode extends Node {


    setPackageVersion(newValue, silent, title) {
        const oldValue = this.packageVersion;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageVersion = newValue;

        this.packageVersion.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageVersion',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPackageVersion() {
        return this.packageVersion;
    }



    setPackageName(newValue, silent, title) {
        const oldValue = this.packageName;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageName = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageName',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPackageName() {
        return this.packageName;
    }


    addPackageName(node, i = -1, silent) {
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.packageName.push(node);
            index = this.packageName.length;
        } else {
            this.packageName.splice(i, 0, node);
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

    removePackageName(node, silent) {
        const index = this.getIndexOfPackageName(node);
        this.removePackageNameByIndex(index, silent);
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

    removePackageNameByIndex(index, silent) {
        this.packageName.splice(index, 1);
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

    replacePackageName(oldChild, newChild, silent) {
        const index = this.getIndexOfPackageName(oldChild);
        this.packageName[index] = newChild;
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

    replacePackageNameByIndex(index, newChild, silent) {
        this.packageName[index] = newChild;
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

    getIndexOfPackageName(child) {
        return _.findIndex(this.packageName, ['id', child.id]);
    }

    filterPackageName(predicateFunction) {
        return _.filter(this.packageName, predicateFunction);
    }


}

export default AbstractPackageDeclarationNode;
