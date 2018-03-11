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
import ExpressionNode from '../expression-node';

class AbstractXmlQnameNode extends ExpressionNode {


    setPrefix(newValue, silent, title) {
        const oldValue = this.prefix;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.prefix = newValue;

        this.prefix.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'prefix',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPrefix() {
        return this.prefix;
    }


    setLocalname(newValue, silent, title) {
        const oldValue = this.localname;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.localname = newValue;

        this.localname.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'localname',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getLocalname() {
        return this.localname;
    }


    setNamespaceUri(newValue, silent, title) {
        const oldValue = this.namespaceUri;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.namespaceUri = newValue;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'namespaceUri',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getNamespaceUri() {
        return this.namespaceUri;
    }


}

export default AbstractXmlQnameNode;
