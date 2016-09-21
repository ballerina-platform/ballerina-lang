
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
        propertyPane: '',
        dataObject:'',

        initialize: function () {
            //Initialize the editor
            JSONEditor.defaults.theme = 'bootstrap3';
            JSONEditor.defaults.iconlib = 'bootstrap3';

            $('#expand-image').click(function (){
                if ($('#property-container1').width() == 240) {
                    $('#property-container1').css("width", 20);
                    $('#editorBottom').css("width", "calc(100vw - 260px)");
                    $('#expand-image').attr("src", "images/leftarrow.svg");
                    $('#propertyPaneContainer').hide();
                    $('#propertySave').hide();
                } else {
                    $('#property-container1').css("width", 240);
                    $('#editorBottom').css("width", "calc(100vw - 480px)");
                    $('#expand-image').attr("src", "images/rightarrow.svg");
                    $('#propertyPaneContainer').show();
                    $('#propertySave').show();
                }
            });
        },

        onSaveImageClick: function() {
            if ($('#save-image').css('opacity') == 1) {
                //This need to be handled in generic way
                ppView.dataObject.set('title', propertyPane.getValue().Title);
                console.log('property pane save image clicked');
                $('#save-image').css({opacity: 0.4});
            }
        },

        getPropertyPane: function (schema, editableProperties, dataModel) {
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

            propertyPane.watch('root',function() {
                $("#save-image").css({ opacity: 1 });
                console.log('property pane values changed');
                //thisLifeline.set('title', pane.getValue().Title); //commented as this results recursive call and updated to the older value.
            });


            $('#save-image').click(this.onSaveImageClick);

            return propertyPane;
        },

        render: function () {
            var schema = this.model.attributes.schema;
            var editableProperties = this.model.attributes.editableProperties;
            updatePropertyPane(schema, editableProperties);
            //this.$el.html(pane);
            return this;
        }
    });

    views.PropertyPaneView = propertyPaneView;
    editor.Views = views;
    return editor;

}(Editor || {}));

