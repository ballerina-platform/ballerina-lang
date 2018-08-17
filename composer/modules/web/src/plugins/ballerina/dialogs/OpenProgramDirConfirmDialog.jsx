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
import { Button } from 'semantic-ui-react';
import Dialog from 'core/view/Dialog';

/**
 * Ask user's confirmation to open the project directory related to file
 * that he/she recently opened.
 *
 * @extends React.Component
 */
class OpenProgramDirConfirmDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            showDialog: true,
        };
        this.onDialogHide = this.onDialogHide.bind(this);
    }

    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            error: '',
            showDialog: false,
        });
        this.props.onCancel();
    }

    /**
     * @inheritdoc
     */
    render() {
        const { file, programDirPath } = this.props;
        return (
            <Dialog
                show={this.state.showDialog}
                title='Open Project Directory'
                titleIcon='info circle'
                size='small'
                actions={
                [
                    <Button
                        onClick={() => {
                            this.setState({
                                showDialog: false,
                            });
                            this.props.onConfirm();
                        }}
                        primary
                    >
                        Open
                    </Button>,
                ]}
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <h4>
                    {`File "${file.name + '.' + file.extension}" resides within a Project Directory.`}
                </h4>
                <p>
                    {`Do you want to open the project directory at ${programDirPath}?`}
                </p>
            </Dialog>
        );
    }
}

OpenProgramDirConfirmDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    programDirPath: PropTypes.string.isRequired,
    onConfirm: PropTypes.func.isRequired,
    onCancel: PropTypes.func,
};

OpenProgramDirConfirmDialog.defaultProps = {
    onCancel: () => {},
};

export default OpenProgramDirConfirmDialog;
