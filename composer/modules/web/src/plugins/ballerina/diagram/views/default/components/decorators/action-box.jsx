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
import ImageUtil from '../../../../image-util';
import './action-box.css';
import Breakpoint from './breakpoint';
import { withDragStateKnowledge } from '../../../../../drag-drop/util';

/**
 * React component for Actionbox
 *
 * @class ActionBox
 * @extends {React.Component}
 */
class ActionBox extends React.Component {

    /**
     * Creates an instance of ActionBox.
     * @param {any} props React properties.
     *
     * @memberof ActionBox
     */
    constructor(props) {
        super(props);
        this.state = { inGracePeriod: false };
        this.isHiddenToHidden = true;
    }

    /**
     * {@inheritDoc}
     *
     * @param {Object} nextProps New properties.
     *
     * @memberof ActionBox
     */
    componentWillReceiveProps(nextProps) {
        this.isHiddenToHidden = !(this.props.show || nextProps.show);
    }

    /**
     * Renders the view of action box.
     *
     * @returns {ReactElement} The view.
     *
     * @memberof ActionBox
     */
    render() {
        const bBox = this.props.bBox;
        const numIcons = 2 - (this.props.isDefaultWorker ? 1 : 0);
        const iconSize = 14;
        const y = bBox.y + ((bBox.h - iconSize) / 2);
        const horizontalGap = (bBox.w - (iconSize * numIcons)) / (numIcons + 1);
        let className;
        // hide action box when mouse moved away or when a drag-drop is happening
        if (this.props.show === 'hidden' || this.props.dragState.isDragging) {
            className = 'hide-action';
        } else if (this.props.show === 'visible') {
            className = 'show-action';
        } else {
            className = 'delayed-hide-action';
        }

        return (<g className={className}>
            <rect
                x={bBox.x}
                y={bBox.y}
                width={bBox.w}
                height={bBox.h}
                rx='0'
                ry='0'
                className='property-pane-action-button-wrapper'
            />
            { !this.props.isDefaultWorker &&
            <text
                width={iconSize}
                height={iconSize}
                x={bBox.x + horizontalGap}
                y={y + iconSize}
                fontFamily='font-ballerina'
                fontSize={iconSize}
                className={this.props.disableButtons.delete ? 'property-pane-action-button-delete-disabled'
                    : 'property-pane-action-button-delete'}
                onClick={this.props.disableButtons.delete ? () => {} : this.props.onDelete}
            >{ImageUtil.getCodePoint('delete')}
                <title>Delete</title> </text> }
            <text
                width={iconSize}
                height={iconSize}
                x={bBox.x + (iconSize * (numIcons - 1)) + (horizontalGap * numIcons)}
                y={y + iconSize}
                fontFamily='font-ballerina'
                fontSize={iconSize}
                className={this.props.disableButtons.jump ? 'property-pane-action-button-jump-disabled'
                    : 'property-pane-action-button-jump'}
                onClick={this.props.disableButtons.jump ? () => {} : this.props.onJumptoCodeLine}
            >{ImageUtil.getCodePoint('code-view')}
                <title>Jump to Source</title> </text>
        </g>);
    }

}

ActionBox.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }).isRequired,
    show: PropTypes.string,
    onDelete: PropTypes.func.isRequired,
    onJumptoCodeLine: PropTypes.func.isRequired,
    disableButtons: PropTypes.shape({
        debug: PropTypes.bool,
        delete: PropTypes.bool,
        jump: PropTypes.bool,
    }).isRequired,
    dragState: PropTypes.shape({
        isDragging: PropTypes.bool.isRequired,
    }).isRequired,
};

ActionBox.defaultProps = {
    show: false,
    disableButtons: {
        debug: false,
        delete: false,
        jump: false,
    },
};


export default withDragStateKnowledge(ActionBox);
