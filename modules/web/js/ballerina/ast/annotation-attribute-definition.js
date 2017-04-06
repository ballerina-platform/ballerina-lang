/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ASTNode from './node';
import log from 'log';

/**
 * Annotation Attribute Definition for defining an attribute definition
 * */
class AnnotationAttributeDefinition extends ASTNode {
    constructor(args) {
        super('AnnotationAttribute');
        this._attributeName = _.get(args, 'attributeName');
        this._attributeType = _.get(args, 'attributeType');
        this._pkgPath = _.get(args, 'pkgPath');
    }

    setAttributeName(attributeName, options) {
        if (!_.isNil(attributeName) && ASTNode.isValidIdentifier(attributeName)) {
            this.setAttribute('_attributeName', attributeName, options);
        } else {
            var error = 'Invalid name for the annotation attribute name: ' + attributeName;
            log.error(error);
            throw  error;
        }
    }


    getAttributeName() {
        return this._attributeName;
    }

    getAttributeType(attributeType, options) {
        this.setAttribute('_attributeType', attributeType, options);
    }

    setAttributeType() {
        return this._attributeType;
    }

    setPackagePath(pkgPath, options){
        this.setAttribute('_pkgPath', pkgPath, options);
    }

    getPackagePath(){
        return this._pkgPath;
    }

    /**
     * @inheritDoc
     * @override
     * */
    initFromJson(jsonNode) {
        var self = this;
        this.setAttributeName(jsonNode.annotation_attribute_name, {doSilently: true});
        this.setAttributeType(jsonNode.annotation_attribute_type, {doSilently: true});
        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default AnnotationAttributeDefinition;