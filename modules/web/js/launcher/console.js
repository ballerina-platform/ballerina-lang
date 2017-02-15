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
define([ 'jquery',  'lodash', 'event_channel' ],
    function ( $, _ , EventChannel ) {
	var instance;

    var Console = function(args) {
        this.container = $('#console-container');
        this.console = $('#console');

        this.container.on('click', '.closeConsole', _.bindKey(this, 'hide'));
    };

    Console.prototype = Object.create(EventChannel.prototype);
    Console.prototype.constructor = Console;

    Console.prototype.show = function(){
        this.container.show();
        $('#service-tabs-wrapper').css('height','70%'); 
        this.container.removeClass('hide'); 
        this.container.css('height','30%');
    };

    Console.prototype.hide = function(){
        this.container.hide();
        $('#service-tabs-wrapper').css('height','100%');
    };    

    Console.prototype.clear = function(){
        this.console.html('');
    };

    Console.prototype.println = function(message){
        this.console.append('<span class="' + message.type + '">' + message.message + '<span>');
        this.console.append("<br />");
        //todo need a proper fix
        this.console.scrollTop(100000);
    };

    return (instance = (instance || new Console()));
});
