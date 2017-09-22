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
import DebugManager from '../DebugManager';
import './BreakpointHoc.scss';

/**
 * Higher order component to add add/remove breakpoint for diagram view nodes
 * @param {React.Component} WrappedComponent - React component to be wrapped
 * @returns {React.Component} - WrappedComponent with props addBreakpoints, showDebugHit
 */
function breakpointHoc(WrappedComponent) {
    const newComponent = class BreakpointHoc extends React.Component {
        /**
         * Creates an instance of HOC.
         */
        constructor() {
            super();
            this.state = {
                isBreakpoint: false,
                isDebugHit: false,
            };

            this.addBreakpoint.bind(this);
            this.removeBreakpoint.bind(this);
            this.onBreakpointClick.bind(this);
        }
        /**
         * hook for componentDidMount
         */
        componentDidMount() {
            if (this.props.model.isBreakpoint) {
                this.setState({
                    isBreakpoint: true,
                });
            }
            this.addListner = DebugManager.on('breakpoint-added', ({ lineNumber, fileName: bpFileName }) => {
                const { editor } = this.context;
                const fileName = `${editor.props.file.name}.${editor.props.file.extension}`;
                if (fileName === bpFileName && this.props.model.getLineNumber() === lineNumber) {
                    this.setState({
                        isBreakpoint: true,
                    });
                    if (typeof this.props.model.addBreakpoint === 'function') {
                        this.props.model.addBreakpoint();
                    }
                }
            });
            this.removeListner = DebugManager.on('breakpoint-removed', ({ lineNumber, fileName: bpFileName }) => {
                const { editor } = this.context;
                const fileName = `${editor.props.file.name}.${editor.props.file.extension}`;
                if (fileName === bpFileName && this.props.model.getLineNumber() === lineNumber) {
                    this.setState({
                        isBreakpoint: false,
                    });
                    if (typeof this.props.model.removeBreakPoint === 'function') {
                        this.props.model.removeBreakPoint();
                    }
                }
            });
            this.hitListner = DebugManager.on('debug-hit', this.debugHit.bind(this));
            this.endListner = DebugManager.on('session-ended', this.end.bind(this));
            this.cmpListner = DebugManager.on('execution-ended', this.end.bind(this));
            this.continueListner = DebugManager.on('resume-execution', this.end.bind(this));
        }
        /**
         * hook for componentWillUnmount
         */
        componentWillUnmount() {
            DebugManager.off('breakpoint-added', this.addListner, this);
            DebugManager.off('breakpoint-removed', this.removeListner, this);
            DebugManager.off('debug-hit', this.hitListner, this);
            DebugManager.off('session-ended', this.endListner, this);
            DebugManager.off('execution-ended', this.cmpListner, this);
            DebugManager.off('resume-execution', this.continueListner, this);
        }
        /**
         * indicate a debug hit
         */
        debugHit(message) {
            const { editor } = this.context;
            const fileName = `${editor.props.file.name}.${editor.props.file.extension}`;
            const lineNumber = this.props.model.getLineNumber();
            const position = message.location;
            const { astRoot } = this.context;
            const packagePath = astRoot.getPackageDefinition().getPackageName() || '.';

            // remove package path from file name
            let fileIdentifier;
            if (position.fileName.includes('/')) {
                const fileArray = position.fileName.split('/');
                fileIdentifier = fileArray[fileArray.length - 1];
            } else {
                fileIdentifier = position.fileName;
            }

            if (message.location.lineNumber === lineNumber
                    && fileName === fileIdentifier
                    && packagePath === position.packagePath) {

                this.setState({
                    isDebugHit: true,
                });
            } else {
                this.setState({
                    isDebugHit: false,
                });
            }
        }

        end() {
            this.setState({
                isDebugHit: false,
            });
        }
        /**
         * add breakpoint
         */
        addBreakpoint() {
            const lineNumber = this.props.model.getLineNumber();
            const { editor, astRoot } = this.context;
            const fileName = `${editor.props.file.name}.${editor.props.file.extension}`;
            const packagePath = astRoot.getPackageDefinition().getPackageName() || '.';
            DebugManager.addBreakPoint(lineNumber, fileName, packagePath);
        }
        /**
         * remove breakpoint
         */
        removeBreakpoint() {
            const lineNumber = this.props.model.getLineNumber();
            const { editor, astRoot } = this.context;
            const fileName = `${editor.props.file.name}.${editor.props.file.extension}`;
            const packagePath = astRoot.getPackageDefinition().getPackageName() || '.';
            DebugManager.removeBreakPoint(lineNumber, fileName, packagePath);
        }

        /**
         * Handles click event of breakpoint, adds/remove breakpoint from the node when click event fired
         *
         */
        onBreakpointClick() {
            const { isBreakpoint } = this.state;
            if (isBreakpoint) {
                this.setState({
                    isBreakpoint: false,
                });
                this.removeBreakpoint();
            } else {
                this.setState({
                    isBreakpoint: true,
                });
                this.addBreakpoint();
            }
        }
        /**
         * @inheritdoc
         */
        render() {  
            const newProps = {
                isBreakpoint: this.state.isBreakpoint,
                isDebugHit: this.state.isDebugHit,
                onBreakpointClick: this.onBreakpointClick.bind(this),
            };
            return <WrappedComponent {...this.props} {...newProps} />;
        }
    };

    newComponent.contextTypes = {
        editor: PropTypes.instanceOf(Object).isRequired,
        astRoot: PropTypes.instanceOf(Object).isRequired,
    };

    newComponent.propTypes = {
        model: PropTypes.instanceOf(Object).isRequired,
    };

    return newComponent;
}


export default breakpointHoc;
