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
import { Scrollbars } from 'react-custom-scrollbars';
import PropTypes from 'prop-types';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import { Overlay, Popover } from 'react-bootstrap/lib';
import classNames from 'classnames';
import File from 'core/workspace/model/file';
import { GO_TO_POSITION } from 'plugins/ballerina/constants/commands';
import { DESIGN_VIEW } from './constants';
import SourceEditor from './source-editor';

/**
 * Source View Component
 */
class SourceView extends React.Component {

    /**
     * Constructor
     * @param {*} props React props
     */
    constructor(props) {
        super(props);
        this.state = {
            displayErrorList: props.displayErrorList,
            syntaxErrors: [],
        };
        this.errorListPopoverTarget = undefined;
        this.onSourceEditorLintErrors = this.onSourceEditorLintErrors.bind(this);
        this.toggleErrorListPopover = this.toggleErrorListPopover.bind(this);
        this.props.commandProxy.on('go-to-node', (node) => {
            this.props.commandProxy.dispatch(GO_TO_POSITION, {
                file: this.props.file,
                row: node.position.startLine,
                column: node.position.startColumn,
            });
        });
    }

    shouldComponentUpdate(nextProps, nextState) {
        return !nextProps.panelResizeInProgress;
    }

    /**
     * Update state with new props
     * @param {*} newProps The new props object.
     */
    componentWillReceiveProps(newProps) {
        this.setState({
            displayErrorList: newProps.displayErrorList,
        });
    }

    /**
     * When source editor finds errors
     * @param {array} lintErrors List of errors received by validations
     */
    onSourceEditorLintErrors(lintErrors) {
        this.setState({
            syntaxErrors: lintErrors,
        });
    }

    /**
     * Toggle error list popover
     */
    toggleErrorListPopover() {
        this.setState({
            displayErrorList: !this.state.displayErrorList,
        });
    }

    /**
     * Render the component
     * @returns {Component} return the component
     */
    render() {
        const hasSyntaxErrors = this.state.syntaxErrors.length > 0;

        const errorListPopover = (
            <Popover
                id='syntax-errors-list-popover'
                title={
                    <div>
                        <i className='fw fw-alert fw-lg' />
                        <span>Cannot update design view due to syntax errors</span>
                    </div>
                }
            >
                <Scrollbars
                    autoHeight
                    autoHeightMin={20}
                    autoHeightMax={350}
                    autoHide // Hide delay in ms
                    autoHideTimeout={1000}
                    universal
                >
                    <ul className='list-group'>
                        {
                            this.state.syntaxErrors.map((error, index) => {
                                return (
                                    <li
                                        key={error.row + error.column + btoa(error.text) + index}
                                        className='list-group-item syntax-error'
                                        onClick={() => {
                                            this.props.commandProxy
                                                .dispatch(GO_TO_POSITION, {
                                                    file: this.props.file,
                                                    row: error.row,
                                                    column: error.column,
                                                });
                                        }}
                                    >
                                        <div>
                                            <span className='line'>line {error.row + 1 + ' '}</span>
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
            <div
                className='source-view-container'
                onClick={
                    () => {
                        if (this.state.displayErrorList) {
                            this.toggleErrorListPopover();
                        }
                    }
                }
                style={{ display: this.props.show ? 'block' : 'none' }}
            >
                <div className='outerSourceDiv'>
                    <SourceEditor
                        commandProxy={this.props.commandProxy}
                        file={this.props.file}
                        parseFailed={this.props.parseFailed}
                        onLintErrors={this.onSourceEditorLintErrors}
                        width={this.props.width}
                        height={this.props.height}
                        ballerinaPlugin={this.context.ballerinaPlugin}
                    />
                    <div
                        className={classNames('bottom-right-controls-container',
                                        { disabled: hasSyntaxErrors })}
                        onClick={
                            () => {
                                if (!hasSyntaxErrors) {
                                    this.context.editor.setActiveView(DESIGN_VIEW);
                                } else {
                                    this.toggleErrorListPopover();
                                }
                            }
                        }
                    >
                        {hasSyntaxErrors && !this.state.displayErrorList &&
                            <CSSTransitionGroup
                                transitionName='error-count-badge'
                                transitionEnterTimeout={300}
                                transitionLeaveTimeout={300}
                            >
                                <div
                                    className='syntax-errors-counter-container'
                                    onClick={this.toggleErrorListPopover}
                                >
                                    <span className='badge'>{this.state.syntaxErrors.length}</span>
                                </div>
                            </CSSTransitionGroup>
                        }
                    </div>
                </div>
            </div>
        );
    }
}

SourceView.propTypes = {
    file: PropTypes.instanceOf(File).isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
    }).isRequired,
    parseFailed: PropTypes.bool.isRequired,
    displayErrorList: PropTypes.bool.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

SourceView.contextTypes = {
    isTabActive: PropTypes.func.isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
    ballerinaPlugin: PropTypes.objectOf(Object).isRequired,
};

export default SourceView;
