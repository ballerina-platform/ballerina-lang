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
import EventChannel from 'event_channel';
import _ from 'lodash';
/**
 * Displays messages sent From Launcher/Debugger backend
 *
 * @class Console
 * @extends {EventChannel}
 */
class Console extends EventChannel {
    /**
     * Creates an instance of Console.
     *
     * @memberof Console
     */
    constructor() {
        super();
        this.container = $('#console-container');
        this.console = $('#console');

        this.container.on('click', '.closeConsole', () => { this.hide(); });
    }
    /**
     * @param {Application} app - Application object
     *
     * @memberof Console
     */
    setApplication(app) {
        this.application = app;
    }
    /**
     * Show console view
     * @memberof Console
     */
    show() {
        this.container.show();
        $('#service-tabs-wrapper').css('height', '70%');
        this.container.removeClass('hide');
        this.container.css('height', '30%');
        this.application.reRender();
    }
    /**
     * Hide console view
     * @memberof Console
     */
    hide() {
        this.container.hide();
        $('#service-tabs-wrapper').css('height', '100%');
        this.application.reRender();
    }
    /**
     * Clears Previous console outputs
     * @memberof Console
     */
    clear() {
        this.console.html('');
    }

    /**
     * Print a line in console
     * @param {Object} message
     *
     * @memberof Console
     */
    println(message) {
        const specialCharsEscapedStr = _.escape(message.message);
        this.console.append(`<div class="${message.type}"><pre>${specialCharsEscapedStr}</pre></div>`);
        // todo need a proper fix
        this.console.scrollTop(100000);
    }
}

export default new Console();
