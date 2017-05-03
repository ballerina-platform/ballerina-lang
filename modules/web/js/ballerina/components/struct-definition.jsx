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

class StructDefinition extends React.Component {

    constructor(props) {
        super(props);
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
                        return (<g key={i}>
                          <rect x={statements[i].x } y={statements[i].y} width={statements[i].w/3} height={statements[i].h} stroke="black" fill="white"/>
                          <text x={statements[i].x } y={statements[i].y + statements[i].h / 2}>
                            {child.children[0].children[0].getTypeName()}
                          </text>

                          <rect x={statements[i].x + statements[i].w/3} y={statements[i].y} width={statements[i].w/3} height={statements[i].h} stroke="black" fill="white"/>
                          <text x={statements[i].x + statements[i].w/3} y={statements[i].y + statements[i].h / 2}>
                              {child.children[0].children[0].getName()}
                          </text>

                          <rect x={statements[i].x + (2 * statements[i].w/3)} y={statements[i].y} width={statements[i].w/3} height={statements[i].h} stroke="black" fill="white"/>
                          <text x={statements[i].x + (2 * statements[i].w/3)} y={statements[i].y + statements[i].h / 2}>
                              {child.children[1]._basicLiteralValue}
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
