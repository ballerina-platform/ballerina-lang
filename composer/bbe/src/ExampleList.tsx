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
import * as React from 'react';
import { Grid, Input } from 'semantic-ui-react';
import { cloneDeep, debounce } from 'lodash';
import { BallerinaExampleCategory } from './model';

export interface SamplesListState {
    samples? : Array<BallerinaExampleCategory>;
    searchQuery? : string;
}

export interface SamplesListProps {
    openSample: (url: string) => void;
    getSamples: () => Promise<Array<BallerinaExampleCategory>>;
}

/**
 * React component for rendering a list of Ballerina examples.
 *
 * @class SamplesList
 * @extends {Component}
 */
export class SamplesList extends React.Component<SamplesListProps, SamplesListState> {

    private _availableSamples: undefined | Array<BallerinaExampleCategory>;
    private searchInput: null | Input;
    private onSearchQueryEdit: () => void;

    constructor(props: SamplesListProps, context: SamplesListState) {
        super(props, context);
        this.onSearchQueryEdit = debounce(() => {
            const { searchQuery } = this.state;
            if (searchQuery != undefined && this._availableSamples) {
                let samples = cloneDeep(this._availableSamples);
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
        this.focusOnSearchInput();
        this.props.getSamples().then((samples) => {
            this._availableSamples = samples;
            this.setState({
                samples,
            });
        });
    }

    componentWillReceiveProps(nextProps: SamplesListProps) {
        this.props.getSamples().then((samples) => {
            this._availableSamples = samples;
            this.setState({
                samples,
            });
        });
    }

    focusOnSearchInput() {
        if (this.searchInput) {
            this.searchInput.focus();
        }
    }

    getColumnContents() {

        const columns: Array<Array<BallerinaExampleCategory>> = [];
        const { samples } = this.state;
        if (samples) {
            samples.forEach((sample: BallerinaExampleCategory) => {
                columns[sample.column] = columns[sample.column] || [];
                columns[sample.column].push(sample);
            });
        }
        return columns;
    }

    renderColumnItem(column: BallerinaExampleCategory) {
        return (
            <ul key={column.title}>
                <li className='title'>{column.title}</li>
                <ul>
                    {
                        column.samples.map((sample) => {
                            return (
                            <li className='list-item' key={sample.url}>
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

    public render() {
        return (
            <Grid className='welcome-page'>
                <Grid.Row className='welcome-navbar' columns={2}>
                    <Grid.Column className='nav-tagline'>
                        Search and open available examples
                    </Grid.Column>
                    <Grid.Column>
                        <div className='top-nav-links' style={{ paddingRight: 0, marginRight: 40 }}>
                            <Input
                                ref={(ref) => {
                                    this.searchInput = ref;
                                }}
                                loading={!this.state || !this.state.samples}
                                placeholder='Search'
                                onChange={(event: React.SyntheticEvent<HTMLInputElement>) => {
                                    this.setState({
                                        searchQuery: event.currentTarget.value
                                    });
                                    this.onSearchQueryEdit();
                                }}
                            />
                        </div>
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row className='welcome-content-wrapper'>
                    <Grid.Column mobile={16} tablet={16} computer={16} className='rightContainer'>
                        <Grid>
                            {this.state && this.state.samples &&
                                <Grid.Row columns={4} className='sample-wrapper'>
                                    {
                                        this.getColumnContents().map((column, index) => {
                                            return (
                                                <Grid.Column key={index} mobile={16} tablet={8} computer={4}>
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
