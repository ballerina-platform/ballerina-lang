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
import TreeView from 'react-treeview';
import ReactJson from 'react-json-view';
import JSON5 from 'json5';
import 'react-treeview/react-treeview.css';
import HtmlTree from '@pahans/react-htmltree';
import './frames.scss';
/**
 *
 *
 * @class Frames
 * @extends {React.Component}
 */
class Frames extends React.Component {
    /**
     * @description Parse string to JSON (using json5 since input is not a valid json)
     * @param {string} str
     * @returns {ReactNode}
     * @memberof Frames
     */
    getObject(str = '') {
        const unescaped = str.replace(/\\"/g, '"');
        return JSON5.parse((unescaped.substring(str.indexOf('{'), unescaped.lastIndexOf('}') + 1)));
    }
    /**
     * @description Parse string to JSON
     * @param {any} str
     * @returns {ReactNode}
     * @memberof Frames
     */
    getArray(str) {
        return JSON.parse(str.substring(str.indexOf('] [') + 2, str.lastIndexOf(']') + 1));
    }
    /**
     * Render a Frame
     * @param {object} frame - Frame object
     * @param {int} i - key
     * @returns {ReactNode}
     * @memberof Frames
     */
    renderFrame(frame, i) {
        frame.variables = frame.variables || [];
        return (
            <div key={i}>
                {frame.variables.map((variable) => {
                    const { type = '', name, value } = variable;
                    const label = <span className='node'><strong>{name}</strong>{` (${type})`}</span>;
                    if (type.toLowerCase().includes('json')
                        || type.toLowerCase().includes('struct')
                        || type.toLowerCase().includes('map')
                        || variable.value.startsWith('struct')) {
                        return (
                            <TreeView key={name} nodeLabel={label} defaultCollapsed>
                                <div className='node'>Value:</div>
                                <ReactJson
                                    src={this.getObject(variable.value)}
                                    theme='eighties'
                                    name={name}
                                    displayDataTypes={false}
                                    collapsed={1}
                                    displayObjectSize={false}
                                />
                            </TreeView>);
                    } else if (type.toLowerCase().includes('array')) {
                        return (
                            <TreeView key={name} nodeLabel={label} defaultCollapsed>
                                <div className='node'>Value:</div>
                                <ReactJson
                                    src={this.getArray(variable.value)}
                                    theme='eighties'
                                    name={name}
                                    displayDataTypes={false}
                                    collapsed={1}
                                    displayObjectSize={false}
                                />
                            </TreeView>
                        );
                    } else if (type.toLowerCase().includes('xml')) {
                        return (
                            <TreeView key={name} nodeLabel={label} defaultCollapsed>
                                <div className='node'>Value:</div>
                                <HtmlTree source={variable.value.substr(1)} theme='firefox-devtools.dark' />
                            </TreeView>
                        );
                    } else {
                        const varLabel = <span className='node'><strong>{name}</strong>{` (${type})`}</span>;
                        return (
                            <TreeView key={name} nodeLabel={varLabel} defaultCollapsed>
                                <div className='node'>Value: {value}</div>
                            </TreeView>
                        );
                    }
                })}
            </div>
        );
    }
    /**
     * @inheritdoc
     * @memberof Frames
     */
    render() {
        const { message: { frames = [] } } = this.props;
        return (
            <div className='debugger-frames-wrapper'>
                {
                    frames.length ?
                        <div className='debug-panel-header debug-frame-header'>
                            <div><a className='tool-group-header-title'>Frames</a></div>
                        </div> : ''
                }
                <div className='debugger-frames-container'>
                    {frames.map((frame, i) => {
                        return (
                            <div className='' key={frame.frameName}>
                                <div className='frame-title'>
                                    <h4>

                                        {`${frame.frameName}`}
                                        <div className='debug-frame-pkg-name'>
                                            <i className='fw fw-package' />{`${frame.packageName}`}
                                        </div>

                                    </h4>
                                </div>
                                <div className='clearfix' />
                                <div
                                    id={`#debugger-frame-${frame.frameName}`}
                                    className=''
                                >
                                    <div className=''>
                                        {this.renderFrame(frame, i)}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        );
    }
}

Frames.propTypes = {
    message: PropTypes.instanceOf(Object),
};

Frames.defaultProps = {
    message: {},
};

export default Frames;
