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
import {renderTextBox} from './text-input';

class StructDefinition extends React.Component {

    constructor(props) {
        super(props);
    }

    deleteStatement(node) {
        node.remove();
    }

    handleStatementClick(node, bBox) {
        console.log(bBox);
        renderTextBox(bBox, () => { console.log('adadadad')}, "adadad");
    }

    render() {
        const { model } = this.props;
				const { bBox, components: {statements} } = model.getViewState();
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

                        return (<g key={i} className="struct-definition-statement">

                          <g className="struct-variable-definition-type">
                              <rect x={statements[i].x } y={statements[i].y} width={statements[i].w/3} height={statements[i].h}
                                  className="struct-variable-definition-type-rect"
                               />
                              <text x={statements[i].x } y={statements[i].y + statements[i].h / 2} className="struct-variable-definition-type-text" onClick={ ()=> this.handleStatementClick(child, statements[i]) }>
                                  {type}
                              </text>
                          </g>

                          <g className="struct-variable-definition-identifier">
                              <rect x={statements[i].x + statements[i].w/3} y={statements[i].y} width={statements[i].w/3} height={statements[i].h}
                                  className="struct-variable-definition-identifier-rect"
                              />
                              <text x={statements[i].x + statements[i].w/3} y={statements[i].y + statements[i].h / 2} className="struct-variable-definition-identifier-text" onClick={ ()=> this.handleStatementClick(child, statements[i]) }>
                                  {identifier}
                              </text>
                          </g>

                          <g className="struct-variable-definition-value">
                              <rect x={statements[i].x + (2 * statements[i].w/3)} y={statements[i].y} width={statements[i].w/3} height={statements[i].h}
                                  className="struct-variable-definition-value-rect"
                              />
                              <text x={statements[i].x + (2 * statements[i].w/3)} y={statements[i].y + statements[i].h / 2} className="struct-variable-definition-value-text" onClick={ ()=> this.handleStatementClick(child, statements[i]) }>
                                  {value}
                              </text>

                          </g>
                          <text x={statements[i].x + statements[i].w - 20} y={statements[i].y + statements[i].h / 2} onClick={ ()=> this.deleteStatement(child) } className="struct-statement-delete">×</text>
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
