
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
        },

        saveProperties: function(event) {

           // if(propertyPane.schema) {
                if (propertyPane.schema.title === "Lifeline") {

                    ppView.dataObject.set('title', propertyPane.getValue().Title);

                } else if (propertyPane.schema.title === "Resource") {

                    diagram.attributes.path = propertyPane.editors['root.Path'].value;
                    diagram.attributes.get = propertyPane.editors['root.Get'].value;
                    diagram.attributes.put = propertyPane.editors['root.Put'].value;
                    diagram.attributes.post = propertyPane.editors['root.Post'].value;

                } else if (propertyPane.schema.title === "Log Mediator") {
                    ppView.dataObject.parameters.parameters = [
                        {
                            key: "message",
                            value: propertyPane.getValue().Message
                        },
                        {
                            key: "logLevel",
                            value: propertyPane.getValue().LogLevel
                        },
                        {
                            key: "description",
                            value: propertyPane.getValue().Description
                        }
                    ];
                }
          //  }
         //  event.preventDefault();
         //   event.stopPropagation();
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

            $('#propertyPaneContainer').on('click', this.saveProperties);
            $('#propertyPane').find('input').on('change', this.saveProperties)
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

