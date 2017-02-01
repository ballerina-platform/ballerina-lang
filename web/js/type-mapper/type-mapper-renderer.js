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
define(['require', 'lodash', 'jquery', 'jsPlumb'], function (require, _, $, jsPlumb) {

    var TypeMapper = function (onConnectionCallback, onDisconnectCallback, typeConverterView) {

        this.references = [];
        this.placeHolderName = "data-mapper-container-" + typeConverterView._model.id;
        this.idNameSeperator = "-";
        this.onConnection = onConnectionCallback;
        this.typeConverterView = typeConverterView;

        var strokeColor = "#414e66";
        var strokeWidth = 2;
        var pointColor = "#414e66";
        var pointSize = 5;
        var dashStyle = "3 3";

        jsPlumb.Defaults.Container = $("#" + this.placeHolderName);
        jsPlumb.Defaults.PaintStyle = {
            strokeStyle: strokeColor,
            lineWidth: strokeWidth,
            dashstyle: dashStyle
        };

        jsPlumb.Defaults.EndpointStyle = {
            radius: pointSize,
            fillStyle: pointColor
        };
        jsPlumb.Defaults.Overlays = [
            ["Arrow", {
                location: 0.5,
                id: "arrow",
                length: 14,
                foldback: 0.8
            }]
        ];

        jsPlumb.importDefaults({Connector: ["Bezier", {curviness: 1}]});
        jsPlumb.bind('dblclick', function (connection, e) {
            console.log('db click');
            console.log(connection);
            var PropertyConnection = {
                sourceStruct: connection.source.id.split(this.idNameSeperator)[0],
                sourceProperty: connection.source.id.split(this.idNameSeperator)[6],
                sourceType: connection.source.id.split(this.idNameSeperator)[7],
                targetStruct: connection.target.id.split(this.idNameSeperator)[0],
                targetProperty: connection.target.id.split(this.idNameSeperator)[6],
                targetType: connection.target.id.split(this.idNameSeperator)[7]
            };

            jsPlumb.detach(connection);
            onDisconnectCallback(PropertyConnection);
        });


    };

    TypeMapper.prototype.constructor = TypeMapper;

    TypeMapper.prototype.removeStruct = function (name) {
        var structId = name + '-' + this.typeConverterView._model.id;
        var structConns = $('div[id^="' + structId + '"]');
        for (var i = 0; i < structConns.length; i++) {
            var div = structConns[i];
            if (_.includes(div.className, 'property')) {
                jsPlumb.remove(div.id);
            }
        }
        $("#" + structId).remove();
    };

    TypeMapper.prototype.addConnection = function (connection) {
        jsPlumb.connect({
            source: connection.sourceStruct + this.idNameSeperator + connection.sourceProperty + this.idNameSeperator + connection.sourceType,
            target: connection.targetStruct + this.idNameSeperator + connection.targetProperty + this.idNameSeperator + connection.targetType
        });
    };

    TypeMapper.prototype.getConnections = function () {
        console.log('get connections called');
        var connections = [];
        for (var i = 0; i < jsPlumb.getConnections().length; i++) {
            var connection = {
                sourceStruct: jsPlumb.getConnections()[i].sourceId.split(this.idNameSeperator)[0],
                sourceProperty: jsPlumb.getConnections()[i].sourceId.split(this.idNameSeperator)[6],
                sourceType: jsPlumb.getConnections()[i].sourceId.split(this.idNameSeperator)[7],
                targetStruct: jsPlumb.getConnections()[i].targetId.split(this.idNameSeperator)[0],
                targetProperty: jsPlumb.getConnections()[i].targetId.split(this.idNameSeperator)[6],
                targetType: jsPlumb.getConnections()[i].targetId.split(this.idNameSeperator)[7]
            };
            connections.push(connection);
        }
        return connections;
    };

    TypeMapper.prototype.addSourceStruct = function (struct, reference) {
        struct.id = struct.name + '-' + this.typeConverterView._model.id;
        this.makeStruct(struct, 50, 50, reference);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addSourceProperty($('#' + struct.id), struct.properties[i].name, struct.properties[i].type);
        }
    };

    TypeMapper.prototype.addTargetStruct = function (struct, reference) {
        struct.id = struct.name + '-' + this.typeConverterView._model.id;
        var placeHolderWidth = document.getElementById(this.placeHolderName).offsetWidth;
        var posY = placeHolderWidth - (placeHolderWidth / 4);
        this.makeStruct(struct, 50, posY, reference);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addTargetProperty($('#' + struct.id), struct.properties[i].name, struct.properties[i].type);
        }
    };

    TypeMapper.prototype.makeStruct = function (struct, posX, posY, reference) {
        this.references.push({name: struct.id, refObj: reference});
        var newStruct = $('<div>').attr('id', struct.id).addClass('struct');

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
    };

    TypeMapper.prototype.makeProperty = function (parentId, name, type) {
        var id = parentId.selector.replace("#", "") + this.idNameSeperator + name + this.idNameSeperator + type;
        var property = $('<div>').attr('id', id).addClass('property');
        var propertyName = $('<span>').addClass('property-name').text(name);
        var seperator = $('<span>').addClass('property-name').text(":");
        var propertyType = $('<span>').addClass('property-type').text(type);

        property.append(propertyName);
        property.append(seperator);
        property.append(propertyType);
        $(parentId).append(property);
        return property;
    };

    TypeMapper.prototype.addSourceProperty = function (parentId, name, type) {
        jsPlumb.makeSource(this.makeProperty(parentId, name, type), {
            anchor: ["Continuous", {faces: ["right"]}]
        });
    };

    TypeMapper.prototype.addTargetProperty = function (parentId, name, type) {
        var callback = this.onConnection;
        var refObjects = this.references;
        var seperator = this.idNameSeperator;
        var typeConverterObj = this.typeConverterView;

        jsPlumb.makeTarget(this.makeProperty(parentId, name, type), {
            maxConnections: 1,
            anchor: ["Continuous", {faces: ["left"]}],
            beforeDrop: function (params) {
                //Checks property types are equal
                var sourceParts = params.sourceId.split(seperator);
                var targetParts = params.targetId.split(seperator);
                var isValidTypes = sourceParts[7] == targetParts[7];
                var sourceId = sourceParts.slice(0, 6).join('-');
                var targetId = targetParts.slice(0, 6).join('-');

                var sourceRefObj;
                var targetRefObj;

                for (var i = 0; i < refObjects.length; i++) {
                    if (refObjects[i].name == sourceId) {
                        sourceRefObj = refObjects[i].refObj;
                    } else if (refObjects[i].name == targetId) {
                        targetRefObj = refObjects[i].refObj;
                    }
                }

                var connection = {
                    sourceStruct: sourceParts[0],
                    sourceProperty: sourceParts[6],
                    sourceType: sourceParts [7],
                    sourceReference: sourceRefObj,
                    targetStruct: targetParts[0],
                    targetProperty: targetParts [6],
                    targetType: targetParts[7],
                    targetReference: targetRefObj
                };

                if (isValidTypes) {
                    callback(connection, typeConverterObj);
                }
                // } else {
                //     var compatibleTypeConverters = [];
                //     var typeConverters = typeConverterObj._package.getTypeConverterDefinitions();
                //     for (var i = 0; i < typeConverters.length; i++) {
                //         var aTypeConverter = typeConverters[i];
                //         if (typeConverterObj._model._typeConverterName !== aTypeConverter.getTypeConverterName()) {
                //             if (connection.sourceType == aTypeConverter.getSourceAndIdentifier().split(" ")[0] &&
                //                 connection.targetType == aTypeConverter.getReturnType()) {
                //                 compatibleTypeConverters.push(aTypeConverter.getTypeConverterName());
                //                 // console.log(aTypeConverter.getTypeConverterName());
                //                 // console.log(aTypeConverter.getSourceAndIdentifier());
                //                 // console.log(aTypeConverter.getReturnType());
                //             }
                //         }
                //     }
                //     console.log(compatibleTypeConverters);
                //     isValidTypes = compatibleTypeConverters.length > 0;
                // }
                return isValidTypes;
            }
        });
    };

    return TypeMapper;

});