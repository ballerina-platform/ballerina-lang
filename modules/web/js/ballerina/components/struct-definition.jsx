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

import React from 'react';
import _ from 'lodash';
import LifeLine from './lifeline.jsx';
import StatementContainer from './statement-container';
import PanelDecorator from './panel-decorator';
import {getComponentForNodeArray} from './utils';
import {lifeLine} from './../configs/designer-defaults';
import './struct-definition.css';
import PropTypes from 'prop-types';
import StructOperationsRenderer from './struct-operations-renderer';
import Renderer from './renderer';
import Alerts from 'alerts';

class StructDefinition extends React.Component {

    constructor(props) {
        super(props);
    }

    deleteStatement(node) {
        node.remove();
    }

    renderTextBox(textValue, bBox, callback) {
        this.context.renderer.renderTextBox({
          bBox: bBox,
          display: true,
          initialValue: textValue || "",
          onChange(value){
            callback(value);
          }
        });
    }

    handleIdentifierClick(textValue, bBox, model) {
        this.renderTextBox(
            textValue,
            bBox,
            (value) => {
                this.validateIdentifierName(value);
                model.setIdentifier(value);
            }
        );
    }

    handleValueClick(textValue, bBox, model) {
        this.renderTextBox(
            textValue,
            bBox,
            (value) => {
                model.setValue(value);
            }
        );
    }

    handleTypeClick(currentSelection, bBox, model) {
        const { renderingContext } = this.context;
        this.context.renderer.renderDropdown({
          bBox: bBox,
          display: true,
          initialValue: currentSelection || "",
          options: renderingContext.environment.getTypes(),
          onChange(value){
              model.setBType(value);
          }
        });
    }

    renderStructOperations() {
        const { model } = this.props;
        const { components: {contentOperations} } = model.getViewState();
        const { renderingContext } = this.context;

        this.context.structOperationsRenderer.renderOverlay({
            bBox: contentOperations,
            types: renderingContext.environment.getTypes(),
            onSubmit: (bType, identifier, defaultValue) => {
              return this.addVariableDefinitionStatement(bType, identifier, defaultValue);
            }
        });
    }
    validateIdentifierName (identifier) {
        const {model} = this.props;
        if (!identifier || !identifier.length) {
            const errorString = "Identifier cannot be empty";
            Alerts.error(errorString);
            throw errorString;
        }
        const identifierAlreadyExists = _.findIndex(model.getVariableDefinitionStatements(), function (variableDefinitionStatement) {
            return variableDefinitionStatement.getIdentifier() === identifier;
        }) !== -1;
        if (identifierAlreadyExists) {
            const errorString = `A variable with identifier ${identifier} already exists.`;
            Alerts.error(errorString);
            throw errorString;
        }
    }
    addVariableDefinitionStatement(bType, identifier, defaultValue) {
        this.validateIdentifierName(identifier);
        this.props.model.addVariableDefinitionStatement(bType, identifier, defaultValue);
    }


    componentDidUpdate() {
        this.renderStructOperations();
    }

    componentDidMount() {
        this.renderStructOperations();
    }

    render() {

        const { model } = this.props;
				const { bBox, components: {statements, contentOperations} } = model.getViewState();
        const children = model.getChildren();
        const title = model.getStructName();
				return (
					<PanelDecorator icon="tool-icons/struct" title={title} bBox={bBox} model={model}>
              <g>
                {
                    children.map( (child, i) => {
                        const type = child.getBType();
                        const identifier = child.getIdentifier();
                        const value = child.getValue();
                        const statementDimensions = statements[i];
                        return (<g key={i} className="struct-definition-statement">

                          <g
                              className="struct-variable-definition-type"
                              onClick={ ()=> this.handleTypeClick(type, statementDimensions.typeWrapper, child) }
                              >
                              <rect
                                  x={statementDimensions.typeWrapper.x }
                                  y={statementDimensions.typeWrapper.y}
                                  width={statementDimensions.typeWrapper.w}
                                  height={statementDimensions.typeWrapper.h}
                                  className="struct-variable-definition-type-rect"
                               />
                              <text
                                  x={statementDimensions.typeText.x }
                                  y={statementDimensions.typeText.y}
                                  className="struct-variable-definition-type-text"
                                  >
                                  {type}
                              </text>
                          </g>

                          <g
                              className="struct-variable-definition-identifier"
                              onClick={() => this.handleIdentifierClick(identifier, statementDimensions.identifierWrapper, child) }
                              >
                              <rect
                                  x={statementDimensions.identifierWrapper.x }
                                  y={statementDimensions.identifierWrapper.y}
                                  width={statementDimensions.identifierWrapper.w}
                                  height={statementDimensions.identifierWrapper.h}
                                  className="struct-variable-definition-identifier-rect"
                              />
                              <text
                                  x={statementDimensions.identifierText.x}
                                  y={statementDimensions.identifierText.y}
                                  className="struct-variable-definition-identifier-text"
                              >
                                  {identifier}
                              </text>
                          </g>

                          <g
                              className="struct-variable-definition-value"
                              onClick={() => this.handleValueClick(value, statementDimensions.valueWrapper, child) }
                              >
                              <rect
                                  x={statementDimensions.valueWrapper.x }
                                  y={statementDimensions.valueWrapper.y}
                                  width={statementDimensions.valueWrapper.w}
                                  height={statementDimensions.valueWrapper.h}
                                  className="struct-variable-definition-value-rect"
                              />
                              <text
                                  x={statementDimensions.valueText.x}
                                  y={statementDimensions.valueText.y}
                                  className="struct-variable-definition-value-text"
                              >
                                  {value}
                              </text>

                          </g>
                          <text
                              x={statementDimensions.deleteButton.x}
                              y={statementDimensions.deleteButton.y}
                              onClick={ ()=> this.deleteStatement(child) }
                              className="struct-statement-delete">
                              ×
                          </text>
                          </g>
                        )
                    })
                }
              </g>
					</PanelDecorator>
				);
    }
}

StructDefinition.contextTypes = {
    structOperationsRenderer: PropTypes.instanceOf(StructOperationsRenderer).isRequired,
    renderingContext: PropTypes.instanceOf(Object).isRequired,
    renderer: PropTypes.instanceOf(Renderer).isRequired,
};

export default StructDefinition;
