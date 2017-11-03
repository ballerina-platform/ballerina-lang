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
import EditableText from '../decorators/editable-text';
import SimpleBBox from '../../../../../model/view/simple-bounding-box';
import * as DesignerDefaults from '../../designer-defaults';
import ActiveArbiter from '../decorators/active-arbiter';
import ImageUtil from './../../../../image-util';
import TransformerNodeModel from '../../../../../model/tree/transformer-node';
import { util } from '../../sizing-util_bk';
import PanelDecoratorButton from './../../components/decorators/panel-decorator-button';

class TransformerStatementDecorator extends React.Component {

    constructor(props, context) {
        super(props, context);
        this.state = {
            active: 'hidden',
        };
    }

    componentWillMount() {
        const { designView } = this.context;
        if (designView.getTransformActive()) {
            designView.setTransformActive(true, this.props.model);
        }
    }

    onDelete() {
        this.props.model.remove();
    }

    onExpand() {
        const { designView } = this.context;
        designView.setTransformActive(true, this.props.model);
    }

    render() {
        const { viewState, model, icon } = this.props;
        const bBox = viewState.bBox;
        // const innerZoneHeight = viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h;
        this.statementBox.y = bBox.y;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;

        const text_x = bBox.x + (bBox.w / 2);
        const text_y = this.statementBox.y + (this.statementBox.h / 2);
        const expand_button_x = bBox.x + (bBox.w / 2) + 40;
        const expand_button_y = this.statementBox.y + (this.statementBox.h / 2) - 7;

        const actionBbox = new SimpleBBox();
        actionBbox.w = DesignerDefaults.actionBox.width;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + (bBox.w - actionBbox.w) / 2;
        actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;
        let statementRectClass = 'statement-rect';
        const titleHeight = this.statementBox.h;
        const titleWidth = util.getTextWidth(model.getSignature());
        const iconSize = 14;


        const staticButtons = [];

        // Creating delete button.
        const deleteButtonProps = {
            bBox: {
                x: bBox.x - (bBox.w * 2),
                y: bBox.y,
                height: titleHeight,
                width: titleWidth,
            },
            icon: ImageUtil.getSVGIconString('delete'),
            tooltip: 'Delete',
            onClick: () => this.onDelete(),
            key: `${this.props.model.getID()}-delete-button`,
        };

        staticButtons.push(React.createElement(PanelDecoratorButton, deleteButtonProps, null));

        const publicPrivateFlagButtonProps = {
            bBox: {
                x: bBox.x - (bBox.w * 2),
                y: bBox.y,
                height: titleHeight,
                width: titleWidth,
            },
            icon: ImageUtil.getSVGIconString(this.props.model.public ? 'lock' : 'public'),
            tooltip: this.props.model.public ? 'Make private' : 'Make public',
            onClick: () => this.togglePublicPrivateFlag(),
            key: `${this.props.model.getID()}-publicPrivateFlag-button`,
        };

        staticButtons.push(React.createElement(PanelDecoratorButton, publicPrivateFlagButtonProps, null));

        const annotationBodyHeight = 0;
        const allowPublicPrivateFlag = true;
        const publicPrivateFlagoffset = (this.props.model.public) ? 50 : 0;

        return (<g className="panel">
            <g className="panel-header">
                <rect
                    x={bBox.x}
                    y={bBox.y + annotationBodyHeight}
                    width={bBox.w}
                    height={titleHeight}
                    rx="0"
                    ry="0"
                    className="headingRect"
                    data-original-title=""
                    title=""
                />
                <rect x={bBox.x - 1} y={bBox.y + annotationBodyHeight} height={titleHeight} rx="0" ry="0" className="panel-heading-decorator" />
                {allowPublicPrivateFlag && <g>
                    <rect
                        className="publicPrivateRectHolder"
                        x={bBox.x + 8}
                        y={bBox.y + annotationBodyHeight}
                        width='45'
                        height='30'
                    />
                    <text
                        x={bBox.x + 15}
                        y={bBox.y + titleHeight / 2 + annotationBodyHeight + 4}
                        className="publicPrivateText"
                    >{this.props.model.public ? 'public' : null}</text>
                </g>}
                <image
                    x={bBox.x + 15 + publicPrivateFlagoffset}
                    y={bBox.y + 8 + annotationBodyHeight}
                    width={iconSize}
                    height={iconSize}
                    xlinkHref={ImageUtil.getSVGIconString(this.props.icon)}
                />
                <g className='statement-body'>
                    <text x={text_x - 10} y={text_y} className='transform-action'
                        onClick={e => this.onExpand()}>{model.getSignature()}</text>
                    <g className='transform-button' onClick={e => this.onExpand()}>
                        <rect x={expand_button_x - 8}
                            y={expand_button_y - 8}
                            width={28}
                            height={30}
                            className='transform-action-button'/>
                        <image className='transform-action-icon'
                            x={expand_button_x} y={expand_button_y}
                            width={14}
                            height={14}
                            xlinkHref={ImageUtil.getSVGIconString('expand')}>
                            <title>Expand</title>
                        </image>
                    </g>
                </g>
                {staticButtons}
            </g>
        </g>);
    }
}

TransformerStatementDecorator.propTypes = {
    model: PropTypes.instanceOf(TransformerNodeModel).isRequired,
};

TransformerStatementDecorator.contextTypes = {
    designView: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default TransformerStatementDecorator;
