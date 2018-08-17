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
 * Ask user's confirmation to move the File to correct Project Directory upon
 * changing the package declaration or fix package name to match file structure.
 *
 * @extends React.Component
 */
class FixPackageNameOrPathConfirmDialog extends React.Component {

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
        const { file, correctPkg, correctPath, programDir } = this.props;
        return (
            <Dialog
                show={this.state.showDialog}
                title='Invalid Package Name for file path'
                titleIcon='warning circle'
                size='small'
                actions={
                [
                    <Button
                        key='move-btn'
                        primary
                        onClick={() => {
                            this.setState({
                                showDialog: false,
                            });
                            this.props.onMoveFile();
                        }}
                    >
                        Save & Move
                    </Button>,
                    <Button
                        key='fix-btn'
                        secondary
                        onClick={() => {
                            this.setState({
                                showDialog: false,
                            });
                            this.props.onFixPackage();
                        }}
                    >
                        Change Package
                    </Button>,
                ]}
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <h4>
                    {`Package declaration for "${file.name + '.' + file.extension}" is incorrect.`}
                </h4>
                <p>
                    {`Following fixes are possible with respective to program dir at ${programDir}`}
                </p>
                <ul>
                    <li>{`Save and move file to correct directory at ${correctPath}`}</li>
                    <li>{`Change package to ${correctPkg}`}</li>
                </ul>
            </Dialog>
        );
    }
}

FixPackageNameOrPathConfirmDialog.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
    correctPkg: PropTypes.string.isRequired,
    programDir: PropTypes.string.isRequired,
    correctPath: PropTypes.string.isRequired,
    onMoveFile: PropTypes.func.isRequired,
    onFixPackage: PropTypes.func.isRequired,
    onCancel: PropTypes.func,
};

FixPackageNameOrPathConfirmDialog.defaultProps = {
    onCancel: () => {},
};

export default FixPackageNameOrPathConfirmDialog;
