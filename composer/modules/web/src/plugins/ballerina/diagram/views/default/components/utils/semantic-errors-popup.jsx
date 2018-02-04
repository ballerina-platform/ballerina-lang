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
import { Overlay, Popover } from 'react-bootstrap/lib';
import { Scrollbars } from 'react-custom-scrollbars';
import './semantic-error-popup.css';
/**
 * React component for the semantic error popup in design view
 *
 * @class SemanticErrorPopup
 * @extends {React.Component}
 */
class SemanticErrorPopup extends React.Component {

    /**
     * Constructor
     * @param {*} props React props
     */
    constructor(props) {
        super(props);
        this.state = {
            displayErrorList: false,
        };
        this.errorListPopoverTarget = undefined;
        this.toggleMouseOut = this.toggleMouseOut.bind(this);
        this.toggleMouseOver = this.toggleMouseOver.bind(this);
        this.onJumpToCodeLine = this.onJumpToCodeLine.bind(this);
    }

    /**
     * Toggle mouse over of the warning icon
     */
    toggleMouseOver() {
        this.setState({
            displayErrorList: true,
        });
    }

    /**
     * Toggle mouse out of the warning icon
     */
    toggleMouseOut() {
        this.setState({
            displayErrorList: false,
        });
    }

    /**
     * Navigates to code line in the source view from the design view node
     */
    onJumpToCodeLine(row, column) {
        const { editor } = this.context;
        editor.jumpToSourcePosition(row, column);
    }

    /**
     * Renders the semantic error popup in design view
     *
     * @returns {ReactElement} The view.
     * @memberof SemanticErrorPopup
     */
    render() {
        const props = this.props.model.props;
        const iconStyle = {
            position: 'absolute',
            color: 'red',
            left: props.bBox.x - 5,
            top: props.bBox.y,
        };
        const hasSyntaxErrors = props.errors.length > 0;

        const errorListPopover = (
            <Popover
                id='semantic-errors-list-popover'
                onMouseOver={
                    () => {
                        this.toggleMouseOver();
                    }
                }
                onMouseOut={() => {
                    this.toggleMouseOut();
                }}
            >
                <Scrollbars
                    autoHeight
                    autoHeightMin={20}
                    autoHeightMax={125}
                    autoHide // Hide delay in ms
                    autoHideTimeout={1000}
                    universal
                >
                    <ul className='list-group'>
                        {
                            props.errors.map((error, index) => {
                                return (
                                    <li
                                        key={error.row + error.column + btoa(error.text) + index}
                                        className='list-group-item'
                                        onClick={() => this.onJumpToCodeLine(error.row - 1, error.column - 1)}
                                    >
                                        <div>
                                            <span className='line'>line {error.row + ' '}</span>
                                            :{' ' + error.text}
                                        </div>
                                    </li>
                                );
                            })
                        }
                    </ul>
                </Scrollbars>
            </Popover>
        );
        return (
            <div id='semanticErrorPopup' style={iconStyle} className='semanticDesignErrors'>
                <i
                    className='fw fw-warning warningIcon'
                    ref={
                        (ref) => {
                            this.errorListPopoverTarget = ref;
                        }
                    }
                    onMouseOver={
                        () => {
                            this.toggleMouseOver();
                        }
                    }
                    onMouseOut={() => {
                        this.toggleMouseOut();
                    }}
                />
                {hasSyntaxErrors &&
                    <Overlay
                        show={this.state.displayErrorList}
                        container={this}
                        target={this.errorListPopoverTarget}
                        placement={props.placement}
                        onMouseOver={
                            () => {
                                this.toggleMouseOver();
                            }
                        }
                        onMouseOut={() => {
                            this.toggleMouseOut();
                        }}
                    >
                        {errorListPopover}
                    </Overlay>
                    }
            </div>
        );
    }
}
SemanticErrorPopup.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default SemanticErrorPopup;
