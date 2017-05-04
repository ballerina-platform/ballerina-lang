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
 
import React from 'react'

class LifeLine extends React.Component {
//{x: 330.8, w: 120, h:320, y:260}
    render() {
        const bBox = this.props.bBox;
        const centerX = bBox.x + (bBox.w / 2);
        const y2 = bBox.h + bBox.y;
        const titleBoxH = 30;
        return (<g className="default-worker-life-line">
                    <line x1={ centerX } y1={ bBox.y + titleBoxH / 2} x2={ centerX } y2={ y2 - titleBoxH / 2 }></line>
                    <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleBoxH } rx="0" ry="0" className="connector-life-line-top-polygon"></rect>
                    <rect x={ bBox.x } y={ y2 - titleBoxH } width={ bBox.w } height={ titleBoxH } rx="0" ry="0" className="connector-life-line-bottom-polygon"></rect>
                    <text x={ centerX } y={ bBox.y + titleBoxH / 2 } textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">{ this.props.title }</text>
                    <text x={ centerX } y={ y2 - titleBoxH / 2 } textAnchor="middle" alignmentBaseline="central" dominantBaseline="central" className="life-line-title genericT">{ this.props.title }</text>
                </g>);
    }
}

export default LifeLine;
