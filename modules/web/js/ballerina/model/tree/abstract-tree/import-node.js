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

import Node from '../node';

class ImportNodeAbstract extends Node {


    setPackageVersion(newValue, title) {
        let oldValue = this.packageVersion;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.packageVersion = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'packageVersion',
                newValue,
                oldValue,
            }
        });
    }

    getPackageVersion() {
        return this.packageVersion;
    }



    setAlias(newValue, title) {
        let oldValue = this.alias;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.alias = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'alias',
                newValue,
                oldValue,
            }
        });
    }

    getAlias() {
        return this.alias;
    }



    setPackageName(newValue, title) {
        let oldValue = this.packageName;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.packageName = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'packageName',
                newValue,
                oldValue,
            }
        });
    }

    getPackageName() {
        return this.packageName;
    }


    addPackageName(node, i = -1){
        node.parent = this;
        let index = i;
        if (i === -1) {
            this.packageName.push(node);
            index = this.packageName.length;
        } else {
            this.packageName.splice(i, 0, node);
        }
        this.trigger('tree-modified', {
            origin: this,
            type: 'child-added',
            title: `Add ${child.kind}`,
            data: {
                child,
                index,
            },
        });
    }


    setWS(newValue, title) {
        let oldValue = this.wS;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.wS = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'wS',
                newValue,
                oldValue,
            }
        });
    }

    getWS() {
        return this.wS;
    }



    setKind(newValue, title) {
        let oldValue = this.kind;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.kind = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'kind',
                newValue,
                oldValue,
            }
        });
    }

    getKind() {
        return this.kind;
    }



    setPosition(newValue, title) {
        let oldValue = this.position;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.position = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'position',
                newValue,
                oldValue,
            }
        });
    }

    getPosition() {
        return this.position;
    }



}

export default ImportNodeAbstract;
