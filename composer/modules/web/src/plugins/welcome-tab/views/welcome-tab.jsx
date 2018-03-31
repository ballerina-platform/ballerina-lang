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
import { Button } from 'semantic-ui-react';
import { getPathSeperator } from 'api-client/api-client';
import { COMMANDS as WORKSPACE_COMMANDS, VIEWS as WORKSPACE_VIEWS } from 'core/workspace/constants';
import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import SamplesPreview from './samples-preview';

/**
 * React component for welcome tab.
 *
 * @class WelcomeTab
 * @extends {React.Component}
 */
class WelcomeTab extends React.Component {
    /**
     * Creates an instance of WelcomeTab.
     * @memberof WelcomeView
     */
    constructor() {
        super();
        this.state = {
            logoLoaded: false,
        };
    }

    renderSamples() {
        const pathSeperator = getPathSeperator();
        const ballerinaHome = this.props.balHome;
        let sampleConfigs = [];
        sampleConfigs = this.props.samples.map(sample => ({
            sampleName: sample.name,
            isFile: sample.isFile,
            clickEventCallback: () => {
                // convert paths to platform specific paths
                const sampleFile = sample.path.split('/').join(pathSeperator);
                const sampleFolder = sample.folder ? sample.folder.split('/').join(pathSeperator) : '';
                if (sample.isFile) {
                    this.props.commandManager.dispatch(WORKSPACE_COMMANDS.OPEN_FILE, {
                        filePath: ballerinaHome + sampleFile,
                        ext: 'bal',
                    });
                } else {
                    if (sample.openFolder) {
                        this.props.commandManager.dispatch(WORKSPACE_COMMANDS.OPEN_FOLDER, {
                            folderPath: ballerinaHome + sampleFolder,
                        });
                        this.props.commandManager.dispatch(LAYOUT_COMMANDS.SHOW_VIEW, { id: WORKSPACE_VIEWS.EXPLORER });
                    }
                    this.props.commandManager.dispatch(WORKSPACE_COMMANDS.OPEN_FILE, {
                        filePath: ballerinaHome + sampleFile,
                        ext: 'bal',
                    });
                }
            },
            image: sample.image,
        }));

        return (
            <SamplesPreview sampleConfigs={sampleConfigs} />
        );
    }

    /**
     * Renders view for welcome view.
     *
     * @returns {ReactElement} The view.
     * @memberof WelcomeTab
     */
    render() {
        const samplesView = this.renderSamples();
        return (<div className='initial-background-container welcome-page'>
            <div className='container-fluid welcome-wrapper'>
                <div className='media media-welcome-container'>
                    <div className='media-left'>
                        <div className='logo' style={{ textAlign: 'center' }}>
                            <a href={undefined}>
                                <img
                                    className='img-responsive'
                                    src='images/BallerinaLogoWelcome.svg'
                                    alt='logo'
                                    onLoad={() => this.setState({ logoLoaded: true })}
                                />
                                {!this.state.logoLoaded &&
                                    <i className='fw fw-loader2 fw-spin fw-lg loader-center' />
                                }
                            </a>
                        </div>

                        <Button
                            id='btn-welcome-new'
                            className='btn-primary'
                            onClick={this.props.createNew}
                        >
                            Create New
                        </Button>
                        <Button
                            id='btn-welcome-open'
                            className='btn-secondary'
                            onClick={this.props.openFile}
                        >
                            Open File
                        </Button>
                        <Button
                            id='btn-welcome-open-dir'
                            className='btn-secondary'
                            onClick={this.props.openDirectory}
                        >
                            Open Directory
                        </Button>
                        <ul className='nav nav-pills'>
                            <li >
                                <a href={this.props.userGuide} target='_blank' rel='noopener noreferrer'>
                                    <i className='fw fw-document' /> Ballerina by Example </a></li>
                        </ul>
                    </div>
                    <div className='media-body'>
                        <div className='welcome-details-wrapper '>
                            <div className='header-title'>
                                Try out our samples / templates
                            </div>
                            <div className='details-container ui grid'>
                                <div id='inner-samples' className='row'>
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

WelcomeTab.propTypes = {
    createNew: PropTypes.func.isRequired,
    openFile: PropTypes.func.isRequired,
    openDirectory: PropTypes.func.isRequired,
    userGuide: PropTypes.string.isRequired,
    samples: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
        isFile: PropTypes.bool,
        folder: PropTypes.string,
        path: PropTypes.string.isRequired,
        image: PropTypes.string.isRequired,
    })).isRequired,
    balHome: PropTypes.string.isRequired,
    commandManager: PropTypes.objectOf(Object).isRequired,
};

export default WelcomeTab;
