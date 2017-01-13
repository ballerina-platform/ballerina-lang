/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['lodash', 'event_channel'],
    function (_, EventChannel){

    var MenuItem = function(args){
        _.assign(this, args);
    };

    MenuItem.prototype = Object.create(EventChannel.prototype);
    MenuItem.prototype.constructor = MenuItem;

    MenuItem.prototype.render = function(){
        var self = this;
        var parent = _.get(this, 'options.parent');
        this._commandManager = _.get(this, 'options.commandManager');
        this._commandID = _.get(this, 'definition.action');

        var item = $('<li></li>');
        var link = $('<a></a>');
        parent.append(item);
        item.append(link);

        link.text(_.get(this, 'definition.label'));
        this._labelElement = link;
        _.forEach(_.get(this, 'definition.attributes'), function(attribute){
            link.attr(attribute.key, attribute.value);
        });

        if (_.get(this, 'definition.disabled')) {
            this.disable();
        } else {
            this.enable();
        }
    };

    MenuItem.prototype.getID = function(){
        return _.get(this, 'definition.id');
    };

    MenuItem.prototype.disable = function(){
        this._labelElement.addClass(_.get(this, 'options.cssClass.inactive'));
        this._labelElement.removeClass(_.get(this, 'options.cssClass.active'));
        this._labelElement.off("click");
    };

    MenuItem.prototype.enable = function(){
        this._labelElement.addClass(_.get(this, 'options.cssClass.active'));
        this._labelElement.removeClass(_.get(this, 'options.cssClass.inactive'));
        var self = this;
        this._labelElement.off("click");
        this._labelElement.click(function () {
            self._commandManager.dispatch(self._commandID);
        });
    };

    MenuItem.prototype.addLabelSuffix = function(labelSuffix){
       if(!_.isNil(labelSuffix)){
           this._labelElement.text(_.get(this, 'definition.label') + " " + labelSuffix)
       }
    };

    MenuItem.prototype.clearLabelSuffix = function(){
           this._labelElement.text(_.get(this, 'definition.label'))
    };

    return MenuItem;
        
});