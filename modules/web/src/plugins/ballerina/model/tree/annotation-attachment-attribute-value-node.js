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
import TreeUtil from 'plugins/ballerina/model/tree-util';
import AbstractAnnotationAttachmentAttributeValueNode from './abstract-tree/annotation-attachment-attribute-value-node';

class AnnotationAttachmentAttributeValueNode extends AbstractAnnotationAttachmentAttributeValueNode {

    isValueLiteral() {
        return this.getValue() && TreeUtil.isLiteral(this.getValue());
    }

    isValueVariableRef() {
        return this.getValue() && TreeUtil.isSimpleVariableRef(this.getValue());
    }

    isValueAnnotationAttachment() {
        return this.getValue() && TreeUtil.isAnnotationAttachment(this.getValue());
    }

    isValueArray() {
        return _.isNil(this.getValue());
    }
}

export default AnnotationAttachmentAttributeValueNode;
