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
import ImageUtil from './image-util';

class PanelDecorator extends React.Component {

    render() {
        const bBox = this.props.bBox;
        const titleHeight = 25;
        const iconSize = 14;
        const collapsed = this.props.collapsed || false;
        return ( <g className="panel">
                     <g className="panel-header">
                         <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleHeight } rx="0" ry="0" className="headingRect" data-original-title="" title=""></rect>
                         <text x={ bBox.x + titleHeight } y={ bBox.y + titleHeight / 2 + 5 }>{this.props.title}</text>
                         <image x={bBox.x + 5} y={bBox.y + 5} width={iconSize} height={iconSize} xlinkHref={ImageUtil.getSVGIconString(this.props.icon)}/>
                         <g className="panel-header-controls">
                             <image x={ bBox.x + bBox.w - 44.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } className="control"
                                  xlinkHref={ImageUtil.getSVGIconString('delete')}/>
                             <line x1={ bBox.x + bBox.w - 50} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 50} y2={ bBox.y + 20} className="operations-separator"></line>
                             <image x={ bBox.x + bBox.w - 19.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize }  className="control"
                                  xlinkHref={(collapsed) ? ImageUtil.getSVGIconString('down') : ImageUtil.getSVGIconString('up')}/>
                             <line x1={ bBox.x + bBox.w - 25} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 25} y2={ bBox.y + 20} className="operations-separator"></line>
                         </g>
                     </g>
                     <g className="panel-body">
                        { !collapsed &&
                             <rect x={ bBox.x } y={ bBox.y + titleHeight} width={ bBox.w } height={ bBox.h - titleHeight } rx="0" ry="0" className="resource-content" fill="#fff"></rect>
                        }
                        { !collapsed && this.props.children}
                     </g>
                 </g>);
    }
}

export default PanelDecorator;
