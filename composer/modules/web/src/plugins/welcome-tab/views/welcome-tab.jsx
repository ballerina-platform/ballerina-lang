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
import { Button, Grid, Menu, Divider, List, Card } from 'semantic-ui-react';
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
        return (
            <Grid className='welcome-page'>
                <Grid.Row className='welcome-navbar' columns={2}>
                    <Grid.Column className='nav-tagline'>
                        Cloud Native Programming Language
                    </Grid.Column>
                    <Grid.Column>
                        <Menu className='top-nav-links' position='right'>
                            <Menu.Item name='Getting started'/>
                            <Menu.Item name='Ballerina by Example' href={this.props.userGuide} target='_blank' rel='noopener noreferrer'/>
                            <Menu.Item name='API Reference'/>
                        </Menu>
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row className='welcome-content-wrapper'>
                    <Grid.Column mobile={7} tablet={5} computer={3} className='leftContainer'>
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
                        <Grid.Column className='button-wrapper'>        
                            <Button
                                id='btn-welcome-new'
                                className='btn-primary'
                                onClick={this.props.createNew}
                            >
                                Create Project
                            </Button>
                            <Button
                                id='btn-welcome-open'
                                className='btn-secondary'
                                onClick={this.props.openDirectory}
                            >
                                Open Project
                            </Button>
                            <Button
                                id='btn-welcome-open'
                                className='btn-secondary'
                                onClick={this.props.openFile}
                            >
                                Create Script
                            </Button>
                        </Grid.Column>
                        {/* <Divider />
                        <Grid.Column>
                            <Grid.Column className='recentWrapper' as='h3'>
                                Recently Opened
                            </Grid.Column>
                            <Grid.Column className='opened-wrapper'>
                                <List.Item className='resentlyOpen'>
                                    <i className='fw fw-folder-open'/> 
                                    <span>serviceChaining</span>
                                </List.Item>
                                <List.Item className='resentlyOpen'>
                                    <i className='fw fw-document'/> 
                                    <span>ATMLocatorService.bal</span>
                                </List.Item>
                                <List.Item className='resentlyOpen'>
                                    <i className='fw fw-document'/> 
                                    <span>echoService.bal</span>
                                </List.Item>
                                <List.Item className='resentlyOpen'>
                                    <i className='fw fw-folder-open'/> 
                                    <span>serviceChaining</span>
                                </List.Item>
                                <List.Item className='resentlyOpen'>
                                    <i className='fw fw-document'/> 
                                    <span>nyseStockQuoteService.bal</span>
                                </List.Item>
                            </Grid.Column>
                        </Grid.Column> */}
                    </Grid.Column>
                    <Grid.Column mobile={9} tablet={11} computer={13} className='rightContainer'>
                        <Grid>
                            <Grid.Row columns={2} className='sample-wrapper'>
                                <Grid.Column mobile={1} tablet={1} computer={1} className='wrapper-label'>
                                    <span>Samples</span> 
                                </Grid.Column>
                                <Grid.Column mobile={13} tablet={14} computer={15} className='thumbnail-container'>
                                    <Grid className='inner-samples' columns='equal'>
                                        {samplesView}
                                    </Grid>
                                </Grid.Column>
                            </Grid.Row>
                        </Grid>
                        <Grid>
                            <Grid.Row columns={2} className='template-wrapper'>
                                <Grid.Column mobile={1} tablet={1} computer={1} className='wrapper-label'>
                                    <span>Template</span> 
                                </Grid.Column>
                                <Grid.Column mobile={14} tablet={14} computer={15} className='thumbnail-container'>
                                    <Grid className='inner-samples' columns='equal'>
                                        {samplesView}
                                    </Grid>
                                </Grid.Column>
                            </Grid.Row>
                        </Grid>
                    </Grid.Column>
                </Grid.Row>
            </Grid>);
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
