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

class AbstractUserDefinedTypeNode extends Node {


    setFlags(newValue, silent, title) {
        const oldValue = this.flags;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.flags = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'flags',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getFlags() {
        return this.flags;
    }



    setPackageAlias(newValue, silent, title) {
        const oldValue = this.packageAlias;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.packageAlias = newValue;

        this.packageAlias.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'packageAlias',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPackageAlias() {
        return this.packageAlias;
    }



    setTypeName(newValue, silent, title) {
        const oldValue = this.typeName;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.typeName = newValue;

        this.typeName.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'typeName',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTypeName() {
        return this.typeName;
    }




    isNullable() {
        return this.nullable;
    }

    setNullable(newValue, silent, title) {
        const oldValue = this.nullable;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.nullable = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'nullable',
                    newValue,
                    oldValue,
                },
            });
        }
    }


    isGrouped() {
        return this.grouped;
    }

    setGrouped(newValue, silent, title) {
        const oldValue = this.grouped;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.grouped = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'grouped',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractUserDefinedTypeNode;
