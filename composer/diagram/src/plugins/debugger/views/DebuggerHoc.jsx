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
import TreeUtil from 'plugins/ballerina/model/tree-util.js';
import DebugManager from '../DebugManager';
import './DebuggerHoc.scss';

/**
 * Higher order component to add add/remove breakpoint for diagram view nodes
 * @param {React.Component} WrappedComponent - React component to be wrapped
 * @returns {React.Component} - WrappedComponent with props addBreakpoints, showDebugHit
 */
function debuggerHoc(WrappedComponent) {
    const newComponent = class DebuggerHoc extends React.Component {
        constructor() {
            super();
            this.state = {
                breakpoints: [],
                debugHit: null,
            };
        }
        /**
         * hook for componentDidMount
         */
        componentDidMount() {
            DebugManager.on('breakpoint-added', this.updateBreakpoints, this);
            DebugManager.on('breakpoint-removed', this.updateBreakpoints, this);
            DebugManager.on('active-debug-hit', this.debugHit, this);
            DebugManager.on('execution-ended', this.end, this);
            DebugManager.on('resume-execution', this.end, this);
        }
        /**
         * hook for componentWillUnmount
         */
        componentWillUnmount() {
            DebugManager.off('breakpoint-added', this.updateBreakpoints, this);
            DebugManager.off('breakpoint-removed', this.updateBreakpoints, this);
            DebugManager.off('active-debug-hit', this.debugHit, this);
            DebugManager.off('execution-ended', this.end, this);
            DebugManager.off('resume-execution', this.end, this);
        }
        /**
         * update breakpoints
         */
        updateBreakpoints() {
            const fileName = this.getFileName();
            const breakpoints = DebugManager.getDebugPoints(fileName);
            this.setState({
                breakpoints,
            });
        }
        /**
         * @description Get package name from astRoot
         *
         * @returns string - Package name
         */
        getPackageName() {
            return this.props.file.debugPackagePath;
        }
        /**
         * indicate debughit
         *
         * @param {object} message - parsed message from backend
         */
        debugHit(message) {
            const fileName = this.getFileName();
            const position = message.location;
            const packagePath = this.getPackageName();
            // remove package path from file name
            let fileIdentifier;
            if (position.fileName.includes('/')) {
                const fileArray = position.fileName.split('/');
                fileIdentifier = fileArray[fileArray.length - 1];
            } else {
                fileIdentifier = position.fileName;
            }

            if (fileName === fileIdentifier && packagePath === position.packagePath) {
                this.setState({
                    debugHit: position.lineNumber,
                });
            }
        }
        /**
         * end debug session
         */
        end() {
            this.setState({
                debugHit: null,
            });
        }
        /**
         * add breakpoint
         */
        addBreakpoint(lineNumber) {
            const fileName = this.getFileName();
            const packagePath = this.getPackageName();
            DebugManager.addBreakPoint(lineNumber, fileName, packagePath);
        }
        /**
         * remove breakpoint
         */
        removeBreakpoint(lineNumber) {
            const fileName = this.getFileName();
            const packagePath = this.getPackageName();
            DebugManager.removeBreakPoint(lineNumber, fileName, packagePath);
        }
        /**
         * Get File name with extension
         * @returns String - file name
         */
        getFileName() {
            return `${this.props.file.name}.${this.props.file.extension}`;
        }

        /**
         * @inheritdoc
         */
        render() {
            const fileName = this.getFileName();
            const breakpoints = DebugManager.getDebugPoints(fileName);

            const newProps = {
                breakpoints,
                debugHit: this.state.debugHit,
                addBreakpoint: this.addBreakpoint.bind(this),
                removeBreakpoint: this.removeBreakpoint.bind(this),
            };
            return <WrappedComponent {...this.props} {...newProps} />;
        }
    };

    newComponent.propTypes = {
        file: PropTypes.instanceOf(Object).isRequired,
    };

    newComponent.contextTypes = {
        astRoot: PropTypes.instanceOf(Object),
    };

    return newComponent;
}


export default debuggerHoc;
