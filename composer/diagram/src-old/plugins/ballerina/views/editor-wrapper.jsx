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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import BallerinaFileEditor from 'plugins/ballerina/views/ballerina-file-editor';
import { EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';


/**
 * Editor for Bal Files
 */
class Editor extends React.Component {
    
     /**
     * @inheritdoc
     */
    getChildContext() {
        return {
            ballerinaPlugin: this.props.ballerinaPlugin,
        };
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.props.file.on(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    /**
     * @inheritdoc
     */
    shouldComponentUpdate(nextProps) {
        return nextProps.isActive();
    }

    /**
     * @inheritdoc
     */
    componetWillUnmount() {
        this.props.file.off(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    /**
     * @inheritdoc
     */
    render() {
        const { width, height } = this.props;
        return (
            <div className='ballerina-editor' style={{ width, height }}>
                <BallerinaFileEditor {...this.props} />
            </div>
        );
    }
}

Editor.propTypes = {
    editorModel: PropTypes.objectOf(Object).isRequired,
    file: PropTypes.objectOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    isActive: PropTypes.func.isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
    }).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
    ballerinaPlugin: PropTypes.objectOf(Object).isRequired,
    onUndoableOperation: PropTypes.func.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

Editor.childContextTypes = {
    ballerinaPlugin: PropTypes.objectOf(Object).isRequired,
};

Editor.defaultProps = {
    isPreviewViewEnabled: false,
};

export default Editor;
