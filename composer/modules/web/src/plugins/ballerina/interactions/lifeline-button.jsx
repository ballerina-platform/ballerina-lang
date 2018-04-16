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
import Area from './area';
import Button from './button';
import Menu from './menu';
import Item from './item';
import Search from './search';
import DefaultNodeFactory from '../model/default-node-factory';
import Endpoint from "../env/endpoint";

// Use your imagination to render suggestions.
const renderSuggestion = (suggestion, value) => {
    if (suggestion.addNewValue) {
        return (
            <div className='add-new-connector-area'>
                <a className='add-new-connector-button'>
                    <i className='fw fw-connector'/>
                    {' Create new endpoint "'}
                    <b>{value.query + '"'}</b>
                </a>
            </div>
        );
    }
    return (<div>
        <div className='pkg-name'>{suggestion.pkg.getName()}</div>
        {suggestion.endpoint.getName()}
    </div>);
};

/**
 * Interaction lifeline button component
 */
class LifelineButton extends React.Component {

    constructor() {
        super();
        this.state = {
            listEndpoints: false,
            value: '',
            suggestions: [],
        };
        this.showEndpoints = this.showEndpoints.bind(this);
        this.hideEndpoints = this.hideEndpoints.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.storeInputReference = this.storeInputReference.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.createEndpoint = this.createEndpoint.bind(this);
        this.getAllSuggestions = this.getAllSuggestions.bind(this);
    }

    // Autosuggest will call this function every time you need to update suggestions.
    // You already implemented this logic above, so just use it.
    onSuggestionsFetchRequested({value}) {
        const environment = this.context.editor.environment;
        const packages = environment.getFilteredPackages([]);
        const suggestions = [];
        packages.forEach((pkg) => {
            const pkgname = pkg.getName();
            const endpoints = pkg.getEndpoints();
            endpoints.forEach((endpoint) => {
                const conName = endpoint.getName();
                // do the match
                if (value === ''
                    || pkgname.toLowerCase().includes(value)
                    || conName.toLowerCase().includes(value)) {
                    suggestions.push({
                        pkg,
                        endpoint,
                        packageName: pkg.getName(),
                        fullPackageName: pkg.getName(),
                    });
                }
            });
        });

        if (value !== '') {
            suggestions.push({addNewValue: true});
        }
        this.setState({
            suggestions,
        });
    }

    onChange(event, {newValue, method}) {
        this.setState({
            value: newValue,
        });
    }

    onSuggestionSelected(event, item) {
        if (item.suggestion.addNewValue) {
            this.createEndpoint();
        } else {
            const node = DefaultNodeFactory.createEndpoint(item.suggestion);
            this.props.model.acceptDrop(node);
        }
    }

    getAllSuggestions() {
        const environment = this.context.editor.environment;
        const packages = environment.getFilteredPackages([]);
        const suggestions = [];
        packages.forEach((pkg) => {
            const pkgname = pkg.getName();
            const endpoints = pkg.getEndpoints();
            endpoints.forEach((endpoint) => {
                suggestions.push({
                    pkg,
                    endpoint,
                    packageName: pkgname,
                    fullPackageName: pkgname,
                });
            });
        });
        return suggestions;
    }

    getSuggestionValue(suggestion) {
        return this.state.value;
    }


    storeInputReference(autosuggest) {
        if (autosuggest !== null) {
            this.input = autosuggest.input;
        }
    }

    showEndpoints() {
        this.setState({listEndpoints: true, suggestions: this.getAllSuggestions()});
    }

    hideEndpoints() {
        this.setState({listEndpoints: false});
    }

    createEndpoint() {
        const endpointNode = DefaultNodeFactory.createEndpoint();
        endpointNode.name.setValue(this.state.value);
        const currentPackage = this.context.editor.environment.getCurrentPackage();

        const node = DefaultNodeFactory.createEndpoint({
            pkg: currentPackage,
            endpoint: new Endpoint({
                name: this.state.value, id: '', actions: [], fields: [],
                packageName: currentPackage.name
            }),
            packageName: currentPackage.getName(),
            fullPackageName: currentPackage.getName(),
        });
        this.props.model.acceptDrop(node);
        this.props.model.getRoot().addTopLevelNodes(endpointNode);
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const {value, suggestions} = this.state;

        const inputProps = {
            placeholder: 'Search',
            value,
            onChange: this.onChange,
        };

        let endpointCssClass = 'connector-select-hidden';
        let endpointListCssClass = 'connector-list';
        if (this.state.listEndpoints) {
            endpointCssClass = 'connector-select';
            endpointListCssClass = 'connector-list-hidden';
        }

        return (
            <Area bBox={this.props.bBox}>
                <Button
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    menuOverButton
                    type='secondary'
                    buttonIconColor='#333'
                >
                    <Menu>
                        <div className={endpointListCssClass}>
                            {this.props.items}
                            <Item
                                label='Endpoint'
                                icon='fw fw-endpoint'
                                callback={this.showEndpoints}
                                closeMenu={false}
                            />
                        </div>

                        <div
                            className={endpointCssClass}
                        >
                            <div className='endpoint-select-header'>
                                <div className='connector-select-close'>
                                    <i onClick={this.hideEndpoints} className='nav-button fw fw-left'/>
                                    Select an endpoint
                                </div>
                            </div>
                            <Search
                                suggestions={suggestions}
                                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                                onSuggestionSelected={this.onSuggestionSelected}
                                getSuggestionValue={this.getSuggestionValue}
                                renderSuggestion={renderSuggestion}
                                alwaysRenderSuggestions
                                inputProps={inputProps}
                                ref={this.storeInputReference}
                            />
                        </div>
                    </Menu>
                </Button>
            </Area>
        );
    }
}

LifelineButton.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }).isRequired,
};

LifelineButton.defaultProps = {};

LifelineButton.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default LifelineButton;
