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
import Statement from './statement';
import ASTFactory from '../ast-factory';

/**
 * Class to represent an Transform statement.
 */
class TransformStatement extends Statement {
    /**
     * Constructor for TransformStatement
     * @param {object} args - transform statement arguments
     * @override
     */
    constructor(args) {
        super(args);
        this.input = _.get(args, 'input', []);
        this.output = _.get(args, 'output', []);
        this.type = 'TransformStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
            3: '\n',
        };
    }

    /**
     * set input
     * @param {object} input - input to be set
     * @returns {void}
     */
    setInput(input) {
        this.input = input;
    }

    /**
     * Set output
     * @param {object} output - output to be set
     * @returns {void}
     */
    setOutput(output) {
        this.output = output;
    }

    /**
     * Get input
     * @return {Object} input
     */
    getInput() {
        return this.input;
    }

    /**
     * Get output
     * @return {Object} output
     */
    getOutput() {
        return this.output;
    }

    /**
     * initialize Transform Statement from json object
     * @param {Object} jsonNode to initialize from
     * @param {Object} jsonNode.transform_input input variable refs for transform statement
     * @param {Object} jsonNode.transform_output output variable refs for transform statement
     * @returns {void}
     */
    initFromJson(jsonNode) {
        _.each(jsonNode.transform_input, (childNode) => {
            const inputVar = ASTFactory.createFromJson(childNode);
            inputVar.initFromJson(childNode);
            this.input.push(inputVar);
        });

        _.each(jsonNode.transform_output, (childNode) => {
            const outputVar = ASTFactory.createFromJson(childNode);
            outputVar.initFromJson(childNode);
            this.output.push(outputVar);
        });

        _.each(jsonNode.children, (childNode) => {
            const child = ASTFactory.createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Override the removeChild function
     * @param {ASTNode} child - child node
     * @param {boolean} ignoreModifiedTreeEvent - whether to ignore the tree modified event
     * @param {boolean} willVisit - whether visit or not
     * @returns {void}
     */
    removeChild(child, ignoreModifiedTreeEvent, willVisit) {
        if (!_.isUndefined(willVisit) && willVisit !== true) {
            const parentModelChildren = this.children;
            for (let itr = 0; itr < parentModelChildren.length; itr++) {
                if (parentModelChildren[itr].id === child.id) {
                    parentModelChildren.splice(itr, 1);
                    break;
                }
            }
        } else {
            Object.getPrototypeOf(this.constructor.prototype).removeChild.call(this, child, ignoreModifiedTreeEvent);
        }
    }

    /**
     * Get the transform statement string
     * @return {string} transform statement string
     */
    getStatementString() {
        return 'transform';
    }

    /**
     * Remove specified type from transform inputs
     * @param  {object} input input type needs to be removed
     */
    removeInput(input){
      _.remove(this.input, function(inputVal) {
          return inputVal.getVariableName() === input.name;
      });

      _.forEach(_.cloneDeep(this.getChildren()), (child) => {
           if(ASTFactory.isFunctionInvocationExpression(child.getRightExpression())) {
               _.forEach(child.getRightExpression().children, (expChild, index) => {
                 if ((ASTFactory.isFieldBasedVarRefExpression(expChild)
                              && expChild.getVarRoot().getVariableName()  == input.name) ||
                     (ASTFactory.isSimpleVariableReferenceExpression(expChild)
                              && expChild.getVariableName()  == input.name)) {
                      this.getChildById(child.getID()).getRightExpression().children[index]
                                                                    = ASTFactory.createNullLiteralExpression();
                 }
               });
           } else if((ASTFactory.isFieldBasedVarRefExpression (child.getRightExpression())
                          && child.getRightExpression().getVarRoot().getVariableName() == input.name)||
                        (ASTFactory.isSimpleVariableReferenceExpression(child.getRightExpression())
                                                && child.getRightExpression().getVariableName() == input.name)){
             this.removeChild(child, true, true);
           }
      });

      this.trigger('tree-modified', {
          origin: this,
          type: 'input-removed',
          title: `Remove ${input.name}`,
          data: {
          }
      });
    }

    /**
     * Remove specified type from transform outputs
     * @param  {object} output input type needs to be removed
     */
    removeOutput(output) {
      _.remove(this.output, function(outputVal) {
          return outputVal.getVariableName() === output.name;
      });

      _.forEach(_.cloneDeep(this.getChildren()), (child) => {
          if(ASTFactory.isFunctionInvocationExpression(child.getRightExpression())) {
            if (child.getLeftExpression().children[0].getVarRoot().getVariableName()  == output.name) {
                this.getChildById(child.getID()).getLeftExpression()
                          .removeChild(this.getChildById(child.getID()).getLeftExpression().children[0]);
            }
          } else if(child.getLeftExpression().children[0].getFieldName() == output.name){
            this.removeChild(child, true, true);
          }
      });

      this.trigger('tree-modified', {
          origin: this,
          type: 'output-removed',
          title: `Remove ${output.name}`,
          data: {
          }
      });
    }
}

export default TransformStatement;
