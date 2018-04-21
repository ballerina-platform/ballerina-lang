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
import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import Area from './area';
import Button from './button';
import Menu from './menu';
import Item from './item';
import Search from './search';
import DefaultNodeFactory from '../model/default-node-factory';
import TreeUtil from '../model/tree-util';
import ConnectorAction from '../env/connector-action';

// Use your imagination to render suggestions.
const renderSuggestion = (suggestion, value) => {
    if (suggestion.addNewValue) {
        return (
            <div className='add-new-connector-area'>
                <a className='add-new-connector-button'>
                    <i className='fw fw-action' />
                    {' Create new action "'}
                    <b>{value.query + '"'}</b>
                </a>
            </div>
        );
    }

    return (<div>
        {suggestion.packageName.split(/[.]+/).pop()}
        -&gt;
        <strong>{suggestion.functionDef.getName()}</strong>
    </div>);
};
/**
 * Interaction lifeline button component
 */
class LifelineButton extends React.Component {

    constructor(props, contex) {
        super();
        this.context = contex;
        this.state = {
            listConnectors: false,
            listActions: false,
            selectedConnecter: '',
            selectedEndpoint: '',
            value: '',
            suggestions: [],
        };
        this.showEndpoints = this.showEndpoints.bind(this);
        this.hideConnectors = this.hideConnectors.bind(this);
        this.showActions = this.showActions.bind(this);
        this.hideActions = this.hideActions.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.storeInputReference = this.storeInputReference.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.createAction = this.createAction.bind(this);
        this.getAllSugestions = this.getAllSugestions.bind(this);
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
                                packageName: pkgname,
                                fullPackageName: pkgname,
                            });
                        }
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
            this.createAction();
        } else {
            item.suggestion.endpoint = this.state.selectedEndpoint;
            this.setState({ listConnectors: false, listActions: false, selectedConnecter: '', selectedEndpoint: '' });
            const node = DefaultNodeFactory.createConnectorActionInvocationAssignmentStatement(item.suggestion);
            this.props.model.acceptDrop(node);
        }
    }

    getAllSugestions(endpointNode) {
        const suggestions = [];
        const environment = this.context.editor.environment;
        const packages = environment.getFilteredPackages([]);
        const endpointTypeName = endpointNode.endPointType.typeName.value;
        let endpointTypePkg = endpointNode.endPointType.packageAlias.getValue();
        endpointTypePkg = (endpointTypePkg === '') ? 'Current Package' : endpointTypePkg;
        const pkg = _.find(packages, (ownerPackage) => (ownerPackage.getName().endsWith(endpointTypePkg)));
        const getClientFunction = _.find(pkg.getFunctionDefinitions(),
            (functionItem) => functionItem._name === 'getClient' && functionItem._receiverType === endpointTypeName);
        const clientStructType = ((getClientFunction)._returnParams[0]).type;
        const structTypeComponents = clientStructType.split(":");
        
        _.filter(pkg.getFunctionDefinitions(), (functionDef) => {
            return functionDef._receiverType === structTypeComponents[structTypeComponents.length - 1];
        }).forEach((functionDef) => {
            const pkgName = structTypeComponents.length > 1 ? structTypeComponents[0] : '';
            suggestions.push({
                pkg,
                functionDef,
                packageName: pkgName,
                fullPackageName: pkgName,
            });
        });
        return suggestions;
    }

    getSuggestionValue(suggestion) {
        return this.state.value;
    }

    showActions() {
        this.setState({ listActions: true, listConnectors: false });
    }

    hideActions() {
        this.setState({ listActions: false, listConnectors: false });
    }

    hideConnectors() {
        this.setState({ listConnectors: false, listActions: true });
    }

    showEndpoints(endpoint) {
        this.setState({
            listConnectors: true,
            listActions: false,
            selectedEndpoint: endpoint.getName().getValue(),
            suggestions: this.getAllSugestions(endpoint),
        });
    }

    storeInputReference(autosuggest) {
        if (autosuggest !== null) {
            this.input = autosuggest.input;
        }
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
            placeholder: 'Search',
            value,
            onChange: this.onChange,
        };
        const currentEndpoints = TreeUtil.getAllEndpoints(this.props.model);
        return (
            <Area bBox={this.props.bBox}>
                <Button
                    buttonX={this.props.button.x}
                    buttonY={this.props.button.y}
                    showAlways={this.props.showAlways}
                    buttonRadius={8}
                    buttonIconColor='#333'
                    menuOverButton
                    type='secondary'
                >
                    <Menu>
                        { !this.state.listConnectors && !this.state.listActions &&
                        <div>
                            {currentEndpoints.length > 0 &&
                            <Item
                                label='Action'
                                icon='fw fw-action'
                                callback={this.showActions}
                                closeMenu={false}
                            />
                            }
                            {this.props.items}
                        </div>
                        }
                        {this.state.listActions && !this.state.listConnectors &&
                        <div>
                            <div className='endpoint-select-header'>
                                <div className='connector-select-close'>
                                    <i onClick={this.hideActions} className='nav-button fw fw-left' />Select an endpoint
                                </div>
                            </div>
                            {
                            currentEndpoints.map((statement) => {
                                return (<Item
                                    label={statement.name.getValue()}
                                    icon='fw fw-endpoint'
                                    callback={() => this.showEndpoints(statement)}
                                    closeMenu={false}
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
                                <div className='endpoint-select-header'>
                                    <div className='connector-select-close'>
                                        <i onClick={this.hideConnectors} className='nav-button fw fw-left' />
                                        Select an action
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
    button: { x: 0, y: 0 },
    showAlways: false,
};

LifelineButton.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default LifelineButton;
