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
import TreeView from 'react-treeview';
import TreeUtil from 'plugins/ballerina/model/tree-util';

/**
 * Model Renderer
 */
class ModelRenderer extends React.Component {
    constructor() {
        super();
        this.getNodeClassName = this.getNodeClassName.bind(this);
    }
    getNodeClassName(node) {
        let className = 'node';
        const { scrollTop, clientHeight } = this.props.scrollPosition;
        const { y, h } = node.viewState.bBox;
        if (scrollTop >= y - (clientHeight / 2) && scrollTop <= (y + h) - (clientHeight / 2)) {
            className = `${className} selected`;
        }
        return className;
    }
    goToNode(node) {
        this.props.goToNode(node);
    }
    renderService(node) {
        return (
            <div className={this.getNodeClassName(node)} key={node.name.value}>
                <TreeView
                    key={node.name.value}
                    nodeLabel={
                        <span className={this.getNodeClassName(node)}>
                            <span onClick={() => this.goToNode(node)}><i className='fw fw-service' />
                                {node.name.value}
                            </span>
                        </span>
                    }
                >
                    {
                        node.resources.map((resource, i) => {
                            return this.renderResource(resource);
                        })
                    }
                </TreeView>
            </div>
        );
    }
    renderResource(node) {
        return (
            <div className={this.getNodeClassName(node)} key={node.name.value} onClick={() => this.goToNode(node)}>
                <span><i className='fw fw-resource' /> {node.name.value} </span>
            </div>
        );
    }
    renderFunction(node) {
        return (
            <div
                className={this.getNodeClassName(node)}
                key={node.name.value}
                onClick={() => this.goToNode(node)}
            >
                <i className='fw fw-function' />{node.name.value}
            </div>
        );
    }

    renderTransformer(node) {
        let transformerName = node.name.value;
        if (!transformerName) {
            transformerName = node.getSignature();
        }
        return (<div className={this.getNodeClassName(node)} key={transformerName} onClick={() => this.goToNode(node)}>
            <i className='fw fw-transformer' /> {transformerName}
        </div>);
    }

    renderDefaultNode(node) {
        if (!node || !node.name || !node.name.value) {
            return null;
        }
        return (
            <div
                className={this.getNodeClassName(node)}
                key={node.name.value}
                onClick={() => this.goToNode(node)}
            >
                <i className={`fw fw-${node.kind.toLowerCase()}`} />
                {node.name.value}
            </div>
        );
    }
    renderTopLevelNode(node) {
        switch (node.kind) {
            case 'Service':
                return this.renderService(node);
            case 'Function':
                return this.renderFunction(node);
            case 'Transformer':
                return this.renderTransformer(node);
            case 'Import':
                return null;
            case 'PackageDeclaration':
                return null;
            default:
                return this.renderDefaultNode(node);
        }
    }
    render() {
        const { model } = this.props;
        if (!model || !model.topLevelNodes || !model.topLevelNodes.length) {
            return null;
        }
        return (
            <div>
                <TreeView
                    key='package'
                    nodeLabel={
                        <span>
                            <i className='fw fw-package' />{TreeUtil.getPackageNameString(model) || model.name}
                        </span>
                    }
                >
                    {
                        model.topLevelNodes.map((node, i) => {
                            return this.renderTopLevelNode(node);
                        })
                    }
                </TreeView>
            </div>
        );
    }
}

ModelRenderer.propTypes = {
    goToNode: PropTypes.func,
    model: PropTypes.objectOf(Object).isRequired,
    scrollPosition: PropTypes.objectOf(Object).isRequired,
};

ModelRenderer.defaultProps = {
    model: {},
    goToNode: () => {},
    scrollPosition: {},
};

export default ModelRenderer;
