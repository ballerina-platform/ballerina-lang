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
import ExamplePreview from './service-preview';

/**
 * React component for welcome page.
 *
 * @class WelcomeView
 * @extends {React.Component}
 */
class WelcomeView extends React.Component {
    /**
     * Creates an instance of WelcomeView.
     * @memberof WelcomeView
     */
    constructor() {
        super();
        this.state = {
            logoLoaded: false,
        };
    }

    /**
     * Rending the view of the samples.
     * @returns {ReactView} The samples view.
     * @memberof WelcomeView
     */
    renderSamples() {
        const pathSeperator = this._options.application.getPathSeperator();
        const ballerinaHome = _.get(this._options, 'balHome');
        let sampleConfigs = [];
        sampleConfigs = this.props.samples.map(sample => ({
            sampleName: sample.name,
            isFile: sample.isFile,
            clickEventCallback: () => {
                // convert paths to platform specific paths
                const sampleFile = sample.path.split('/').join(pathSeperator);
                const sampleFolder = (sample.folder) ? sample.folder.split('/').join(pathSeperator) : '';
                if (sample.isFile) {
                    commandManager.dispatch('open-file', ballerinaHome + sampleFile);
                } else {
                    commandManager.dispatch('open-folder', ballerinaHome + sampleFolder);
                    if (!workspaceExplorer.isActive()) {
                        commandManager.dispatch('toggle-file-explorer');
                    }
                    commandManager.dispatch('open-file', ballerinaHome + sampleFile);
                }
            },
            image: sample.image,
        }));

        return (
            <ExamplePreview sampleConfigs={sampleConfigs} />
        );
    }

    /**
     * Renders view for welcome view.
     *
     * @returns {ReactElement} The view.
     * @memberof WelcomeView
     */
    render() {
        const samplesView = this.renderSamples();
        return (<div className="initial-background-container welcome-page">
            <div className="container-fluid welcome-wrapper">
                <div className="media media-welcome-container">
                    <div className="media-left">
                        <div className="logo" style={{ textAlign: 'center' }}>
                            <a href={undefined}>
                                <img
                                    className="img-responsive"
                                    src="images/BallerinaLogoWelcome.svg"
                                    alt="logo"
                                    onLoad={() => this.setState({ logoLoaded: true })}
                                />
                                {!this.state.logoLoaded &&
                                    <i className="fw fw-loader2 fw-spin fw-lg loader-center" />
                                }
                            </a>
                        </div>

                        <button
                            id="btn-welcome-new"
                            className="btn btn-primary" 
                            onClick={this.props.createNew}
                        >
                            Create New
                        </button>
                        <button
                            id="btn-welcome-open"
                            className="btn btn-secondary"
                            onClick={this.props.openFile}
                        >
                            Open File
                        </button>
                        <button
                            id="btn-welcome-open-dir"
                            className="btn btn-secondary"
                            onClick={this.props.openDirectory}
                        >
                            Open Directory
                        </button>
                        <ul className="nav nav-pills">
                            {/* <li ><a href="#"><i className="fw fw-settings"></i> Settings</a></li>*/}
                            {/* <li ><a href="#"><i className="fw fw-settings"></i> Select a Theme</a></li>*/}
                            {/* <li ><a href="#"><i className="fw fw-settings"></i> Shortcuts</a></li>*/}
                            <li >
                                <a href={this.props.referenceUrl} target="_blank" rel="noopener noreferrer">
                                    <i className="fw fw-document" /> Ballerina by Example </a></li>
                        </ul>
                    </div>
                    <div className="media-body">
                        <div className="welcome-details-wrapper ">
                            <div className="header-title">
                                Try out our samples / templates
                            </div>
                            <div className="details-container">
                                <div id="inner-samples" className="row">
                                    {samplesView}
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>);
    }
}

WelcomeView.propTypes = {
    createNew: PropTypes.func.isRequired,
    openFile: PropTypes.func.isRequired,
    openDirectory: PropTypes.func.isRequired,
    referenceUrl: PropTypes.string.isRequired,
    samples: PropTypes.arrayOf(PropTypes.shape({
        clickEventCallback: PropTypes.func.isRequired,
        sampleName: PropTypes.string.isRequired,
        isFile: PropTypes.bool.isRequired,
        image: PropTypes.string.isRequired,
    })).isRequired,
};

export default WelcomeView;
