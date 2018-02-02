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
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';
/**
 * React component for a drop down menu
 *
 * @class DropdownMenu
 * @extends {React.Component}
 */
class StructBindingDropDown extends React.Component {

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
                this.context.editor.update();
            }
        }
    }

    /**
     * Handle the on click method on a drop down item
     * @param {function} callback - callback function to be called
     */
    handleOnItemClick(struct) {
        const props = this.props.model.props;
        if (struct !== 'Remove struct binding') {
            // add a receiver to the function
            const fragment = FragmentUtils.createArgumentParameterFragment(struct);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if (!parsedJson.error) {
                props.model.setReceiver(TreeBuilder.build(parsedJson), true);
            }
        } else if (props.model.getReceiver()) {
            delete props.model.receiver;
        }
        props.model.viewState.overlayContainer = {};
        props.model.viewState.showOverlayContainer = false;
        props.model.trigger('tree-modified', {
            origin: props.model,
            type: 'Receiver Changed',
            title: 'Receiver Changed',
            data: {
                node: props.model,
            },
        });
    }

    /**
     * Renders the view for a drop down menu
     *
     * @returns {ReactElement} The view.
     * @memberof DropdownMenu
     */
    render() {
        const props = this.props.model.props;
        const dropDownBBox = props.bBox;
        const style = {
            display: !_.isEmpty(props.model.viewState.overlayContainer) ? 'block' : 'none',
            top: dropDownBBox.y,
            left: dropDownBBox.x,
        };
        const availableStructs = props.structList;
        return (
            <div
                key={props.key}
                id='myDropdown'
                className='dropdown-content'
                style={style}
                ref={(node) => { this.node = node; }}
            >
                {props.model.getReceiver() &&
                <a onClick={e => this.handleOnItemClick('Remove struct binding')}>Remove struct binding</a> }
                {availableStructs.map((struct) => {
                    return (<a onClick={e => this.handleOnItemClick(struct)}>{struct}</a>);
                })}
            </div>);
    }
}

StructBindingDropDown.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

StructBindingDropDown.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default StructBindingDropDown;
