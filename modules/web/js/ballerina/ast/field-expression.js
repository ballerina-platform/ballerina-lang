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
define(['lodash', './expression'], function (_, Expression) {

    /**
     * Constructor for FieldExpression
     * @param {Object} args - Arguments to create the FieldExpression
     */
    var FieldExpression = function (args) {
        this._fieldName = _.get(args, 'fieldName');
        Expression.call(this, 'FieldExpression');
    };

    FieldExpression.prototype = Object.create(Expression.prototype);
    FieldExpression.prototype.constructor = FieldExpression;

    /**
     * Setter for Field Reference
     * @param fieldReference
     */
    FieldExpression.prototype.setFieldName = function (fieldName, options) {
        this.setAttribute('_fieldName', fieldName, options);
    };

    /**
     * Getter for Field name
     * @returns fieldName
     */
    FieldExpression.prototype.getFieldName = function () {
        return this._fieldName;
    };


    FieldExpression.prototype.generateExpression = function () {
        this._expression = this.getFieldName()
    };

    return FieldExpression;
});
