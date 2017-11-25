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
import SimpleBBox from '../../../../../model/view/simple-bounding-box';
import * as DesignerDefaults from '../../designer-defaults';
import ActiveArbiter from '../decorators/active-arbiter';
import ImageUtil from './../../../../image-util';
import TransformerNodeModel from '../../../../../model/tree/transformer-node';
import SizingUtils from '../../sizing-util';
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
        const { viewState, model } = this.props;
        const bBox = viewState.bBox;
        // const innerZoneHeight = viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h;
        this.statementBox.y = bBox.y;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;

        const textX = bBox.x + (bBox.w / 2);
        const textY = this.statementBox.y + (this.statementBox.h / 2);
        const expandButtonX = bBox.x + (bBox.w / 2) + 40;
        const expandButtonY = this.statementBox.y + (this.statementBox.h / 2) - 7;

        const actionBbox = new SimpleBBox();
        actionBbox.w = DesignerDefaults.actionBox.width;
        actionBbox.h = DesignerDefaults.actionBox.height;
        actionBbox.x = bBox.x + ((bBox.w - actionBbox.w) / 2);
        actionBbox.y = bBox.y + bBox.h + DesignerDefaults.actionBox.padding.top;
        const titleHeight = this.statementBox.h;
        const titleWidth = new SizingUtils().getTextWidth(model.getSignature());
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

        return (<g className='panel'>
            <g className='panel-header'>
                <rect
                    x={bBox.x}
                    y={bBox.y + annotationBodyHeight}
                    width={bBox.w}
                    height={titleHeight}
                    rx='0'
                    ry='0'
                    className='headingRect'
                    data-original-title=''
                    title=''
                />
                <rect
                    x={bBox.x - 1}
                    y={bBox.y + annotationBodyHeight}
                    height={titleHeight}
                    rx='0'
                    ry='0'
                    className='panel-heading-decorator'
                />
                {allowPublicPrivateFlag && <g>
                    <rect
                        className='publicPrivateRectHolder'
                        x={bBox.x + 8}
                        y={bBox.y + annotationBodyHeight}
                        width='45'
                        height='30'
                    />
                    <text
                        x={bBox.x + 15}
                        y={bBox.y + (titleHeight / 2) + annotationBodyHeight + 4}
                        className='publicPrivateText'
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
                    <text
                        x={textX - 10}
                        y={textY}
                        className='transform-action'
                        onClick={e => this.onExpand()}
                    >{model.getSignature()}</text>
                    <g className='transform-button' onClick={e => this.onExpand()}>
                        <rect
                            x={expandButtonX - 8}
                            y={expandButtonY - 8}
                            width={28}
                            height={30}
                            className='transform-action-button'
                        />
                        <image
                            className='transform-action-icon'
                            x={expandButtonX}
                            y={expandButtonY}
                            width={14}
                            height={14}
                            xlinkHref={ImageUtil.getSVGIconString('expand')}
                        >
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
    viewState: PropTypes.instanceOf(Object).isRequired,
    icon: PropTypes.string.isRequired,
};

TransformerStatementDecorator.contextTypes = {
    designView: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default TransformerStatementDecorator;
