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
import Dialog from './../../../core/view/Dialog';

/**
 * About Dialog
 * @extends React.Component
 */
class AboutDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
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
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title={
                    <div>
                        <div className='brand'>
                            <i className='fw fw-composer fw-lg'/>
                        </div>
                        <div className='version'>v0.963.1.SNAPSHOT</div>
                    </div>
                }
                onHide={this.onDialogHide}
                className='modal-about'
            >
                <p className='aboutWrapper'>
                    Ballerina Composer provides a flexible and powerful browser-based tool for creating your
                    Ballerina programs. You can build your integrations by creating sequence diagrams, dragging
                    elements from a tool palette onto a canvas. As you build the diagrams, the underlying code
                    is written for you, which you can work with in the Source view. You can also use the Swagger
                    view to define services by writing Swagger definitions. You can switch seamlessly between
                    the Design view, Source view, and Swagger view and create your programs in the way that
                    you like to work.
                    <br /><br />
                    Please use &nbsp;
                    <a
                        rel='noopener noreferrer'
                        target='_blank'
                        href='https://github.com/ballerinalang/composer/issues'
                    >
                        GitHub issues
                    </a>
                    &nbsp; tracker for reporting issues.
                </p>
                <div className='pull-right modal-footer'>
                    © {new Date().getFullYear()}
                    &nbsp;
                    <a
                        href='http://wso2.com/'
                        rel='noopener noreferrer'
                        target='_blank'
                    >
                        <i className='fw fw-wso2' /> Inc.
                    </a>
                </div>
            </Dialog>
        );
    }
}

export default AboutDialog;
