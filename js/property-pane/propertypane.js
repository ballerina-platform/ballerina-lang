
var Editor = (function (editor) {
    var views = editor.Views || {};

    var propertyView = Backbone.Model.extend({
        initialize: function (attrs, options) {
        },

        modelName: "PropertyView",

        defaults: {
            schema: "",
            editableProperties: ""
        }
    });

    views.PropertyPaneModel = propertyView;
    editor.Views = views;
    return editor;
}(Editor || {}));


var Editor = (function (editor) {
    var views = editor.Views || {};

    var propertyPaneView = Backbone.View.extend({

        el:'#propertyPaneContainer',
        dataObject:'',

        initialize: function () {
            JSONEditor.defaults.theme = 'bootstrap3';
            JSONEditor.defaults.iconlib = 'bootstrap3';

            $('#expand-image').click(function (){
                if ($('#property-container1').width() == 240) {
                    $('#property-container1').css("width", 20);
                    $('#expand-image').attr("src", "images/leftarrow.svg");
                    $('#propertyPaneContainer').hide();
                } else {
                    $('#property-container1').css("width", 240);
                    $('#expand-image').attr("src", "images/rightarrow.svg");
                    $('#propertyPaneContainer').show();
                }
            });

           // $('#propertyPaneContainer').on('mouseleave', this.onSaveImageClick);
        },

        onSaveImageClick: function(event) {
            console.log('savee11');
            if(propertyPane.schema) {
                if (propertyPane.schema.title === "Lifeline") {
                    //TODO This need to be handled in generic way
                    console.log('lifeline');
                    ppView.dataObject.set('title', propertyPane.getValue().Title);
                } else if (propertyPane.schema.title === "Resource") {
                    diagram.attributes.path = propertyPane.editors['root.Path'].value;
                    diagram.attributes.get = propertyPane.editors['root.Get'].value;
                    diagram.attributes.put = propertyPane.editors['root.Put'].value;
                    diagram.attributes.post = propertyPane.editors['root.Post'].value;
                } else if (propertyPane.schema.title === "Log Mediator") {
                    console.log('save log');
                    // var models = selectedModel.__on[0].capture.collection.models;
                    //for(var j=0;j<models.length;j++) {
                    // if(models[j].cid === diagram.selectedNodeId) {
                    // console.log('cid'+models[j].cid);

                    // var processParameters = selectedModel.__on[0].capture.attributes.parameters.parameters;
                    //
                    // for(var i=0; i<processParameters.length; i++) {
                    //     var parameter = processParameters[i];
                    //     if(parameter.key === "message") {
                    //         parameter.value =  propertyPane.editors["root.Message"].value; //propertyPane.getValue().Title
                    //     } else if (parameter.key === "logLevel") {
                    //         parameter.value =  propertyPane.editors["root.LogLevel"].value;
                    //     } else if (parameter.key === "description") {
                    //         parameter.value =  propertyPane.editors["root.Description"].value;
                    //     }
                    // }

                    ppView.dataObject.parameters.parameters[0].value = propertyPane.getValue().Message;
                    ppView.dataObject.parameters.parameters[1].value = propertyPane.getValue().LogLevel;
                    ppView.dataObject.parameters.parameters[2].value = propertyPane.getValue().Description;
                    // ppView.dataObject.set('message', propertyPane.getValue.Message);

                    // break;
                    //  }
                    // }
                }
            }
           event.preventDefault();
            event.stopPropagation();
        },

        createPropertyPane: function (schema, editableProperties, dataModel) {
            this.dataObject = dataModel;
            if (propertyPane) {
                propertyPane.destroy();
            }
            propertyPane = new JSONEditor(document.getElementById("propertyPane"),{
                schema: schema,
                no_additional_properties: true,
                disable_properties:true,
                disable_edit_json:true
            });

            propertyPane.setValue(editableProperties);
            $('#propertyPaneContainer').on('mouseleave', this.onSaveImageClick);
            $('#save-image').click(this.onSaveImageClick);
            return propertyPane;
        },

        render: function () {
            var schema = this.model.attributes.schema;
            var editableProperties = this.model.attributes.editableProperties;
            return this;
        }
    });

    views.PropertyPaneView = propertyPaneView;
    editor.Views = views;
    return editor;

}(Editor || {}));

