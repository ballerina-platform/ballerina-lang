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
import ModelRenderer from './ModelRenderer';
import './CodeExplorer.scss';

/**
 * Code Explorer
 */
class CodeExplorerPanel extends React.Component {
    constructor(props) {
        super(props);
        this.state = { ast: {} };
        this.props.codeExplorerPlugin.appContext.command.on('ACTIVE_AST_CHANGED', (ast) => {
            this.setState({
                ast,
            });
        });
    }

    render() {
        const { ast } = this.state;
        if (ast.parseFailed) {
            return 'Parse failed';
        }
        return (
            <div className='code-explorer'>
                <ModelRenderer
                    model={ast.model}
                    goToNode={node => this.props.codeExplorerPlugin.appContext.command.dispatch('go-to-node', node)}
                />
            </div>
        );
    }
}

export default CodeExplorerPanel;
