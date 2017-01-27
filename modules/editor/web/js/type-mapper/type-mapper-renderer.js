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
define(['require', 'lodash','jquery','jsPlumb','nano_scroller'], function(require, _,$,jsPlumb,ggg) {

    var TypeMapper = function(onConnectionCallback, onDisconnectCallback) {
        this.placeHolderName = "data-mapper-container"
        this.idNameSeperator = "-";
        this.onConnection = onConnectionCallback;

        var strokeColor = "#414e66";
        var strokeWidth = 2;
        var pointColor = "#414e66";
        var pointSize = 5;
        var dashStyle = "3 3";

        jsPlumb.Defaults.Container = $("#" + this.placeHolderName);
        jsPlumb.Defaults.PaintStyle = {
            strokeStyle:strokeColor,
            lineWidth:strokeWidth,
            dashstyle: dashStyle
        };

        jsPlumb.Defaults.EndpointStyle = {
            radius:pointSize,
            fillStyle:pointColor
        };
        jsPlumb.Defaults.Overlays = [
            [ "Arrow", {
                location:0.5,
                id:"arrow",
                length:14,
                foldback:0.8
            } ]
        ];

        jsPlumb.importDefaults({Connector : [ "Bezier", { curviness:1 } ]});
        jsPlumb.bind('dblclick', function (connection, e) {
            var PropertyConnection = {
                sourceStruct : connection.source.id.split(this.idNameSeperator)[0],
                sourceProperty : connection.source.id.split(this.idNameSeperator)[1],
                sourceType : connection.source.id.split(this.idNameSeperator)[2],
                targetStruct : connection.target.id.split(this.idNameSeperator)[0],
                targetProperty : connection.target.id.split(this.idNameSeperator)[1],
                targetType : connection.target.id.split(this.idNameSeperator)[2]
            }

            jsPlumb.detach(connection);
            onDisconnectCallback(PropertyConnection);
        });


    }

    TypeMapper.prototype.removeStruct  = function (name){
        jsPlumb.detachEveryConnection();
        $("#" + name).remove();
    }

    TypeMapper.prototype.addConnection  = function (connection) {
        jsPlumb.connect({
            source:connection.sourceStruct + this.idNameSeperator + connection.sourceProperty + this.idNameSeperator + connection.sourceType,
            target:connection.targetStruct + this.idNameSeperator + connection.targetProperty + this.idNameSeperator + connection.targetType
        });
    }

    TypeMapper.prototype.getConnections  = function () {
        var connections = [];

        for (var i = 0; i < jsPlumb.getConnections().length; i++) {
            var connection = {
                sourceStruct : jsPlumb.getConnections()[i].sourceId.split(this.idNameSeperator)[0],
                sourceProperty : jsPlumb.getConnections()[i].sourceId.split(this.idNameSeperator)[1],
                sourceType : jsPlumb.getConnections()[i].sourceId.split(this.idNameSeperator)[2],
                targetStruct : jsPlumb.getConnections()[i].targetId.split(this.idNameSeperator)[0],
                targetProperty : jsPlumb.getConnections()[i].targetId.split(this.idNameSeperator)[1],
                targetType : jsPlumb.getConnections()[i].targetId.split(this.idNameSeperator)[2]
            }
            connections.push(connection);
        };

        return connections;
    }

    TypeMapper.prototype.addSourceStruct  = function (struct) {
        this.makeStruct(struct, 50, 50);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addSourceProperty($('#' + struct.name), struct.properties[i].name, struct.properties[i].type);
        };
    }

    TypeMapper.prototype.addTargetStruct  = function (struct) {
        var placeHolderWidth = document.getElementById(this.placeHolderName).offsetWidth;
        var posY = placeHolderWidth - (placeHolderWidth/4);
        this.makeStruct(struct, 50, posY);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addTargetProperty($('#' +struct.name), struct.properties[i].name, struct.properties[i].type);
        };
    }

    TypeMapper.prototype.makeStruct  = function (struct, posX, posY) {
        var newStruct = $('<div>').attr('id', struct.name).addClass('struct');

        var structName = $('<div>').addClass('struct-name').text(struct.name);
        newStruct.append(structName);

        newStruct.css({
            'top': posX,
            'left': posY
        });

        $("#" + this.placeHolderName).append(newStruct);
        jsPlumb.draggable(newStruct, {
            containment: 'parent'
        });
    }

    TypeMapper.prototype.makeProperty  = function (parentId, name, type) {
        var id = parentId.selector.replace("#","") + this.idNameSeperator + name + this.idNameSeperator  + type;
        var property = $('<div>').attr('id', id).addClass('property')
        var propertyName = $('<span>').addClass('property-name').text(name);
        var seperator = $('<span>').addClass('property-name').text(":");
        var propertyType = $('<span>').addClass('property-type').text(type);

        property.append(propertyName);
        property.append(seperator);
        property.append(propertyType);
        $(parentId).append(property);

        return property;
    }

    TypeMapper.prototype.addSourceProperty  = function (parentId, name, type) {
        jsPlumb.makeSource(this.makeProperty(parentId, name, type), {
            anchor:["Continuous", { faces:["right"] } ]
        });
    }

    TypeMapper.prototype.addTargetProperty  = function (parentId, name, type) {
        var callback = this.onConnection;

        jsPlumb.makeTarget(this.makeProperty(parentId, name, type), {
            maxConnections:1,
            anchor:["Continuous", { faces:[ "left"] } ],
            beforeDrop: function (params) {
                //Checks property types are equal
                var isValidTypes = params.sourceId.split(this.idNameSeperator)[2] == params.targetId.split(this.idNameSeperator)[2];

                if (isValidTypes) {

                    var connection = {
                        sourceStruct : params.sourceId.split(this.idNameSeperator)[0],
                        sourceProperty : params.sourceId.split(this.idNameSeperator)[1],
                        sourceType : params.sourceId.split(this.idNameSeperator)[2],
                        targetStruct : params.targetId.split(this.idNameSeperator)[0],
                        targetProperty : params.targetId.split(this.idNameSeperator)[1],
                        targetType : params.targetId.split(this.idNameSeperator)[2]
                    }

                    callback(connection);
                }
                return isValidTypes;
            }
        });
    }

});