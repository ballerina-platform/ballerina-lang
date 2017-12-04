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
import TreeUtil from 'plugins/ballerina/model/tree-util';

/**
 * Model Renderer
 */
class ModelRenderer extends React.Component {
    goToNode(node) {
        this.props.goToNode(node);
    }
    renderService(node) {
        return (
            <li key={node.name.value}>
                <span onClick={() => this.goToNode(node)}><i className='fw fw-service' /> {node.name.value} </span>
                <ul>
                    {
                        node.resources.map((resource, i) => {
                            return this.renderResource(resource);
                        })
                    }
                </ul>
            </li>
        );
    }
    renderResource(node) {
        return (
            <li key={node.name.value} onClick={() => this.goToNode(node)}>
                <span><i className='fw fw-resource' /> {node.name.value} </span>
            </li>
        );
    }
    renderFunction(node) {
        return (<li key={node.name.value} onClick={() => this.goToNode(node)}><i className='fw fw-function' />
            {node.name.value}
        </li>);
    }

    renderTransformer(node) {
        let transformerName = node.name.value;
        if (!transformerName) {
            transformerName = node.getSignature();
        }
        return (<li key={transformerName} onClick={() => this.goToNode(node)}>
            <i className='fw fw-transformer' /> {transformerName}
        </li>);
    }

    renderDefaultNode(node) {
        return (<li key={node.name.value} onClick={() => this.goToNode(node)}><i className={`fw fw-${node.kind.toLowerCase()}`} />
            {node.name.value}
        </li>);
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
                <ul >
                    <li key='package'><i className='fw fw-package' /> {TreeUtil.getPackageNameString(model) || model.name}</li>
                    <ul>
                        {
                            model.topLevelNodes.map((node, i) => {
                                return this.renderTopLevelNode(node);
                            })
                        }
                    </ul>
                </ul>
            </div>
        );
    }
}

ModelRenderer.defaultProps = {
    model: {},
    goToNode: () => {},
};

export default ModelRenderer;
