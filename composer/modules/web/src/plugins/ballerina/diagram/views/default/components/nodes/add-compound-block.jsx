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
import './service-definition.css';
import SimpleBBox from './../../../../../model/view/simple-bounding-box';

/**
 * class for rendering the add compound block.
 * @class AddCompoundBlock
 * @extends React.Component
 * */
class AddCompoundBlock extends React.Component {
    /**
     * Constructor for AddCompoundBlock
     * @param {Object} props - properties to render the view.
     * */
    constructor(props) {
        super(props);

        this.onAddBlockClick = this.onAddBlockClick.bind(this);
    }

    /**
     * invoke on component did mount.
     * */
    componentDidMount() {
        document.addEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * invoke on component did update.
     * */
    componentDidUpdate() {
        document.addEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * invoke on component will unmount.
     * */
    componentWillUnmount() {
        document.removeEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * Handle the event on add block click.
     * */
    onAddBlockClick() {
        const blocksToBeAdded = this.props.blocksToBeAdded;
        const overlayComponents = {
            kind: 'MultiBlockSelect',
            props: {
                key: this.props.model.getID(),
                model: this.props.model,
                blocksToBeAdded,
            },
        };

        this.props.model.viewState.showOverlayContainer = true;
        this.props.model.viewState.overlayContainer = overlayComponents;
        this.context.editor.update();
    }

    /**
     * Render the component
     * @return {XML} react component.
     * */
    render() {
        const model = this.props.model;
        const label = new SimpleBBox();
        label.x = model.viewState.components['statement-box'].x
            + model.viewState.components['statement-box'].w
            + model.viewState.bBox.expansionW - 10 + 20;
        label.y = model.viewState.components['statement-box'].y
            + model.viewState.components['statement-box'].h - 15;
        return (
            <g
                onClick={this.onAddBlockClick}
                ref={(node) => {
                    this.node = node;
                }}
            >
                <title>Add Block</title>
                <rect
                    x={model.viewState.components['statement-box'].x
                    + model.viewState.components['statement-box'].w
                    + model.viewState.bBox.expansionW - 10}
                    y={model.viewState.components['statement-box'].y
                    + model.viewState.components['statement-box'].h - 25}
                    width={20}
                    height={20}
                    rx={10}
                    ry={10}
                    className='add-catch-button'
                />
                <text
                    x={model.viewState.components['statement-box'].x
                    + model.viewState.components['statement-box'].w
                    + model.viewState.bBox.expansionW - 4}
                    y={model.viewState.components['statement-box'].y
                    + model.viewState.components['statement-box'].h - 15}
                    width={20}
                    height={20}
                    className='add-catch-button-label'
                >
                    +
                </text>
            </g>
        );
    }
}

AddCompoundBlock.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default AddCompoundBlock;
