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
import React from "react";
import PropTypes from 'prop-types';
import ImageUtil from './image-util';

class ParameterView extends React.Component {

    onDelete() {
        this.props.model.remove();
    }

    render() {
        let model = this.props.model;
        let viewState = model.viewState;
        let lengthOfIcon = 14;
        let xOfTextWithLeftPadding = viewState.x + 5;
        let parameterAsString = this.props.model.getFactory().isArgument(this.props.model) ? model.getArgumentAsString() : model.getParameterAsString() ;
        return (<g><rect x={viewState.x} y={this.props.rectY} width={viewState.textLength + lengthOfIcon} height={this.props.h} rx="0" ry="0" className="parameter-wrapper"></rect>
            <rect x={viewState.x} y={this.props.rectY} width={viewState.textLength - 10} height={this.props.h} rx="0" ry="0" className="parameter-text-wrapper"></rect>
            <text x={xOfTextWithLeftPadding} y={this.props.textY}>{parameterAsString}</text>
            <rect x={viewState.textLength + viewState.x - 10} y={this.props.rectY} width={lengthOfIcon + 10} height={this.props.h} rx="0" ry="0" className="parameter-delete-icon-wrapper"></rect>
            <image x={viewState.textLength + viewState.x - 5} y={this.props.rectY + 5} width={lengthOfIcon} height={lengthOfIcon } className="parameter-delete-icon"
                                    xlinkHref={ImageUtil.getSVGIconString('delete')} onClick={() => this.onDelete()}/>
                </g>)
    }
}

ParameterView.propTypes = {
}


export default ParameterView;