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

/**
 * React component for welcome page.
 *
 * @class WelcomeView
 * @extends {React.PureComponent}
 */
class WelcomeView extends React.Component {
    constructor() {
        super();
        this.state = {
            logoLoaded: false,
        };
    }

    render() {
        return (<div className="initial-background-container">
            <div className="container-fluid welcome-wrapper">
                <div className="media media-welcome-container">
                    <div className="media-left">
                        <div className="logo" style={{ 'text-align': 'center' }}>
                            <a href={undefined}>
                                <img
                                    className="img-responsive"
                                    src="images/ballerina_logo.png"
                                    alt="logo"
                                    onLoad={() => this.setState({ logoLoaded: true })}
                                />
                                {!this.state.logoLoaded &&
                                    <i className="fw fw-loader5 fw-spin fw-2x" />
                                }
                            </a>
                        </div>

                        <button id="btn-welcome-new" className="btn btn-primary">Create New </button>
                        <button id="btn-welcome-open" className="btn btn-secondary"> Open File</button>
                        <button id="btn-welcome-open-dir" className="btn btn-secondary"> Open Directory</button>

                        <ul className="nav nav-pills">
                            {/* <li ><a href="#"><i className="fw fw-settings"></i> Settings</a></li>*/}
                            {/* <li ><a href="#"><i className="fw fw-settings"></i> Select a Theme</a></li>*/}
                            {/* <li ><a href="#"><i className="fw fw-settings"></i> Shortcuts</a></li>*/}
                            <li >
                                <a href="http://ballerinalang.org/docs/user-guide/0.8/" target="_blank" rel="noopener noreferrer">
                                    <i className="fw fw-document" /> User Guide </a></li>
                        </ul>
                    </div>
                    <div className="media-body">
                        <div className="welcome-details-wrapper ">
                            <div className="header-title">
                                Try out our samples / templates
                            </div>
                            <div className="details-container">
                                <div id="inner-samples" className="row" />
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>);
    }
}


export default WelcomeView;
