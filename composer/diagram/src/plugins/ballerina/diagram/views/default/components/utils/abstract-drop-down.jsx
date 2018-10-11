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
import _ from 'lodash';
import './abstract-drop-down.css';
/**
 * React component for a drop down menu
 *
 * @class DropdownMenu
 * @extends {React.Component}
 */
class AbstractDropdown extends React.Component {

    constructor(props) {
        super(props);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
        this.handleOnItemClick = this.handleOnItemClick.bind(this);
    }

    componentDidMount() {
        if (!_.isEmpty(this.props.model.props.model.viewState.overlayContainer)) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentDidUpdate() {
        if (!_.isEmpty(this.props.model.props.model.viewState.overlayContainer)) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentWillUnmount() {
        document.removeEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * Handles the outside click event of the drop down
     * @param e
     */
    handleOutsideClick(e) {
        if (this.node) {
            if (!this.node.contains(e.target)) {
                this.props.model.props.model.viewState.overlayContainer = {};
                this.props.model.props.model.viewState.showOverlayContainer = false;
                this.props.model.props.editor.update(true);
            }
        }
    }

    /**
     * Handle the on click method on a drop down item
     * @param {function} callback - callback function to be called
     */
    handleOnItemClick(callback) {
        const props = this.props.model.props;
        callback();
        props.model.viewState.overlayContainer = {};
        props.model.viewState.showOverlayContainer = false;
    }

    /**
     * Renders the view for a drop down menu
     *
     * @returns {ReactElement} The view.
     * @memberof DropdownMenu
     */
    render() {
        const dropDownBBox = this.props.model.props.dropDownBBox;
        const style = {
            display: !_.isEmpty(this.props.model.props.model.viewState.overlayContainer) ? 'block' : 'none',
            top: dropDownBBox.y,
            left: dropDownBBox.x,
        };
        const metaInfoList = this.props.model.props.itemsMeta;
        return (
            <div
                id='myDropdown'
                className='dropdown-content'
                style={style}
                ref={(node) => { this.node = node; }}
            >
                {metaInfoList.map((meta) => {
                    return (<a onClick={e => this.handleOnItemClick(() => meta.callback(meta.text))}>{meta.text}</a>);
                })}
            </div>);
    }
}

AbstractDropdown.propTypes = {
    items: {
        text: PropTypes.string.isRequired,
    },
};

export default AbstractDropdown;
