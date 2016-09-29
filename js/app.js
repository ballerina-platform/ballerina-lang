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

var lifeLineOptions = {};
lifeLineOptions.class = "lifeline";
// Lifeline rectangle options
lifeLineOptions.rect = {};
lifeLineOptions.rect.width = 100;
lifeLineOptions.rect.height = 30;
lifeLineOptions.rect.roundX = 20;
lifeLineOptions.rect.roundY = 20;
lifeLineOptions.rect.class = "lifeline-rect";

// Lifeline middle-rect options
lifeLineOptions.middleRect = {};
lifeLineOptions.middleRect.width = 100;
lifeLineOptions.middleRect.height = 700;
lifeLineOptions.middleRect.roundX = 1;
lifeLineOptions.middleRect.roundY = 1;
lifeLineOptions.middleRect.class = "lifeline-middleRect";

// Lifeline options
lifeLineOptions.line = {};
lifeLineOptions.line.height = 700;
lifeLineOptions.line.class = "lifeline-line";
// Lifeline text options
lifeLineOptions.text = {};
lifeLineOptions.text.class = "lifeline-title";

var createPoint = function (x, y) {
    return new GeoCore.Models.Point({'x': x, 'y': y});
};

var createLifeLine = function (title, center, colour) {
    return new SequenceD.Models.LifeLine({title: title, centerPoint: center, colour: colour});
};

var createFixedSizedMediator = function (title, center) {
    return new SequenceD.Models.FixedSizedMediator({title: title, centerPoint: center});
};

var createMessage = function (start, end) {
    return new SequenceD.Models.Message({source: start, destination: end});
};


// Create tool palette elements
//var lifeline = new Tools.Models.Tool({
//    id: "LifeLine",
//    title: "Lifeline",
//    icon: "images/icon1.png",
//    dragCursorOffset : { left: 30, top: 40 },
//    createCloneCallback : function(view){
//        function cloneCallBack() {
//            var svgRoot = view.createSVGForDraggable();
//            var line = svgRoot.draw.line(30, 10, 30, 60, svgRoot).attr("class", 'lifeline-tool-line');
//            var rect = svgRoot.draw.basicRect(0, 0, 60, 20, 0, 0, svgRoot).attr("class", 'lifeline-tool-rect');
//            return svgRoot.getDraggableRoot();
//        }
//        return cloneCallBack;
//    },
//});


// Create main tool group
var mainToolGroup = new Tools.Models.ToolGroup();
//mainToolGroup.add(lifeline);

for (var lifeline in MainElements.lifelines) {
    var tool = new Tools.Models.Tool(MainElements.lifelines[lifeline]);
    mainToolGroup.add(tool);
}

var mainToolGroupWrapper = new Tools.Models.ToolGroupWrapper({
    toolGroupName: "Main Elements",
    toolGroupID: "main-tool-group",
    toolGroup: mainToolGroup
});

// Create mediators tool group
var mediatorsToolGroup = new Tools.Models.ToolGroup();
for (var manipulator in Processors.manipulators) {
    var tool = new Tools.Models.Tool(Processors.manipulators[manipulator]);
    mediatorsToolGroup.add(tool);
}
for (var flowController in Processors.flowControllers) {
    var tool = new Tools.Models.Tool(Processors.flowControllers[flowController]);
    mediatorsToolGroup.add(tool);
}
var mediatorsToolGroupWrapper = new Tools.Models.ToolGroupWrapper({
    toolGroupName: "Mediators",
    toolGroupID: "mediators-tool-group",
    toolGroup: mediatorsToolGroup
});

// Create tool palette
var toolPalette = new Tools.Models.ToolPalatte();
toolPalette.add(mainToolGroupWrapper);
toolPalette.add(mediatorsToolGroupWrapper);
var paletteView = new Tools.Views.ToolPalatteView({collection: toolPalette});
paletteView.render();

// Create the model for the diagram
var diagram = new Diagrams.Models.Diagram({});

// Create the diagram view
var diagramOptions = {selector: '.editor'};
var diagramView = new Diagrams.Views.DiagramView({model: diagram, options: diagramOptions});
diagramView.render();
var diagramViewElements = [];

lifeLineOptions.diagram = diagram;

// var lifeline1 = createLifeLine("LifeLine1",createPoint(250, 50));
// diagram.addElement(lifeline1, lifeLineOptions);
// var lifeline2 = createLifeLine("LifeLine2",createPoint(500, 50));
// diagram.addElement(lifeline2, lifeLineOptions);
// var lifeline3 = createLifeLine("LifeLine3",createPoint(750, 50));
// diagram.addElement(lifeline3, lifeLineOptions);

// var lf1Activation1 = new SequenceD.Models.Activation({owner:lifeline1});
// var lf2Activation1 = new SequenceD.Models.Activation({owner:lifeline2});
// var lf3Activation1 = new SequenceD.Models.Activation({owner:lifeline3});

// var messageOptions = {'class':'message'};
// var msg1 = new SequenceD.Models.Message({source: lf1Activation1, destination: lf3Activation1});
// diagram.addElement(msg1, messageOptions);
// var msg2 = new SequenceD.Models.Message({source: lf2Activation1, destination: lf3Activation1});
// diagram.addElement(msg2, messageOptions);
// var msg3 = new SequenceD.Models.Message({source: lf3Activation1, destination: lf1Activation1});
// diagram.addElement(msg3, messageOptions);
// var msg4 = new SequenceD.Models.Message({source: lf3Activation1, destination: lf2Activation1});
// diagram.addElement(msg4, messageOptions);
// var msg5 = new SequenceD.Models.Message({source: lf3Activation1, destination: lf1Activation1});
// diagram.addElement(msg5, messageOptions);
selected = "";
selectedModel = "";
var udcontrol = new Dialogs.Controls.UpdateDeleteControler({visible: false});
var udcontrolView = new Dialogs.Views.UpdateDeletedControlerView({model: udcontrol});
udcontrolView.render();

//var ppModel = new Editor.Views.PropertyPaneModel();
var ppView = new Editor.Views.PropertyPaneView();
propertyPane = ''; //ppView.createPropertyPane(schema, properties);
endpointLifelineCounter = 0;
resourceLifelineCounter = 0;

function TreeNode (value, type,cStart, cEnd) {
    this.object = undefined;
    this.children = [];
    this.value = value;
    this.type = type;
    this.configStart = cStart;
    this.configEnd = cEnd;

    this.getChildren = function () {
        return this.children;
    };

    this.getValue = function () {
        return this.value;
    };
}

// defining the constants such as the endpoints, this variable need to be positioned properly when restructuring
// This is a map of constants as --> constantType: constantValue
// Ex: HttpEP: "http://localhost/test/test2"
var definedConstants = {};
