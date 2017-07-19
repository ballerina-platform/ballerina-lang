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
import log from 'log';
import _ from 'lodash';
import commandManager from 'command';
import File from './../../workspace/file';
import { DESIGN_VIEW } from './constants';
import SourceEditor from './source-editor';

class SourceView extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="source-view-container" >
                <div className="wrapperDiv">
                    <div className="outerSourceDiv">
                        <SourceEditor
                            commandManager={this.props.commandManager}
                            file={this.props.file}
                            parseFailed={this.props.parseFailed}
                        />
                        <div className="bottom-right-controls-container">
                            <div className="view-design-btn btn-icon">
                                <div className="bottom-label-icon-wrapper">
                                    <i className="fw fw-design-view fw-inverse" />
                                </div>
                                <div
                                    className="bottom-view-label"
                                    onClick={
                                    () => {
                                        this.context.editor.setActiveView(DESIGN_VIEW);
                                    }
                                }
                                >
                            Design View
                        </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

SourceView.propTypes = {
    file: PropTypes.instanceOf(File).isRequired,
    commandManager: PropTypes.instanceOf(commandManager).isRequired,
    parseFailed: PropTypes.bool.isRequired,
    syntaxErrors: PropTypes.arrayOf(Object).isRequired,
};

SourceView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default SourceView;
