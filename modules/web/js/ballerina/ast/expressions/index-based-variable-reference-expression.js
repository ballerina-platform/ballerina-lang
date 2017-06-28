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
import Expression from './expression';
import FragmentUtils from './../../utils/fragment-utils';

/**
 * Constructor for IndexBasedVariableReferenceExpression
 * @param {Object} args - Arguments to create the IndexBasedVariableReferenceExpression
 * @constructor
 * @augments Expression
 */
class IndexBasedVariableReferenceExpression extends Expression {
    constructor(args) {
        super('IndexBasedVariableReferenceExpression');
    }

    /**
     * Init from json node
     * There are two children expressions expected
     * eg. a["b"] or a[b]  or a.aa["b"]
     *      1st child will be a var ref expr representing 'a' or 'a.aa' (including package if any)
     *      2nd child will be either a basic literal (of type string) or a simple var ref expr
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.getChildren().length = 0
        if (!_.isNil(jsonNode.children) && !_.isEmpty(jsonNode.children)) {
            jsonNode.children.forEach((childNode) => {
                const child = this.getFactory().createFromJson(childNode);
                this.addChild(child, undefined, true, true);
                child.initFromJson(childNode);
            });
        }
    }

    /**
     * @see initFromJson docs
     */
    getVarRefExpr() {
        return this.children[0];
    }

     /**
     * @see initFromJson docs
     */
    getIndexExpr() {
        return this.children[1];
    }

    getExpressionString() {
        const varRefString = this.getVarRefExpr().getExpressionString(),
              indexExprString =  this.getIndexExpr().getExpressionString();
        return `${varRefString}[${indexExprString}]`;
    }

    /**
    * get the string from expression editor
    * call fragment parser and get parse tree of the node
    * validate and create children from scratch
    **/
    setExpressionFromString(exprString, callback) {
        if (!_.isNil(exprString)) {
            const fragment = FragmentUtils.createExpressionFragment(exprString);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if ((!_.has(parsedJson, 'error')
                   || !_.has(parsedJson, 'syntax_errors'))
                   && _.isEqual(parsedJson.type, 'index_based_variable_reference_expression')) {
                this.initFromJson(parsedJson);
                if (_.isFunction(callback)) {
                    callback({ isValid: true });
                }
            } else if (_.isFunction(callback)) {
                callback({ isValid: false, response: parsedJson });
            }
        }
    }
}

export default IndexBasedVariableReferenceExpression;

