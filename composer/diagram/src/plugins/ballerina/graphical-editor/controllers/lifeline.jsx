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
import _ from 'lodash';
import log from 'log';
import { Menu } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import WorkerTools from 'plugins/ballerina/tool-palette/item-provider/worker-tools';
import { getEndpoints } from 'api-client/api-client';
import ControllerUtil from '../controller-utils/controller-util';
import TreeUtil from 'plugins/ballerina/model/tree-util';
import LifelineTools from 'plugins/ballerina/tool-palette/item-provider/lifeline-tools';
import TreeBuilder from 'plugins/ballerina/model/tree-builder';
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';
import FragmentUtils from 'plugins/ballerina/utils/fragment-utils';
import HoverButton from '../controller-utils/hover-button';
import Item from '../controller-utils/item';
import Search from '../controller-utils/search';
import Toolbox from 'plugins/ballerina/diagram/views/default/components/decorators/action-box';
import { ACTION_BOX_POSITION } from '../../constants';

class DefaultCtrl extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();

        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const top = bBox.y + bBox.h + 15;
        const left = bBox.x;
        if (TreeUtil.isInitFunction(model) || model.workers.length > 0 || model.lambda) {
            return null;
        }

        if (model.viewState.collapsed
            || (!model.viewState.collapsed && TreeUtil.isResource(model) && model.parent.viewState.collapsed)
            || (TreeUtil.isFunction(model) && model.name.getValue() === 'new')) {
            return null;
        }
        return (
            <HoverButton
                style={{
                    top,
                    left,
                }}
            >
                <Menu vertical>
                    {items}
                </Menu>
            </HoverButton>
        );
    }
}

DefaultCtrl.contextTypes = {
    designer: PropTypes.instanceOf(Object),
    config: PropTypes.instanceOf(Object),
};

function calculateLifelineX(node, config) {
    let x = node.viewState.components.defaultWorker.x + node.viewState.components.defaultWorker.w +
        config.lifeLine.gutter.h;

    if (node.workers.length > 0) {
        x = node.workers[node.workers.length - 1].viewState.bBox.x +
            node.workers[node.workers.length - 1].viewState.bBox.w +
            config.lifeLine.gutter.h;
    }

    if (node.endpointNodes.length > 0) {
        node.endpointNodes.forEach((endpointNode) => {
            x += endpointNode.viewState.bBox.w + config.lifeLine.gutter.h;
        });
    }


    // Set the size of the connector declarations
    const statements = node.body.statements;
    if (statements instanceof Array) {
        statements.forEach((statement) => {
            if (TreeUtil.isEndpointTypeVariableDef(statement)) {
                x = statement.viewState.bBox.w + statement.viewState.bBox.x + config.lifeLine.gutter.h;
            }
        });
    }
    return x;
}

class RightCtrl extends React.Component {
    constructor() {
        super();
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.showEndpoints = this.showEndpoints.bind(this);
        this.hideEndpoints = this.hideEndpoints.bind(this);
        this.storeInputReference = this.storeInputReference.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onChange = this.onChange.bind(this);
        this.renderSuggestion = this.renderSuggestion.bind(this);
        this.state = {
            listEndpoints: false,
            value: '',
            suggestions: [],
            allSuggestions: [],
        };
    }

    // Autosuggest will call this function every time you need to update suggestions.
    // You already implemented this logic above, so just use it.
    onSuggestionsFetchRequested({ value }) {
        let suggestions = this.state.allSuggestions;
        if (value !== '') {
            suggestions = this.state.allSuggestions.filter((pkg) => {
                if (pkg.endpoint.toLowerCase().includes(value)
                    || pkg.packageName.toLowerCase().includes(value)) {
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
    storeInputReference(autosuggest) {
        if (autosuggest !== null) {
            this.input = autosuggest.input;
        }
    }
    renderSuggestion(suggestion, value) {
        return (<div className='endpoint-item'>
            <div className='pkg-name'>{suggestion.packageName}</div>
            {suggestion.endpoint}
        </div>);
    }
    onSuggestionSelected(event, item) {
        
        const existingImports = this.context.astRoot.getImports();
        if (_.isArray(existingImports)) {
            try {
                const orgName = item.suggestion.orgName;
                const pkgName = item.suggestion.packageName;
                const importFound = existingImports.find((importNode) => {
                    return importNode.orgName.value === orgName
                        && importNode.packageName[0].value === pkgName; // TODO: improve to support multipart pkgNames
                });
                if (!importFound && `${orgName}/${pkgName}` !== 'ballerina/builtin') {
                    const importNodeCode = `\nimport ${orgName}/${pkgName};`;
                    const fragment = FragmentUtils.createTopLevelNodeFragment(importNodeCode);
                    FragmentUtils.parseFragment(fragment)
                        .then((newImportNode) => {
                            this.context.astRoot.addImport(TreeBuilder.build(newImportNode));
                        });
                }
            } catch (err) {
                log.error('Error while adding import', err);
            }
        }
        DefaultNodeFactory.createEndpoint(item.suggestion).then(node => this.props.model.acceptDrop(node));
    }

    getSuggestionValue(suggestion) {
        return this.state.value;
    }

    showEndpoints() {
        // give priority to getEndpoints method
        // provided via context
        const getEPs = this.context.getEndpoints
                        ? this.context.getEndpoints
                        : getEndpoints;
        getEPs().then((endpoints) => {
            const suggestionsMap = {};
            endpoints.forEach((ep) => {
                const pkgname = ep.packageName;
                const endpoint = ep.name;
                if (endpoint.includes('Listener')) {
                    return;
                }
                const key = `${pkgname}-${endpoint}`;
                suggestionsMap[key] = {
                    endpoint,
                    packageName: pkgname,
                    fullPackageName: pkgname,
                    orgName: ep.orgName,
                };
            });
            const suggestions = _.values(suggestionsMap);
            this.setState({
                suggestions,
                allSuggestions: suggestions,
                listEndpoints: true,
            });
        }).catch((e) => {
            console.error('could not retrieve endpoints');
        });
    }

    onChange(event, { newValue, method }) {
        this.setState({
            value: newValue,
        });
    }

    hideEndpoints() {
        this.setState({ listEndpoints: false });
    }

    render() {

        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();
        const items = ControllerUtil.convertToAddItems(LifelineTools, model);
        const node = this.props.model;
        const y = node.viewState.components.defaultWorker.y - 20;
        const x = calculateLifelineX(node, this.context.config);

        if (node.lambda) {
            return null;
        }

        if (node.viewState.collapsed
            || (!node.viewState.collapsed && TreeUtil.isResource(node) && node.parent.viewState.collapsed)
            || (TreeUtil.isFunction(node) && node.name.getValue() === 'new')) {
            return null;
        }

        if (TreeUtil.isObject(node.parent)) {
            return null;
        }

        if (TreeUtil.isInitFunction(node)) {
            return null;
        }

        const { value, suggestions } = this.state;

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
            <HoverButton
                style={{
                    top: y,
                    left: x,
                }}
                menuPosition='bottom left'
            >
                <div className={endpointListCssClass}>
                    {items}
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
                            <i onClick={this.hideEndpoints} className='nav-button fw fw-left' />
                            Select an endpoint
                            </div>
                    </div>
                    <div className='suggest-list'>
                        <Search
                            suggestions={suggestions}
                            onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                            onSuggestionSelected={this.onSuggestionSelected}
                            getSuggestionValue={this.getSuggestionValue}
                            renderSuggestion={this.renderSuggestion}
                            alwaysRenderSuggestions
                            inputProps={inputProps}
                            ref={this.storeInputReference}
                        />
                    </div>
                </div>
            </HoverButton>
        );
    }
}

RightCtrl.contextTypes = {
    designer: PropTypes.instanceOf(Object),
    config: PropTypes.instanceOf(Object),
    astRoot: PropTypes.instanceOf(Object).isRequired,
    getEndpoints: PropTypes.func,
};

class ActionBox extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();

        const top = model.viewState.components.defaultWorker.y + 20;
        const left = bBox.x + ACTION_BOX_POSITION.SINGLE_ACTION_OFFSET;

        const onDelete = () => { model.remove(); };
        const onJumptoCodeLine = () => {
            const { goToSource } = this.context;
            goToSource(model);
        };

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
}

ActionBox.contextTypes = {
    config: PropTypes.instanceOf(Object),
    goToSource: PropTypes.func.isRequired,
    designer: PropTypes.instanceOf(Object),
};

export default {
    regions: {
        actionBox: ActionBox,
    },
    defaults: [
        DefaultCtrl,
        RightCtrl,
    ],
    name: 'Lifeline',
};
