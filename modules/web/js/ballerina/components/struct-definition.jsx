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
import LifeLine from './lifeline.jsx';
import StatementContainer from './statement-container';
import PanelDecorator from './panel-decorator';
import {getComponentForNodeArray} from './utils';
import {lifeLine} from './../configs/designer-defaults';
import './struct-definition.css';
import PropTypes from 'prop-types';
// import {renderTextBox} from './text-input';

class StructDefinition extends React.Component {

    constructor(props) {
        super(props);
    }

    deleteStatement(node) {
        node.remove();
    }

    handleStatementClick(node, bBox) {
        console.log(bBox);
        // renderTextBox(bBox, () => { console.log('adadadad')}, "");
    }

    render() {
        const { model } = this.props;
				const { bBox, components: {statements, contentOperations} } = model.getViewState();
        const children = model.getChildren();
        const title = model.getStructName();
				return (
					<PanelDecorator icon="tool-icons/struct" title={title} bBox={bBox} model={model}>
              <g>
                  <rect
                      x={contentOperations.x}
                      y={contentOperations.y}
                      width={contentOperations.w}
                      height={contentOperations.h}
                   />
              </g>
              <g>
                {
                    children.map( (child, i) => {
                        const type = child.getBType();
                        const identifier = child.getIdentifier();
                        const value = child.getValue();
                        const statementDimensions = statements[i];
                        return (<g key={i} className="struct-definition-statement">

                          <g className="struct-variable-definition-type">
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
                                  onClick={ ()=> this.handleStatementClick(child, statementDimensions.identifierText) }>
                                  {type}
                              </text>
                          </g>

                          <g className="struct-variable-definition-identifier">
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
                                  onClick={ ()=> this.handleStatementClick(child, statementDimensions) }>
                                  {identifier}
                              </text>
                          </g>

                          <g className="struct-variable-definition-value">
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
                                  onClick={ ()=> this.handleStatementClick(child, statementDimensions) }>
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

export default StructDefinition;
