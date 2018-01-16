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
import TreeUtil from '../model/tree-util';
import ConnectorAction from '../env/connector-action';

// Use your imagination to render suggestions.
const renderSuggestion = suggestion => (
    <div>
        {suggestion.packageName.split(/[.]+/).pop()}
            -&gt;
        <strong>{suggestion.action.getName()}</strong>
    </div>
);
/**
 * Interaction lifeline button component
 */
class LifelineButton extends React.Component {

    constructor() {
        super();
        this.state = {
            listConnectors: false,
            listActions: false,
            selectedConnecter: '',
            selectedEndpoint: '',
            value: '',
            suggestions: [],
        };
        this.showConnectors = this.showConnectors.bind(this);
        this.hideConnectors = this.hideConnectors.bind(this);
        this.showActions = this.showActions.bind(this);
        this.hideActions = this.hideActions.bind(this);
        /* this.onChange = this.onChange.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this); */

        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.storeInputReference = this.storeInputReference.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.createAction = this.createAction.bind(this);
    }

    getSuggestionValue(suggestion) {
        return this.state.value;
    }


    // Autosuggest will call this function every time you need to clear suggestions.
    onSuggestionsClearRequested() {
        this.setState({
            suggestions: [],
        });
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
                if (this.state.selectedConnecter === conName) {
                    const actions = connector.getActions();
                    actions.forEach((action) => {
                        const actionName = action.getName();
                        // do the match
                        if (value === ''
                            || actionName.toLowerCase().includes(value)) {
                            suggestions.push({
                                pkg,
                                action,
                                connector,
                                packageName: pkg.getName(),
                                fullPackageName: pkg.getName(),
                            });
                        }
                    });
                }
            });
        });

        this.setState({
            suggestions,
        });
    }

    componentDidMount() {
        if (this.input) {
            this.input.focus();
        }
    }

    onChange(event, { newValue, method }) {
        this.setState({
            value: newValue,
        });
    }

    storeInputReference(autosuggest) {
        if (autosuggest !== null) {
            this.input = autosuggest.input;
        }
    }

    showConnectors(connectorName, endpointName) {
        this.setState({
            listConnectors: true,
            listActions: false,
            selectedConnecter: connectorName,
            selectedEndpoint: endpointName,
        });
    }

    hideConnectors() {
        this.setState({ listConnectors: false, listActions: true });
    }

    showActions() {
        this.setState({ listActions: true, listConnectors: false });
    }

    hideActions() {
        this.setState({ listActions: false, listConnectors: false });
    }

    onSuggestionSelected(event, item) {
        item.suggestion.endpoint = this.state.selectedEndpoint;
        this.setState({ listConnectors: false, listActions: false, selectedConnecter: '', selectedEndpoint: '' });
        const node = DefaultNodeFactory.createConnectorActionInvocationAssignmentStatement(item.suggestion);
        this.props.model.acceptDrop(node);
    }


    createAction() {
        const actionNode = DefaultNodeFactory.createConnectorAction();
        actionNode.name.setValue(this.state.value);
        this.props.model.getRoot().topLevelNodes.forEach((topLevelNode) => {
            if (TreeUtil.isConnector(topLevelNode) &&
                topLevelNode.getName().getValue() === this.state.selectedConnecter) {
                topLevelNode.addActions(actionNode);
            }
        });
        const currentPackage = this.context.editor.environment.getCurrentPackage();
        const currentConnector = currentPackage.getConnectorByName(this.state.selectedConnecter);
        const node = DefaultNodeFactory.createConnectorActionInvocationAssignmentStatement({
            pkg: currentPackage,
            action: new ConnectorAction(
                {
                    name: this.state.value,
                    id: '',
                    action: this.state.value,
                    parameters: [],
                    returnParams: [],
                }),
            connector: currentConnector,
            endpoint: this.state.selectedEndpoint,
            packageName: currentPackage.getName(),
            fullPackageName: currentPackage.getName(),
        });
        this.props.model.acceptDrop(node);
        this.setState({ listConnectors: false, listActions: false, selectedConnecter: '', selectedEndpoint: '' });
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const { value, suggestions } = this.state;

        const inputProps = {
            placeholder: 'Select Action',
            value,
            onChange: this.onChange,
        };

        const currentEndpoints = this.props.model.getStatements()
        .filter(stmt => TreeUtil.isVariableDef(stmt) && TreeUtil.isEndpointType(stmt.getVariable().getTypeNode()));

        return (
            <Area bBox={this.props.bBox}>
                <Button
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    buttonRadius={8}
                    menuOverButton
                >
                    <Menu>
                        { !this.state.listConnectors && !this.state.listActions &&
                        <div>
                            {currentEndpoints.length > 0 &&
                            <Item
                                label='Action'
                                icon='fw fw-action'
                                callback={this.showActions}
                            />
                            }
                            {this.props.items}
                        </div>
                        }
                        {this.state.listActions && !this.state.listConnectors &&
                        <div>
                            <div className='endpoint-select-header'>
                                <div
                                    className='connector-select-close'
                                >
                                    <i onClick={this.hideActions} className='nav-button fw fw-left' />Select an endpoint
                                </div>
                            </div>
                            {
                            currentEndpoints.map((statement) => {
                                return (<Item
                                    label={statement.getVariable().getName().getValue()}
                                    icon='fw fw-endpoint'
                                    callback={() => this.showConnectors(
                                        statement.getVariable().getTypeNode().getConstraint().getTypeName().getValue()
                                        , statement.getVariable().getName().getValue())}
                                />);
                            })
                            }
                        </div>
                        }
                        { this.state.listConnectors && !this.state.listActions &&
                            <div
                                className='connector-select'
                                // onMouseOut={this.hideConnectors}
                            >
                                <div
                                    className='connector-select-close'
                                    onClick={this.hideConnectors}
                                >
                                    <i className='fw fw-left' />
                                </div>
                                <Autosuggest
                                    suggestions={suggestions}
                                    onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                                    onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                                    onSuggestionSelected={this.onSuggestionSelected}
                                    getSuggestionValue={this.getSuggestionValue}
                                    renderSuggestion={renderSuggestion}
                                    alwaysRenderSuggestions
                                    inputProps={inputProps}
                                    ref={this.storeInputReference}
                                />
                                {this.state.value !== '' &&
                                <div className='add-new-connector-area'>
                                    <a className='add-new-connector-button' onClick={this.createAction}>
                                        <i className='fw fw-action' />
                                        {' Create new action "'}
                                        <b>{this.state.value + '"'}</b>
                                    </a>
                                </div>
                                }
                            </div>
                        }
                    </Menu>
                </Button>
            </Area>
        );
    }
}

LifelineButton.propTypes = {

};

LifelineButton.defaultProps = {

};

LifelineButton.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default LifelineButton;
