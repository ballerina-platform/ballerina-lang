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

import $ from 'jquery';
import Backbone from 'backbone';
import _ from 'lodash';
import log from 'log';
import Tools from './tools';
import Frames from './frames';
import './debugger.css';
/**
 * Debugger view
 */
const Debugger = Backbone.View.extend({
    /** @inheritdoc */
    initialize(config) {
        let errMsg;
        this._breakPoints = {};
        if (!_.has(config, 'container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        const container = $(_.get(config, 'container'));
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = `unable to find container for debugger with selector: ${_.get(config, 'container')}`;
            log.error(errMsg);
            throw errMsg;
        }
        this._$parent_el = container;

        if (!_.has(config, 'application')) {
            log.error('Cannot init debugger. config: application not found.');
        }

        this.application = _.get(config, 'application');
        this.launchManager = _.get(config, 'launchManager');
        this._options = config;
        this.debuggerServiceUrl = _.get(this._options, 'application.config.services.debugger.endpoint');
        this._lastWidth = undefined;
        this._verticalSeparator = $(_.get(this._options, 'separator'));
        this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));

        // register command
        this.application.commandManager.registerCommand(config.command.id, { shortcuts: config.command.shortcuts });
        this.application.commandManager.registerHandler(config.command.id, this.toggleDebugger, this);
    },
    /**
     * Returns true if debugger toolbar is active
     * @returns Boolean
     */
    isActive() {
        return this._activateBtn.parent('li').hasClass('active');
    },
    /**
     * Toggle debugger view
     */
    toggleDebugger() {
        if (this.isActive()) {
            this._$parent_el.parent().width('0px');
            this._containerToAdjust.css('padding-left', _.get(this._options, 'leftOffset'));
            this._verticalSeparator.css('left',
                _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
            this._activateBtn.parent('li').removeClass('active');
            this.application.reRender();// to update the diagrams
        } else {
            this._activateBtn.tab('show');
            const width = this._lastWidth || _.get(this._options, 'defaultWidth');
            this._$parent_el.parent().width(width);
            this._containerToAdjust.css('padding-left', width);
            this._verticalSeparator.css('left', width - _.get(this._options, 'separatorOffset'));
            this.application.reRender();// to update the diagrams
        }
    },
    /** @inheritdoc */
    render() {
        const activateBtn = $(_.get(this._options, 'activateBtn'));
        this._activateBtn = activateBtn;

        this.renderContent();
        activateBtn.on('show.bs.tab', () => {
            this._isActive = true;
            const width = this._lastWidth || _.get(this._options, 'defaultWidth');
            this._$parent_el.parent().width(width);
            this._containerToAdjust.css('padding-left', width + _.get(this._options, 'leftOffset'));
            this._verticalSeparator.css('left',
                (width + _.get(this._options, 'leftOffset')) - _.get(this._options, 'separatorOffset'));
        });

        activateBtn.on('hide.bs.tab', () => {
            this._isActive = false;
        });

        activateBtn.on('click', (e) => {
            $(this).tooltip('hide');
            e.preventDefault();
            e.stopPropagation();
            this.application.commandManager.dispatch(_.get(this._options, 'command.id'));
        });

        activateBtn.attr('data-placement', 'bottom').attr('data-container', 'body');

        if (this.application.isRunningOnMacOS()) {
            activateBtn.attr('title', `Debugger ( ${_.get(this._options, 'command.shortcuts.mac.label')} ) `).tooltip();
        } else {
            activateBtn.attr('title',
              `Debugger ( ${_.get(this._options, 'command.shortcuts.other.label')} ) `).tooltip();
        }

        return this;
    },
    /**
     * Render body of the debugger view
     */
    renderContent() {
        const debuggerContainer = $(`
            <div>
                <div class="debug-tools-container"></div>
                <div class="debug-frames-container"></div>
            </div>
        `);
        debuggerContainer.addClass(_.get(this._options, 'cssClass.container'));
        debuggerContainer.attr('id', _.get(this._options, ('containerId')));
        this._$parent_el.append(debuggerContainer);

        Tools.setArgs({ container: debuggerContainer.find('.debug-tools-container'),
            launchManager: this.launchManager,
            application: this.application,
            toolbarShortcuts: _.get(this._options, 'toolbarShortcuts') });
        Tools.render();

        Frames.setContainer(debuggerContainer.find('.debug-frames-container'));

        this._debuggerContainer = debuggerContainer;
        debuggerContainer.mCustomScrollbar({
            theme: 'minimal',
            scrollInertia: 0,
        });
    },

});

export default Debugger;
