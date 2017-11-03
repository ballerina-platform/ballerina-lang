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
import PropTypes from 'prop-types';
import PanelDecorator from '../decorators/panel-decorator';
import TransformerNodeModel from '../../../../../model/tree/transformer-node';
import TransformerStatementDecorator from '../transform/transformer-statement-decorator';
import ImageUtil from '../../../../image-util';

class TransformerNode extends React.Component {

    constructor(props) {
        super(props);
        this.onExpand = this.onExpand.bind(this);
    }

    onExpand() {
        const { designView } = this.context;
        designView.setTransformActive(true, this.props.model);
    }

    render() {
        const model = this.props.model;
        const icon = 'tool-icons/transformer';
        const { bBox } = model.viewState;

        const TransFormDetails = ({x, y}) => {
            const text_x = x;
            const text_y = y + (bBox.h / 2);
            const expand_button_x = text_x + model.viewState.titleWidth;
            const expand_button_y = y;

            return (
                <g className='statement-body'>
                    <text x={text_x} y={text_y} className='transform-action'
                        onClick={e => this.onExpand()}>{model.getSignature()}</text>
                    <g className='transform-button' onClick={e => this.onExpand()}>
                        <rect x={expand_button_x}
                            y={expand_button_y}
                            width={30}
                            height={30}
                            className='transform-action-button'/>
                        <image className='transform-action-icon'
                            x={expand_button_x + 8} y={expand_button_y + 8}
                            width={14}
                            height={14}
                            xlinkHref={ImageUtil.getSVGIconString('expand')}>
                            <title>Expand</title>
                        </image>
                    </g>
                </g>
            );
        };

        return (<PanelDecorator bBox={model.viewState.bBox} icon={icon} model={model} headerComponent={TransFormDetails}/>);
    }
}

TransformerNode.propTypes = {
    model: PropTypes.instanceOf(TransformerNodeModel).isRequired,
};

TransformerNode.contextTypes = {
    mode: PropTypes.string,
    designView: PropTypes.instanceOf(Object).isRequired,
};

export default TransformerNode;
