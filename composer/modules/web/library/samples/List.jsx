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
import { Grid, Input } from 'semantic-ui-react';
import _ from 'lodash';

/**
 * React component for list of ballerina samples.
 *
 * @class SamplesList
 * @extends {React.Component}
 */
class SamplesList extends React.Component {

    constructor(props, context) {
        super(props, context);
        this._availableSamples = undefined;
        this.state = {
            samples: undefined,
        };
        this.searchInput = undefined;
        this.onSearchQueryEdit = _.debounce(() => {
            if (this.searchInput && this._availableSamples) {
                const searchQuery = this.searchInput.inputRef.value.toLowerCase();
                let samples = _.cloneDeep(this._availableSamples);
                samples = samples.filter((sampleCategory) => {
                    if (!sampleCategory.title.toLowerCase().includes(searchQuery)) {
                        sampleCategory.samples = sampleCategory.samples.filter(sample => sample.name.toLowerCase().includes(searchQuery));
                    }
                    return sampleCategory.samples.length !== 0;
                });
                this.setState({
                    samples,
                });
            }
        }, 500).bind(this);
    }

    componentDidMount() {
        if (this.searchInput) {
            this.searchInput.focus();
        }
        this.props.getSamples().then((samples) => {
            this._availableSamples = samples;
            this.setState({
                samples,
            });
        });
    }

    componentWillReceiveProps(nextProps) {
        this.props.getSamples().then((samples) => {
            this._availableSamples = samples;
            this.setState({
                samples,
            });
        });
    }

    getColumnContents() {
        const columns = [];
        this.state.samples.forEach((sample) => {
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
        // const columnSize = columns.length > 0 ? (16 / columns.length) : 16;
        return (
            <Grid className='welcome-page'>
                <Grid.Row className='welcome-navbar' columns={2}>
                    <Grid.Column className='nav-tagline'>
                        Search and open available examples
                    </Grid.Column>
                    <Grid.Column>
                        <div className='top-nav-links' style={{ paddingRight: 0, marginRight: 40 }} position='right'>
                            <Input
                                ref={(ref) => {
                                    this.searchInput = ref;
                                }}
                                loading={!this.state.samples}
                                placeholder='Search'
                                onChange={this.onSearchQueryEdit}
                                
                            />
                        </div>
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row className='welcome-content-wrapper'>
                    <Grid.Column mobile={16} tablet={16} computer={16} className='rightContainer'>
                        <Grid>
                            {this.state.samples &&
                                <Grid.Row columns={4} className='sample-wrapper'>
                                    {
                                        this.getColumnContents().map((column) => {
                                            return (
                                                <Grid.Column mobile={16} tablet={8} computer={4}>
                                                    {column.map(columnItem => this.renderColumnItem(columnItem))}
                                                </Grid.Column>
                                            );
                                        })
                                    }
                                </Grid.Row>
                            }
                        </Grid>
                    </Grid.Column>
                </Grid.Row>
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
    getSamples: PropTypes.func.isRequired,
    openSample: PropTypes.func.isRequired,
    openLink: PropTypes.func.isRequired,
};

export default SamplesList;
