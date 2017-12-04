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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { COMMANDS } from './../constants';

/**
 * Enable implicit features of views including reRender upon plugin re-render.
 * @param {Reac.Component} View
 * @param {String} pluginID
 */
export function withViewFeatures(View, pluginID) {
    class ViewWrapper extends React.Component {
         /**
         * @inheritdoc
         */
        constructor(props, context) {
            super(props, context);
            this.onPluginReRender = this.onPluginReRender.bind(this);
        }

        /**
         * @inheritdoc
         */
        componentDidMount() {
            const { on } = this.context.command;
            on(COMMANDS.RE_RENDER_PLUGIN, this.onPluginReRender);
        }

        /**
         * @inheritdoc
         */
        componentWillUnmount() {
            const { off } = this.context.command;
            off(COMMANDS.RE_RENDER_PLUGIN, this.onPluginReRender);
        }

        /**
         * Invoked when a plugin wants to re-render
         * @param {Object} command args
         */
        onPluginReRender({ id }) {
            if (id === pluginID) {
                this.forceUpdate();
            }
        }

         /**
         * @inheritdoc
         */
        render() {
            return (<View {...this.props} />);
        }
    }

    ViewWrapper.contextTypes = {
        command: PropTypes.shape({
            on: PropTypes.func,
            dispatch: PropTypes.func,
        }).isRequired,
    };

    return ViewWrapper;
}

/**
 * Creates View from view Def
 * @param {Object} viewDef View Definition
 * @param {Object} additionalProps additional props for view
 */
export function createViewFromViewDef(viewDef, additionalProps) {
    const { View, propsProvider } = viewDef;
    return (
        <View {...propsProvider()} key={viewDef.id} definition={viewDef} {...additionalProps} />
    );
}


/**
 * Inject apis into context.
 *
 * @param {Reac.Component} Dialog
 * @param {Object} appContext
 */
export function withDialogContext(Dialog, appContext) {
    class DialogWrapper extends React.Component {

        /**
         * @inheritdoc
         */
        getChildContext() {
            return {
                history: appContext.pref.history,
                command: appContext.command,
                alert: appContext.alert,
                editor: appContext.editor,
                workspace: appContext.workspace,
            };
        }

         /**
         * @inheritdoc
         */
        render() {
            return (<Dialog {...this.props} />);
        }
    }

    DialogWrapper.childContextTypes = {
        history: PropTypes.shape({
            put: PropTypes.func,
            get: PropTypes.func,
        }).isRequired,
        command: PropTypes.shape({
            on: PropTypes.func,
            dispatch: PropTypes.func,
        }).isRequired,
        alert: PropTypes.shape({
            showInfo: PropTypes.func,
            showSuccess: PropTypes.func,
            showWarning: PropTypes.func,
            showError: PropTypes.func,
        }).isRequired,
        editor: PropTypes.shape({
            getSupportedExtensions: PropTypes.func,
            isFileOpenedInEditor: PropTypes.func,
            getEditorByID: PropTypes.func,
            setActiveEditor: PropTypes.func,
            getActiveEditor: PropTypes.func,
            closeEditor: PropTypes.func,
        }).isRequired,
        workspace: PropTypes.shape({
            isFilePathOpenedInExplorer: PropTypes.func,
            refreshPathInExplorer: PropTypes.func,
            goToFileInExplorer: PropTypes.func,
        }).isRequired,
    };

    return DialogWrapper;
}
