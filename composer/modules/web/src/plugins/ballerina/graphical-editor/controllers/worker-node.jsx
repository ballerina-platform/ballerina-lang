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
import _ from 'lodash';
import WorkerTools from 'plugins/ballerina/tool-palette/item-provider/worker-tools';
import ControllerUtil from '../controller-utils/controller-util';
import TreeUtil from 'plugins/ballerina/model/tree-util';
import { getActions } from 'api-client/api-client';
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';
import HoverButton from '../controller-utils/hover-button';
import Item from '../controller-utils/item';
import Search from '../controller-utils/search';
import Toolbox from 'plugins/ballerina/diagram/views/default/components/decorators/action-box';
import { ACTION_BOX_POSITION } from '../../constants';

// Use your imagination to render suggestions.
const renderSuggestion = (suggestion, value) => {
    return (<div className='endpoint-item'>
        {suggestion.actionName}
    </div>);
};

/**
 * Interaction lifeline button component
 */
class DefaultCtrl extends React.Component {

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
    }

    // Autosuggest will call this function every time you need to update suggestions.
    // You already implemented this logic above, so just use it.
    onSuggestionsFetchRequested({ value }) {
        let suggestions = this.state.allSuggestions;
        if (value !== '') {
            suggestions = this.state.allSuggestions.filter((pkg) => {
                if (pkg.actionName.toLowerCase().includes(value)) {
                    return true;
                } else {
                    return false;
                }
            });
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
        item.suggestion.endpoint = this.state.selectedEndpoint;
        this.setState({ listConnectors: false, listActions: false, selectedConnecter: '', selectedEndpoint: '' });
        const node = DefaultNodeFactory.createConnectorActionInvocationAssignmentStatement(item.suggestion);
        this.props.model.getBody().acceptDrop(node);
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
        if (!endpoint) {
            return;
        }
        const type = endpoint.endPointType.typeName.value;
        const pkgname = endpoint.endPointType.packageAlias.value;
        getActions(pkgname, type).then((actions) => {
            const suggestionsMap = {};
            actions.forEach((pkg) => {
                const actionName = pkg.name.split(/[.]+/).pop();
                const key = `${pkgname}-${actionName}`;
                if (actionName === 'new') {
                    return;
                }
                suggestionsMap[key] = {
                    actionName,
                    packageName: pkgname,
                    fullPackageName: pkgname,
                };
            });
            const suggestions = _.values(suggestionsMap);
            this.setState({
                listConnectors: true,
                listActions: false,
                selectedEndpoint: endpoint.getName().getValue(),
                suggestions,
                allSuggestions: suggestions,
            });
        }).catch((e) => {
            console.error('could not retrieve actions');
        });
    }

    storeInputReference(autosuggest) {
        if (autosuggest !== null) {
            this.input = autosuggest.input;
        }
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const { model } = this.props;
        const { value, suggestions } = this.state;
        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const { viewState: { bBox } } = model.getBody();
        const inputProps = {
            placeholder: 'Search',
            value,
            onChange: this.onChange,
        };
        const currentEndpoints = TreeUtil.getAllEndpoints(model);
        const y = bBox.y + bBox.h;
        const x = bBox.x;

        if (model.parent.viewState.collapsed ||
            (model.parent.parent && model.parent.parent.viewState.collapsed)) {
            return null;
        }

        return (
            <HoverButton
                style={{
                    top: y,
                    left: x,
                }}
                menuPosition='bottom left'
            >
                {!this.state.listConnectors && !this.state.listActions &&
                    <div>
                        {currentEndpoints.length > 0 &&
                            <Item
                                label='Action'
                                icon='fw fw-action'
                                callback={this.showActions}
                                closeMenu={false}
                            />
                        }
                        {items}
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
                {this.state.listConnectors && !this.state.listActions &&
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
                        <div className='suggest-list'>
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
                    </div>
                }
            </HoverButton>
        );
    }
}

DefaultCtrl.propTypes = {

};

DefaultCtrl.defaultProps = {
    button: { x: 0, y: 0 },
    showAlways: false,
};

class ActionBox extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();

        const top = bBox.y - 30;
        let left = bBox.x;

        const onDelete = () => { model.remove(); };
        const onJumptoCodeLine = () => {
            const { goToSource } = this.context;
            goToSource(model);
        };

        if (model.name.value === 'default') {
            left += ACTION_BOX_POSITION.SINGLE_ACTION_OFFSET;
            return (
                <Toolbox
                    disableButtons={{ delete: true }}
                    onJumptoCodeLine={onJumptoCodeLine}
                    show
                    style={{
                        top,
                        left,
                    }}
                />
            );
        }

        return (
            <Toolbox
                onDelete={onDelete}
                onJumptoCodeLine={onJumptoCodeLine}
                show
                style={{
                    top,
                    left,
                }}
            />
        );
    }
}

ActionBox.contextTypes = {
    config: PropTypes.instanceOf(Object),
    goToSource: PropTypes.func.isRequired,
    designer: PropTypes.instanceOf(Object),
};

export default {
    defaults: [DefaultCtrl],
    regions: {
        actionBox: ActionBox,
    },
    name: 'Worker',
};
