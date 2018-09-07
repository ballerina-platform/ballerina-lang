/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Grid, Menu } from 'semantic-ui-react';

/**
 * React component for list of ballerina samples.
 *
 * @class SamplesList
 * @extends {React.Component}
 */
class SamplesList extends React.Component {

    getColumnContents() {
        const columns = [];
        this.props.samples.forEach((sample) => {
            columns[sample.column] = columns[sample.column] || [];
            columns[sample.column].push(sample);
        });

        return columns;
    }

    renderColumnItem(column) {
        return (
            <ul>
                <li className='title'>{column.title}</li>
                <ul>
                    {
                        column.samples.map((sample) => {
                            return (<li className='list-item'>
                                <a
                                    href='#'
                                    onClick={
                                        () => this.props.openSample(sample.url)}
                                >
                                    {sample.name}
                                </a>
                            </li>);
                        })
                    }
                </ul>
            </ul>
        );
    }

    /**
     * Renders view for samples list.
     *
     * @returns {ReactElement} The view.
     * @memberof SamplesList
     */
    render() {
        const samples = this.getColumnContents();
        return (
            <Grid className='welcome-page'>
                <Grid.Column>
                    <Grid.Row className='welcome-navbar' columns={2}>
                        <Grid.Column className='nav-tagline'>
                            Ballerina Language Examples
                        </Grid.Column>
                        <Grid.Column>
                            <Menu className='top-nav-links' position='right'>
                                <a
                                    rel='noopener noreferrer'
                                    target='_blank'
                                    href='https://ballerina.io/learn/api-docs/ballerina/http.html'
                                    onClick={(event) => {
                                        event.preventDefault();
                                        this.props.openLink(event.currentTarget.href);
                                    }}
                                >
                                    <Menu.Item name='API Reference' />
                                </a>
                            </Menu>
                        </Grid.Column>
                    </Grid.Row>
                    <Grid.Row className='welcome-content-wrapper'>
                        <Grid.Column mobile={16} tablet={16} computer={16} className='rightContainer'>
                            <Grid>
                                <Grid.Row columns={4} className='sample-wrapper'>
                                    <Grid.Column mobile={16} tablet={16} computer={4} className=''>
                                        {samples[0].map(column => this.renderColumnItem(column))}
                                    </Grid.Column>
                                    <Grid.Column mobile={16} tablet={16} computer={4} className=''>
                                        {samples[1].map(column => this.renderColumnItem(column))}
                                    </Grid.Column>
                                    <Grid.Column mobile={16} tablet={16} computer={4} className=''>
                                        {samples[2].map(column => this.renderColumnItem(column))}
                                    </Grid.Column>
                                    <Grid.Column mobile={16} tablet={16} computer={4} className=''>
                                        {samples[3].map(column => this.renderColumnItem(column))}
                                    </Grid.Column>
                                </Grid.Row>
                            </Grid>
                        </Grid.Column>
                    </Grid.Row>
                </Grid.Column>
            </Grid>);
    }
}

SamplesList.propTypes = {
    samples: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
        isFile: PropTypes.bool,
        folder: PropTypes.string,
        path: PropTypes.string.isRequired,
        image: PropTypes.string.isRequired,
    })).isRequired,
    openSample: PropTypes.func.isRequired,
    openLink: PropTypes.func.isRequired,
};

export default SamplesList;
