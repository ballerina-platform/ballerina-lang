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

/**
 * Class for view overlay of Multi block select.
 * @class MultiBlockSelect
 * @extends React.Component
 * */
class AddBlockSelect extends React.Component {
    /**
     * Constructor for MultiBlockSelect.
     * @param {Object} props - properties to render the view.
     * */
    constructor(props) {
        super(props);

        this.handleAddBlock = this.handleAddBlock.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
    }

    /**
     * invoke on component did mount.
     * */
    componentDidMount() {
        if (this.props.model.props.model.viewState.showOverlayContainer) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    /**
     * invoke on component did update.
     * */
    componentDidUpdate() {
        if (this.props.model.props.model.viewState.showOverlayContainer) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    /**
     * invoke on component will unmount.
     * */
    componentWillUnmount() {
        document.removeEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * Handles the outside click event of the drop down
     * @param {Object} e - event object.
     */
    handleOutsideClick(e) {
        if (this.node) {
            if (!this.node.contains(e.target)) {
                this.props.model.props.model.viewState.showOverlayContainer = false;
                this.props.model.props.model.viewState.overlayContainer = {};
                this.context.editor.update();
            }
        }
    }

    /**
     * Handle add block to the compound statement.
     * @param {Object} block - block node.
     * */
    handleAddBlock(block) {
        block.addBlock();
        this.props.model.props.model.viewState.showOverlayContainer = false;
        this.props.model.props.model.viewState.overlayContainer = {};
        this.context.editor.update();
    }

    /**
     * Render multi block select view.
     * @return {XML} return view rendered.
     * */
    render() {
        const props = this.props.model.props;
        const blocksToBeAdded = this.props.model.props.blocksToBeAdded;
        const style = {
            display: 'block',
            top: props.y,
            left: props.x,
        };

        return (
            <div
                id='blockDropdown'
                className='dropdown-content'
                style={style}
                ref={(node) => {
                    this.node = node;
                }}
            >
                {blocksToBeAdded.map((block) => {
                    return (<a
                        key={block.name}
                        name={block.name}
                        onClick={e => this.handleAddBlock(block)}
                    >{block.name}</a>);
                })}
            </div>
        );
    }
}

AddBlockSelect.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default AddBlockSelect;
