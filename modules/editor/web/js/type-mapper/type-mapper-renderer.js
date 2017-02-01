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
define(['require', 'lodash','jquery','jsPlumb', 'dagre'], function(require, _,$,jsPlumb, dagre) {

    var TypeMapper = function(onConnectionCallback, onDisconnectCallback) {

        this.references = []
        this.placeHolderName = "data-mapper-container"
        this.idNameSeperator = "-";
        this.onConnection = onConnectionCallback;

        var strokeColor = "#414e66";
        var strokeWidth = 2;
        var pointColor = "#414e66";
        var pointSize = 1;

        jsPlumb.Defaults.Container = $("#" + this.placeHolderName);
        jsPlumb.Defaults.PaintStyle = {
            strokeStyle:strokeColor,
            lineWidth:strokeWidth
        };

        jsPlumb.Defaults.EndpointStyle = {
            radius:pointSize,
            fillStyle:pointColor
        };
        jsPlumb.Defaults.Overlays = [
            [ "Arrow",{location:1, width:12, length:12} ]
        ];

        jsPlumb.importDefaults({Connector : [ "Flowchart",
            {
                cornerRadius: 10,
                stub:20,  alwaysRespectStubs: true
            } ]});

        var positionFunc = this.dagrePosition;
        var separator = this.idNameSeperator;
        var refObjects = this.references;

        jsPlumb.bind('dblclick', function (connection, e) {

            var sourceRefObj;
            var targetRefObj;

            for (var i = 0; i < refObjects.length; i++) {
                if (refObjects[i].name == connection.sourceId.split(separator)[0]) {
                    sourceRefObj = refObjects[i].refObj;
                } else if (refObjects[i].name == connection.targetId.split(separator)[0]) {
                    targetRefObj = refObjects[i].refObj;
                }
            };

            var PropertyConnection = {
                sourceStruct : connection.source.id.split(separator)[0],
                sourceProperty : connection.source.id.split(separator)[1],
                sourceType : connection.source.id.split(separator)[2],
                sourceReference : sourceRefObj,
                targetStruct : connection.target.id.split(separator)[0],
                targetProperty : connection.target.id.split(separator)[1],
                targetType : connection.target.id.split(separator)[2],
                targetReference : targetRefObj
            }

            jsPlumb.detach(connection);
            positionFunc();
            onDisconnectCallback(PropertyConnection);
        });

        jsPlumb.bind('connection',function(info,ev){
            positionFunc();
        });


    }

    TypeMapper.prototype.constructor = TypeMapper;

    TypeMapper.prototype.removeStruct  = function (name){
        jsPlumb.detachEveryConnection();
        $("#" + name).remove();
        this.dagrePosition();
    }

    TypeMapper.prototype.addConnection  = function (connection) {
        jsPlumb.connect({
            source:connection.sourceStruct + this.idNameSeperator + connection.sourceProperty + this.idNameSeperator + connection.sourceType,
            target:connection.targetStruct + this.idNameSeperator + connection.targetProperty + this.idNameSeperator + connection.targetType
        });
        this.dagrePosition();
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

    TypeMapper.prototype.addSourceStruct  = function (struct, reference) {
        this.makeStruct(struct, 50, 50, reference);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addSourceProperty($('#' + struct.name), struct.properties[i].name, struct.properties[i].type);
        };

        this.dagrePosition();
    }

    TypeMapper.prototype.addTargetStruct  = function (struct, reference) {
        var placeHolderWidth = document.getElementById(this.placeHolderName).offsetWidth;
        var posY = placeHolderWidth - (placeHolderWidth/4);
        this.makeStruct(struct, 50, posY, reference);
        for (var i = 0; i < struct.properties.length; i++) {
            this.addTargetProperty($('#' +struct.name), struct.properties[i].name, struct.properties[i].type);
        };

        this.dagrePosition();
    }

    TypeMapper.prototype.makeStruct  = function (struct, posX, posY, reference) {
        this.references.push({ name : struct.name, refObj : reference});
        var newStruct = $('<div>').attr('id', struct.name).addClass('struct');

        var structName = $('<div>').addClass('struct-name').text(struct.name);
        newStruct.append(structName);

        newStruct.css({
            'top': posX,
            'left': posY
        });

        $("#" + this.placeHolderName).append(newStruct);
        // jsPlumb.draggable(newStruct, {
        //     containment: 'parent'
        // });
    }

    TypeMapper.prototype.makeFunction  = function (func, reference) {
        this.references.push({ name : func.name, refObj : reference});
        var newFunc = $('<div>').attr('id', func.name).addClass('struct');

        var funcName = $('<div>').addClass('struct-name').text(func.name);
        newFunc.append(funcName);

        newFunc.css({
            'top': 0,
            'left': 0
        });

        $("#" + this.placeHolderName).append(newFunc);



        for (var i = 0; i < func.parameters.length; i++) {
            this.addTargetProperty($('#' +func.name), func.parameters[i].name, func.parameters[i].type);
        };

        this.addSourceProperty($('#' + func.name), "output", func.returnType);
        this.dagrePosition();

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
        var refObjects = this.references;
        var seperator = this.idNameSeperator;

        jsPlumb.makeTarget(this.makeProperty(parentId, name, type), {
            maxConnections:1,
            anchor:["Continuous", { faces:[ "left"] } ],
            beforeDrop: function (params) {
                //Checks property types are equal
                var isValidTypes = params.sourceId.split(seperator)[2] == params.targetId.split(seperator)[2];


                if (isValidTypes) {

                    var sourceRefObj;
                    var targetRefObj;

                    for (var i = 0; i < refObjects.length; i++) {
                        if (refObjects[i].name == params.sourceId.split(seperator)[0]) {
                            sourceRefObj = refObjects[i].refObj;
                        } else if (refObjects[i].name == params.targetId.split(seperator)[0]) {
                            targetRefObj = refObjects[i].refObj;
                        }
                    };

                    var connection = {
                        sourceStruct : params.sourceId.split(seperator)[0],
                        sourceProperty : params.sourceId.split(seperator)[1],
                        sourceType : params.sourceId.split(seperator)[2],
                        sourceReference : sourceRefObj,
                        targetStruct : params.targetId.split(seperator)[0],
                        targetProperty : params.targetId.split(seperator)[1],
                        targetType : params.targetId.split(seperator)[2],
                        targetReference : targetRefObj
                    }

                    callback(connection);
                }
                return isValidTypes;
            },

            onDrop : function(params) {
                this.dagrePosition();
            }
        });
    }


    TypeMapper.prototype.dagrePosition  = function(){
        // construct dagre graph from JsPlumb graph
        var g = new dagre.graphlib.Graph();
        g.setGraph({ranksep:'100',rankdir: 'LR', edgesep:'50', ranker : 'tight-tree'});
        g.setDefaultEdgeLabel(function() { return {}; });
        var nodes = $(".struct");
        for (var i = 0; i < nodes.length; i++) {
            var n = nodes[i];

            g.setNode(n.id, {width: 300, height: $("#" + n.id).height()});
        }
        var edges = jsPlumb.getAllConnections();
        for (var i = 0; i < edges.length; i++) {
            var c = edges[i];
            g.setEdge(c.source.id.split("-")[0],c.target.id.split("-")[0]);
        }

        // calculate the layout (i.e. node positions)
        dagre.layout(g);

        // Applying the calculated layout
        g.nodes().forEach(function(v) {
            $("#" + v).css("left", g.node(v).x + "px");
            $("#" + v).css("top", g.node(v).y + "px");
        });
        jsPlumb.repaintEverything();
    }

    return TypeMapper;

});