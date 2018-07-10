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
import _ from 'lodash';
import $ from 'jquery';
import * as YAML from 'js-yaml';
import PropTypes from 'prop-types';
import log from 'log';
import { Button, Icon } from 'semantic-ui-react';
import SwaggerEditorBundle from 'swagger-editor-dist/swagger-editor-bundle';
import ServiceNode from 'plugins/ballerina/model/tree/service-node';
import { getSwaggerDefinition, getServiceDefinition } from 'api-client/api-client';
import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import { DIALOGS as DIALOG_IDS } from '../constants';
import TreeBuilder from './../model/tree-builder';
import { SPLIT_VIEW } from './constants';
import SwaggerUtil from '../swagger-util/swagger-util';


const ace = global.ace;
const AceUndoManager = ace.acequire('ace/undomanager').UndoManager;

/**
 * Class to override default undo manager of swagger editor.
 *
 * @class CustomUndoManager
 * @extends {AceUndoManager}
 */
class CustomUndoManager extends AceUndoManager {
    /**
     * Creates an instance of CustomUndoManager.
     * @param {func} onExecute On execute event.
     * @memberof CustomUndoManager
     */
    constructor(onExecute) {
        super();
        this.onExecute = onExecute;
    }

    /**
     * @override
     */
    execute(args) {
        super.execute(args);
        this.onExecute(args);
    }
}

// look & feel configurations FIXME: Make this overridable from settings
const theme = 'ace/theme/tomorrow_night';
const fontSize = '14px';

/**
 * React component for the swagger view.
 *
 * @class SwaggerView
 * @extends {React.Component}
 */
class SwaggerView extends React.Component {

    /**
     * Creates an instance of SwaggerView.
     * @param {Object} props React properties.
     * @memberof SwaggerView
     */
    constructor(props) {
        super(props);
        this.container = undefined;
        this.swagger = undefined;
        this.swaggerEditorID = undefined;
        this.swaggerEditor = undefined;
        this.swaggerAce = undefined;
        this.resourceMappings = new Map();
        this.onEditorChange = this.onEditorChange.bind(this);

        /* props.commandProxy.on('save', () => {
            this.updateService();
        }, this); */
    }

    /**
     * When this component is initially rendered, it won't have
     * a targetService. However, when a user clicks swagger-view
     * icon of a service, we update the state of parent component
     * and it will eventually provide a new set of props to this.
     *
     * Since we have disabled react rerender for this component
     * {@see SwaggerView#shouldComponentUpdate},
     * we have to grab those new props and update the component
     * manually.
     *
     * @param {Object} newProps React properties.
     * @memberof SwaggerView
     */
    componentWillReceiveProps(newProps) {
        if (!_.isNil(newProps.targetService) && newProps.visible) {
            this.props = newProps;
            this.swagger = newProps.swagger;
            this.swaggerEditorID = newProps.swaggerEditorID;
            this.renderSwaggerEditor();
        }
    }

    /**
     * @override
     */
    shouldComponentUpdate() {
        // prevent this component being re-rendered by react
        return false;
    }

    /**
     * onChange event for ace editor.
     * @memberof SwaggerView
     */
    onEditorChange() {
        try {
            // keep swagger spec up-to-date
            this.swagger = YAML.safeLoad(this.swaggerAce.getValue());
        } catch (error) {
            // No need to throw.
            log.error(error);
        }
    }

    handleCloseSwaggerView() {
        if (this.props.hideSwaggerAceEditor ||
            this.swaggerAce.getSession().getUndoManager().isClean()) {
            this.context.editor.setActiveView(SPLIT_VIEW);
        } else if (!this.hasSwaggerErrors()) {
            this.updateService();
            this.context.editor.setActiveView(SPLIT_VIEW);
        } else if (this.hasSwaggerErrors()) { 
            this.props.commandProxy.dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                id: DIALOG_IDS.INVALID_SWAGGER_DIALOG,
            });
        }
        this.props.resetSwaggerViewFun();
        /* this.context.astRoot.trigger('tree-modified', {
            origin: this.context.astRoot,
            type: 'swagger',
            title: 'Modify Swagger Definition',
            context: this.context.astRoot,
        }); */
    }

    /**
     * Binds a shortcut to ace editor so that it will trigger the command on source view upon key press.
     * All the commands registered app's command manager will be bound to source view upon render.
     *
     * @param {Object} command A command of the command manager.
     * @param {string} command.id Id of the command to dispatch
     * @param {Object} command.shortcuts Shortcuts
     * @param {Object} command.shortcuts.mac Max Shortcuts
     * @param {string} command.shortcuts.mac.key key combination for mac platform eg. 'Command+N'
     * @param {Object} command.shortcuts.other Other os shortcuts
     * @param {string} command.shortcuts.other.key key combination for other platforms eg. 'Ctrl+N'
     */
    bindCommand(command) {
        const id = command.id;
        const hasShortcut = _.has(command, 'shortcuts');
        const self = this;
        if (hasShortcut && (id !== 'undo' && id !== 'redo' && id !== 'format')) {
            const macShortcut = _.replace(command.shortcuts.mac.key, '+', '-');
            const winShortcut = _.replace(command.shortcuts.other.key, '+', '-');
            this.swaggerAce.commands.addCommand({
                name: id,
                bindKey: { win: winShortcut, mac: macShortcut },
                exec() {
                    self.props.commandManager.dispatch(id);
                },
            });
        }
    }

     /**
     * Merge the updated YAMLs to the service definition
     */
    updateService() {
        // we do not update the dom if swagger is not edited.
        if (this.swaggerAce && !this.swaggerAce.getSession().getUndoManager().isClean()) {
            // Merge to service. this.swagger
            getServiceDefinition(this.swagger, this.props.targetService.getName().getValue())
                .then((serviceDefinition) => {
                    SwaggerUtil.merge(this.context.astRoot, TreeBuilder.build(serviceDefinition.model),
                    this.props.targetService);
                })
                .catch(error => log.error(error));
        }
    }

    /**
     * Update spec in editor.
     * @memberof SwaggerView
     */
    syncSpec() {
        this.swaggerEditor.specActions.updateUrl('');
        this.swaggerEditor.specActions.updateLoadingStatus('success');
        try {
            const yamlString = YAML.safeDump(YAML.safeLoad(this.swagger), {
                lineWidth: -1, // don't generate line folds
            });
            this.swaggerEditor.specActions.updateSpec(yamlString);
        } catch (e) {
            log.error('Error while updating swagger editor.');
        }
    }

    /**
     * Returns the number of errors in the editor.
     *
     * @returns {boolean} True if errors exists, else false.
     * @memberof SwaggerView
     */
    hasSwaggerErrors() {
        return _.size(this.swaggerAce.getSession().getAnnotations());
    }

    /**
     * Renders the editor.
     * @memberof SwaggerView
     */
    renderSwaggerEditor() {
        if (!_.isNil(this.props.targetService)) {
            const $container = $(this.container);
            $container.empty();
            $container.attr('id', this.swaggerEditorID);

            if (this.props.hideSwaggerAceEditor) {
                $container.hide();
            }

            this.swaggerEditor = SwaggerEditorBundle({
                dom_id: `#${this.swaggerEditorID}`,
            });
            const $swaggerAceContainer = $container.find('#ace-editor');
            const swaggerAceContainerID = `z-ace-editor-${this.swaggerEditorID}`;
            $swaggerAceContainer.attr('id', swaggerAceContainerID);
            const swaggerAce = ace.edit(swaggerAceContainerID);

            swaggerAce.getSession().setUndoManager(new CustomUndoManager(this.onEditorChange));
            swaggerAce.$blockScrolling = Infinity;
            swaggerAce.setTheme(ace.acequire(theme));
            swaggerAce.setFontSize(fontSize);
            this.swaggerAce = swaggerAce;
            this.syncSpec();

            if (this.props.hideSwaggerAceEditor) {
                const editorPanel = $swaggerAceContainer.parent().parent();
                editorPanel.hide();
                editorPanel.next().next().css('width', '100%');
                $container.show();
            }

            // bind app keyboard shortcuts to ace editor
            this.props.commandProxy.getCommands().forEach((command) => {
                this.bindCommand(command);
            });
        }
    }

    /**
     * Render the required foundation to init swagger editor
     *
     * @returns {ReactElement} The view.
     * @memberof SwaggerView
     */
    render() {
        return (
            <div
                className='swagger-view-container'
                style={{
                    width: '100%',
                    height: this.props.height,
                }}
            >
                <div className='close-swagger'>
                    <Button
                        primary
                        onClick={() => {
                            this.handleCloseSwaggerView();
                        }}
                        size='small'
                        content='Back'
                        icon='left arrow'
                        labelPosition='left'
                    />
                </div>
                <div
                    className='swaggerEditor'
                    // keep the ref to this element as the container ref
                    ref={(ref) => { this.container = ref; }}
                    data-editor-url='lib/swagger-editor/#/'
                />
            </div>
        );
    }
}

SwaggerView.propTypes = {
    targetService: PropTypes.instanceOf(ServiceNode),
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
    }).isRequired,
    hideSwaggerAceEditor: PropTypes.bool,
    resetSwaggerViewFun: PropTypes.func.isRequired,
};

SwaggerView.defaultProps = {
    targetService: undefined,
    hideSwaggerAceEditor: false,
};

SwaggerView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    astRoot: PropTypes.instanceOf(Object),
    isPreviewViewEnabled: PropTypes.bool.isRequired,
};

export default SwaggerView;
