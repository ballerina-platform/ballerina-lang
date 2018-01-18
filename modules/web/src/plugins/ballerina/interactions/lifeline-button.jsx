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
import Autosuggest from 'react-autosuggest';
import Area from './area';
import Button from './button';
import Menu from './menu';
import Item from './item';
import DefaultNodeFactory from '../model/default-node-factory';
import Connector from '../env/connector';

// Use your imagination to render suggestions.
const renderSuggestion = (suggestion, value) => {
    if (suggestion.addNewValue) {
        return (
            <div className='add-new-connector-area'>
                <a className='add-new-connector-button'>
                    <i className='fw fw-connector' />
                    {' Create new connector "'}
                    <b>{value.query + '"'}</b>
                </a>
            </div>
        );
    }
    return (<div>
        <div className='pkg-name'>{suggestion.pkg.getName()}</div>
        {suggestion.connector.getName()}
    </div>);
};
/**
 * Interaction lifeline button component
 */
class LifelineButton extends React.Component {

    constructor() {
        super();
        this.state = {
            listConnectors: false,
            value: '',
            suggestions: [],
        };
        this.showConnectors = this.showConnectors.bind(this);
        this.hideConnectors = this.hideConnectors.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.storeInputReference = this.storeInputReference.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.createConnector = this.createConnector.bind(this);
        this.getAllSuggestions = this.getAllSuggestions.bind(this);
    }

    componentDidMount() {

    }

    // Autosuggest will call this function every time you need to update suggestions.
    // You already implemented this logic above, so just use it.
    onSuggestionsFetchRequested({ value }) {
        const environment = this.context.editor.environment;
        const packages = environment.getFilteredPackages([]);
        const suggestions = [];
        packages.forEach((pkg) => {
            const pkgname = pkg.getName();
            const connectors = pkg.getConnectors();
            connectors.forEach((connector) => {
                const conName = connector.getName();
                // do the match
                if (value === ''
                    || pkgname.toLowerCase().includes(value)
                    || conName.toLowerCase().includes(value)) {
                    suggestions.push({
                        pkg,
                        connector,
                        packageName: pkg.getName(),
                        fullPackageName: pkg.getName(),
                    });
                }
            });
        });

        if (value !== '') {
            suggestions.push({ addNewValue: true });
        }
        this.setState({
            suggestions,
        });
    }

    onChange(event, { newValue, method }) {
        this.setState({
            value: newValue,
        });
    }

    onSuggestionSelected(event, item) {
        if (item.suggestion.addNewValue) {
            this.createConnector();
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
            const connectors = pkg.getConnectors();
            connectors.forEach((connector) => {
                suggestions.push({
                    pkg,
                    connector,
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

    showConnectors() {
        this.setState({ listConnectors: true, suggestions: this.getAllSuggestions() });
    }

    hideConnectors() {
        this.setState({ listConnectors: false });
    }

    createConnector() {
        const connectorNode = DefaultNodeFactory.createConnector();
        connectorNode.name.setValue(this.state.value);
        const currentPackage = this.context.editor.environment.getCurrentPackage();

        const node = DefaultNodeFactory.createEndpoint({
            pkg: currentPackage,
            connector: new Connector({ name: this.state.value, id: '', actions: [], params: [] }),
            packageName: currentPackage.getName(),
            fullPackageName: currentPackage.getName(),
        });
        this.props.model.acceptDrop(node);
        this.props.model.getRoot().addTopLevelNodes(connectorNode);
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const { value, suggestions } = this.state;

        const inputProps = {
            placeholder: 'Search',
            value,
            onChange: this.onChange,
        };

        let connectorCssClass = 'connector-select-hidden';
        let connectorListCssClass = 'connector-list';
        if (this.state.listConnectors) {
            connectorCssClass = 'connector-select';
            connectorListCssClass = 'connector-list-hidden';
        }

        return (
            <Area bBox={this.props.bBox}>
                <Button
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    menuOverButton
                >
                    <Menu>
                        <div className={connectorListCssClass}>
                            {this.props.items}
                            <Item
                            label='Endpoint'
                            icon='fw fw-endpoint'
                            callback={this.showConnectors}
                        />
                        </div>

                        <div
                            className={connectorCssClass}
                        >
                            <div className='endpoint-select-header'>
                                <div className='connector-select-close'>
                                    <i onClick={this.hideConnectors} className='nav-button fw fw-left' />
                                    Select a connector
                                </div>
                            </div>
                            <Autosuggest
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
    bBox: PropTypes.valueOf(PropTypes.object).isRequired,
};

LifelineButton.defaultProps = {

};

LifelineButton.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default LifelineButton;
