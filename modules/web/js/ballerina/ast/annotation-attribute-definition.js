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
        this._attributeValue = _.get(args, 'attributeValue', '');
        this._pkgPath = _.get(args, 'pkgPath');
        this.whiteSpace.defaultDescriptor.regions =  {
            0: '',
            1: ' ',
            2: ' ',
            3: ' ',
            4: '',
            5: '\n'
        }
    }

    setAttributeName(attributeName, options) {
        if (!_.isNil(attributeName) && ASTNode.isValidIdentifier(attributeName)) {
            this.setAttribute('_attributeName', attributeName, options);
        } else {
            let error = 'Invalid name for the annotation attribute name: ' + attributeName;
            log.error(error);
            throw  error;
        }
    }

    getAttributeName() {
        return this._attributeName;
    }

    setAttributeType(attributeType, options) {
        this.setAttribute('_attributeType', attributeType, options);
    }

    getAttributeType() {
        return this._attributeType;
    }

    setPackagePath(pkgPath, options) {
        this.setAttribute('_pkgPath', pkgPath, options);
    }

    getPackagePath() {
        return this._pkgPath;
    }

    setAttributeValue(value, options) {
        this.setAttribute('_attributeValue', value, options);
    }

    getAttributeValue() {
        return this._attributeValue;
    }

    getAttributeStatementString() {
        let statement = this.getAttributeType() + this.getWSRegion(1)
            + this.getAttributeName();
        if (!_.isEmpty(this.getAttributeValue())) {
            statement += this.getWSRegion(2) + '=' + this.getWSRegion(3)
                + this.getAttributeValue()
                + this.getWSRegion(4);
        } else if (!this.whiteSpace.useDefault) {
            // ignore a default space between indenifier & semicolon
            statement += this.getWSRegion(2);
        }
        statement += ';' + this.getWSRegion(5);
        return statement;
    }

    /**
     * @inheritDoc
     * @override
     * */
    initFromJson(jsonNode) {
        let self = this;
        this.setAttributeName(jsonNode.annotation_attribute_name, {doSilently: true});
        this.setAttributeType(jsonNode.annotation_attribute_type, {doSilently: true});
        _.each(jsonNode.children, function (childNode) {
            let child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
        if (this.getChildren().length > 0) {
            if (this.getFactory().isExpression(this.getChildren()[0])) {
                this.setAttributeValue(this.getChildren()[0].getExpressionString(), {doSilently: true});
            }
        }
    }
}

export default AnnotationAttributeDefinition;
