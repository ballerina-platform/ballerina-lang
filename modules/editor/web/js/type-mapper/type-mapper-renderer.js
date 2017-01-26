var PLACEHOLDER_NAME = "data-mapper-container"
var STROKE_COLOR = "#414e66";
var STROKE_WIDTH = 2;
var POINT_COLOR = "#414e66";
var POINT_SIZE = 5;
var DASH_STYLE = "3 3";

var ID_NAME_SEPERATOR = "-";

jsPlumb.ready(function() {
    jsPlumb.Defaults.Container = $("#" + PLACEHOLDER_NAME);
    jsPlumb.Defaults.PaintStyle = {
        strokeStyle:STROKE_COLOR,
        lineWidth:STROKE_WIDTH,
        dashstyle: DASH_STYLE
    };

    jsPlumb.Defaults.EndpointStyle = {
        radius:POINT_SIZE,
        fillStyle:POINT_COLOR
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
        jsPlumb.detach(connection);
    });

});

function removeStruct(name) {
    jsPlumb.detachEveryConnection();
    $("#" + name).remove();
}


function addConnection(connection) {
    jsPlumb.connect({
        source:connection.sourceStruct + ID_NAME_SEPERATOR + connection.sourceProperty + ID_NAME_SEPERATOR + connection.sourceType,
        target:connection.targetStruct + ID_NAME_SEPERATOR + connection.targetProperty + ID_NAME_SEPERATOR + connection.targetType
    });
}

function getConnections() {
    var connections = [];

    for (var i = 0; i < jsPlumb.getConnections().length; i++) {
        var connection = {
            sourceStruct : jsPlumb.getConnections()[i].sourceId.split(ID_NAME_SEPERATOR)[0],
            sourceProperty : jsPlumb.getConnections()[i].sourceId.split(ID_NAME_SEPERATOR)[1],
            sourceType : jsPlumb.getConnections()[i].sourceId.split(ID_NAME_SEPERATOR)[2],
            targetStruct : jsPlumb.getConnections()[i].targetId.split(ID_NAME_SEPERATOR)[0],
            targetProperty : jsPlumb.getConnections()[i].targetId.split(ID_NAME_SEPERATOR)[1],
            targetType : jsPlumb.getConnections()[i].targetId.split(ID_NAME_SEPERATOR)[2]
        }
        connections.push(connection);
    };

    return connections;
}

function addSourceStruct(struct) {
    makeStruct(struct, 50, 50);
    for (var i = 0; i < struct.properties.length; i++) {
        addSourceProperty($('#' + struct.name), struct.properties[i].name, struct.properties[i].type);
    };
}

function addTargetStruct(struct) {
    var placeHolderWidth = document.getElementById(PLACEHOLDER_NAME).offsetWidth;
    var posY = placeHolderWidth - (placeHolderWidth/4);
    makeStruct(struct, 50, posY);
    for (var i = 0; i < struct.properties.length; i++) {
        addTargetProperty($('#' +struct.name), struct.properties[i].name, struct.properties[i].type);
    };
}

function makeStruct(struct, posX, posY) {
    var newStruct = $('<div>').attr('id', struct.name).addClass('struct');

    var structName = $('<div>').addClass('struct-name').text(struct.name);
    newStruct.append(structName);

    newStruct.css({
        'top': posX,
        'left': posY
    });

    $("#" + PLACEHOLDER_NAME).append(newStruct);
    jsPlumb.draggable(newStruct, {
        containment: 'parent'
    });
}

function makeProperty(parentId, name, type) {
    var id = parentId.selector.replace("#","") + ID_NAME_SEPERATOR + name + ID_NAME_SEPERATOR  + type;
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

function addSourceProperty(parentId, name, type) {
    jsPlumb.makeSource(makeProperty(parentId, name, type), {
        anchor:["Continuous", { faces:["right"] } ]
    });
}

function addTargetProperty(parentId, name, type) {
    jsPlumb.makeTarget(makeProperty(parentId, name, type), {
        maxConnections:1,
        anchor:["Continuous", { faces:[ "left"] } ],
        beforeDrop: function (params) {
            //Checks property types are equal
            return params.sourceId.split(ID_NAME_SEPERATOR)[2] == params.targetId.split(ID_NAME_SEPERATOR)[2];
        }
    });
}