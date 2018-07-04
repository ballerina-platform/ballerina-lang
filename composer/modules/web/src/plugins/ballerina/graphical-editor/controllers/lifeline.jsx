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
import ControllerUtil from 'plugins/ballerina/diagram/views/default/components/controllers/controller-util';
import TreeUtil from 'plugins/ballerina/model/tree-util';
import LifelineTools from 'plugins/ballerina/tool-palette/item-provider/lifeline-tools';
import TreeBuilder from 'plugins/ballerina/model/tree-builder';
import DefaultNodeFactory from 'plugins/ballerina/model/default-node-factory';
import FragmentUtils from 'plugins/ballerina/utils/fragment-utils';
import HoverButton from '../controller-utils/hover-button';
import Item from '../controller-utils/item';
import Search from '../controller-utils/search';

class DefaultCtrl extends React.Component {
    render() {
        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();
        const items = ControllerUtil.convertToAddItems(WorkerTools, model.getBody());
        const top = bBox.y + bBox.h + 15;
        const left = bBox.x;
        if (TreeUtil.isInitFunction(model)) {
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


class RightCtrl extends React.Component {
    constructor() {
        super();
        this.onSuggestionSelected = this.onSuggestionSelected.bind(this);
        this.getSuggestionValue = this.getSuggestionValue.bind(this);
        this.showEndpoints = this.showEndpoints.bind(this);
        this.hideEndpoints = this.hideEndpoints.bind(this);
        this.storeInputReference = this.storeInputReference.bind(this);
        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.state = {
            listEndpoints: false,
            value: '',
            suggestions: [],
        };
        this.getAllSuggestions = this.getAllSuggestions.bind(this);
    }

    // Autosuggest will call this function every time you need to update suggestions.
    // You already implemented this logic above, so just use it.
    onSuggestionsFetchRequested({ value }) {
        const environment = this.context.editor.environment;
        const packages = environment.getFilteredPackages([]);
        const suggestionsMap = {};
        packages.forEach((pkg) => {
            const pkgname = pkg.getName();
            const endpoints = pkg.getEndpoints();

            endpoints.forEach((endpoint) => {
                const conName = endpoint.getName();
                // do the match
                if (value === ''
                    || pkgname.toLowerCase().includes(value)
                    || conName.toLowerCase().includes(value)) {
                    const key = `${pkg.getName()}-${conName}`;
                    suggestionsMap[key] = {
                        pkg,
                        endpoint,
                        packageName: pkg.getName(),
                        fullPackageName: pkg.getName(),
                    };
                }
            });
        });

        const suggestions = _.values(suggestionsMap);

        if (value !== '') {
            suggestions.push({ addNewValue: true });
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
        if (suggestion.addNewValue) {
            return (
                <span />
            );
        }
        return (<div className='endpoint-item'>
            <div className='pkg-name'>{suggestion.pkg.getName()}</div>
            {suggestion.endpoint.getName()}
        </div>);
    }

    onSuggestionSelected(event, item) {
        if (item.suggestion.addNewValue) {
            this.createEndpoint(item.suggestionValue);
        } else {
            const existingImports = this.context.astRoot.getImports();
            if (_.isArray(existingImports)) {
                try {
                    const orgName = item.suggestion.pkg.getOrg();
                    const pkgName = item.suggestion.pkg.getName();
                    const importFound = existingImports.find((importNode) => {
                        return importNode.orgName.value === orgName
                            && importNode.packageName[0].value === pkgName; // TODO: improve to support multipart pkgNames
                    });
                    if (!importFound) {
                        const importNodeCode = `\nimport ${orgName}/${pkgName};`;
                        const fragment = FragmentUtils.createTopLevelNodeFragment(importNodeCode);
                        const newImportNode = FragmentUtils.parseFragment(fragment);
                        this.context.astRoot.addImport(TreeBuilder.build(newImportNode));
                    }
                } catch (err) {
                    log.error('Error while adding import', err);
                }
            }
            const node = DefaultNodeFactory.createEndpoint(item.suggestion);
            this.props.model.acceptDrop(node);
        }
    }

    getAllSuggestions() {
        const environment = this.context.editor.environment;
        const packages = environment.getFilteredPackages([]);
        const suggestionsMap = {};
        packages.forEach((pkg) => {
            const pkgname = pkg.getName();
            const endpoints = pkg.getEndpoints();
            endpoints.forEach((endpoint) => {
                const key = `${pkgname}-${endpoint.getName()}`;
                suggestionsMap[key] = {
                    pkg,
                    endpoint,
                    packageName: pkgname,
                    fullPackageName: pkgname,
                };
            });
        });
        const suggestions = _.values(suggestionsMap);
        return suggestions;
    }

    getSuggestionValue(suggestion) {
        return this.state.value;
    }

    showEndpoints() {
        this.setState({ listEndpoints: true, suggestions: this.getAllSuggestions() });
    }

    hideEndpoints() {
        this.setState({ listEndpoints: false });
    }
    


    render() {
        
        const { model } = this.props;
        const { viewState: { bBox } } = model.getBody();
        const items = ControllerUtil.convertToAddItems(LifelineTools, model);
        // const top = model.viewState.components.defaultWorker.y;
        // const left = bBox.x + bBox.w;
        const node = this.props.model;
        const y = node.viewState.components.defaultWorker.y - 20;
        let x = node.viewState.components.defaultWorker.x + node.viewState.components.defaultWorker.w +
            this.context.config.lifeLine.gutter.h;

        if (node.lambda) {
            return null;
        }

        if (node.workers.length > 0) {
            x = node.workers[node.workers.length - 1].viewState.bBox.x +
                node.workers[node.workers.length - 1].viewState.bBox.w +
                this.context.config.lifeLine.gutter.h;
        }

        if (node.endpointNodes.length > 0) {
            node.endpointNodes.forEach((endpointNode) => {
                x += endpointNode.viewState.bBox.w + this.context.config.lifeLine.gutter.h;
            });
        }

        if (node.viewState.collapsed
            || (!node.viewState.collapsed && TreeUtil.isResource(node) && node.parent.viewState.collapsed)
            || (TreeUtil.isFunction(node) && node.getName().getValue() === 'new')) {
            return null;
        }
        // Set the size of the connector declarations
        const statements = node.body.statements;
        if (statements instanceof Array) {
            statements.forEach((statement) => {
                if (TreeUtil.isEndpointTypeVariableDef(statement)) {
                    x = statement.viewState.bBox.w + statement.viewState.bBox.x + this.context.config.lifeLine.gutter.h;
                }
            });
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
    editor: PropTypes.instanceOf(Object).isRequired,
    astRoot: PropTypes.instanceOf(Object).isRequired,
};

export default {
    defaults: [
        DefaultCtrl,
        RightCtrl,
    ],
    name: 'Lifeline',
};
