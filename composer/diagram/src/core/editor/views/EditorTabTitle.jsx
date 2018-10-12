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
import cn from 'classnames';
import PropTypes from 'prop-types';
import { EVENTS as WORKSPACE_EVENTS } from './../../workspace/constants';

/**
 * React component for Editor Tab Title
 */
class EditorTabTitle extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            isFileDirty: props.editor.file.isDirty,
        };
        this.onFileDirtyStateChange = this.onFileDirtyStateChange.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.props.editor.file
            .on(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        this.props.editor.file
            .off(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
    }

    /**
     * @inheritdoc
     */
    componentWillReceiveProps(nextProps) {
        if (this.props.editor.file.id !== nextProps.editor.file.id) {
            this.props.editor.file
                .off(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
            nextProps.editor.file
                .on(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
            this.setState({
                isFileDirty: nextProps.editor.file.isDirty,
            });
        }
    }

    /**
     * When File's dirty state changed
     * @param {boolean} isFileDirty Flag to indicate dirty state
     */
    onFileDirtyStateChange(isFileDirty) {
        this.setState({
            isFileDirty,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const { editor, editor: { file }, onTabClose, customClass } = this.props;
        return (
            <div
                data-placement="bottom"
                data-toggle="tooltip"
                title={file.isPersisted ? file.fullPath : file.name}
                data-extra="tab-bar-title"
                className={`tab-title-wrapper ${customClass}`}
            >
                <button
                    type="button"
                    className="close close-tab pull-right"
                    onClick={(evt) => {
                        onTabClose(editor);
                        evt.stopPropagation();
                        evt.preventDefault();
                    }}
                >
                    ×
                </button>
                {/* TODO: Add a new contribution point to get icons for each ext and use them here */}
                <i className={
                        cn('fw', 'tab-icon', { 'fw-ballerina': file.extension === 'bal' },
                        { 'fw-document': file.extension !== 'bal' })
                    }
                />
                {file.name + '.' + file.extension}
                {this.state.isFileDirty && <span className="dirty-indicator">*</span> }
            </div>
        );
    }
}

EditorTabTitle.propTypes = {
    customClass: PropTypes.string,
    editor: PropTypes.objectOf(Object).isRequired,
    onTabClose: PropTypes.func.isRequired,
};

EditorTabTitle.defaultProps = {
    customClass: '',
};

export default EditorTabTitle;
