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
import { EVENTS as BAL_PLUGIN_EVENTS } from 'plugins/ballerina/constants';
import { EVENTS as EDITOR_EVENTS } from 'core/editor/constants';
import Editor from 'core/editor/model/Editor';
import ModelRenderer from './ModelRenderer';
import './CodeExplorer.scss';
import ErrorBoundary from 'core/editor/views/ErrorBoundary';

/**
 * Code Explorer
 */
class CodeExplorerPanel extends React.Component {
    constructor(props) {
        super(props);
        const editor = this.props.codeExplorerPlugin.appContext.editor.getActiveEditor();
        const ast = editor && editor instanceof Editor ? editor.getProperty('ast') : {};
        this.state = { ast: ast || {} };
        this.onActiveBalASTChange = this.onActiveBalASTChange.bind(this);
        this.onTabChange = this.onTabChange.bind(this);
        this.onScrollDesignView = this.onScrollDesignView.bind(this);
    }

    componentDidMount() {
        const { command: { on } } = this.props.codeExplorerPlugin.appContext;
        on(BAL_PLUGIN_EVENTS.ACTIVE_BAL_AST_CHANGED, this.onActiveBalASTChange);
        on(EDITOR_EVENTS.ACTIVE_TAB_CHANGE, this.onTabChange);
        on(BAL_PLUGIN_EVENTS.SCROLL_DESIGN_VIEW, this.onScrollDesignView);
    }

    componentWillUnmount() {
        const { command: { off } } = this.props.codeExplorerPlugin.appContext;
        off(BAL_PLUGIN_EVENTS.ACTIVE_BAL_AST_CHANGED, this.onActiveBalASTChange);
        off(EDITOR_EVENTS.ACTIVE_TAB_CHANGE, this.onTabChange);
        off(BAL_PLUGIN_EVENTS.SCROLL_DESIGN_VIEW, this.onScrollDesignView);
    }

    onScrollDesignView({ scrollTop, scrollLeft, scrollHeight, scrollWidth, clientHeight, clientWidth }) {
        this.setState({
            scrollPosition: {
                scrollTop,
                scrollLeft,
                scrollHeight,
                scrollWidth,
                clientHeight,
                clientWidth,
            },
        });
    }

    onTabChange({ editor }) {
        const ast = editor && editor instanceof Editor ? editor.getProperty('ast') : {};
        this.setState({
            ast,
        });
        this.forceUpdate();
    }

    onActiveBalASTChange({ ast }) {
        this.setState({
            ast,
        });
        this.forceUpdate();
    }

    render() {
        const { ast } = this.state;
        if (ast && ast.parseFailed) {
            return 'Parse failed';
        }
        return (
            <div className='code-explorer'>
                <ErrorBoundary>
                    <ModelRenderer
                        model={ast}
                        goToNode={node => this.props.codeExplorerPlugin.appContext.command.dispatch('go-to-node', node)}
                        scrollPosition={this.state.scrollPosition}
                    />
                </ErrorBoundary>
            </div>
        );
    }
}

CodeExplorerPanel.propTypes = {
    codeExplorerPlugin: PropTypes.objectOf(Object).isRequired,
};

export default CodeExplorerPanel;
