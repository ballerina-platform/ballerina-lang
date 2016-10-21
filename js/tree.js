var Tree = Backbone.Model.extend({
    // This attribute is useful to identify the model at debug time
    modelName: "Tree"
});


var TreeItem = Backbone.Model.extend({
    // This attribute is useful to identify the model at debug time
    modelName: "TreeItem",
    defaults: {
        isDir: false,
        /**@type {TreeItemList} */
        children: null
    },

    initialize: function () {
        var self = this;
        Backbone.Model.prototype.initialize.apply(this, arguments);
        var error = this.validate(this.attributes);
        if (error) {
            throw error;
        }

        if (this.attributes.isDir) {
            this.attributes.children.on("select", function (e) {
                self.trigger("select", e);
            })
        }
    },

    /**
     * @param {TreeItem.defaults} attributes
     * @returns {string}
     */
    validate: function (attributes) {
        if (!attributes.isDir && attributes.children && attributes.children.length > 0) {
            return "only dirs can have children";
        }
        if (attributes.isDir && !attributes.children) {
            return "dirs must have children of type TreeItemList";
        }
    }
});

var TreeItemList = Backbone.Collection.extend({
    model: TreeItem,
    // This attribute is useful to identify the model at debug time
    modelName: "TreeItemList"
});

var TreeItemView = Backbone.View.extend({
    tagName: "li",

    events: {
        "click .file": "triggerSelect",
        "click .branch": "toggle"
    },

    render: function () {
        var model = this.model;
        var $el = this.$el;
        $el.attr('aria-expanded', "true");
        $el.html("<span>" + model.get("name") + "</span>");
        if (model.get("isDir")) {
            var children = model.get("children");
            var listModel = {collection: children};
            $el.prepend('<i class="icon"></i>');
            $el.append(new TreeItemListView(listModel).render().el);
            $el.addClass("branch");
        } else {
            $el.addClass("file");
        }
        return this;
    },

    toggle: function (e) {
        var icon = $(e.currentTarget).children('i:first');
        var item = icon.closest('li');
        var attr = 'aria-expanded';
        if (item.attr(attr) == 'true') {
            item.attr(attr, 'false');
        } else {
            item.attr(attr, 'true');
        }
        this.model.trigger("viewChange", e);
        e.stopPropagation();
    },

    triggerSelect: function (e) {
        var path = "";
        var target = $(e.currentTarget);

        e.name = target.text();

        var parents = target.parentsUntil(".tree-view", "li");
        for (var i = 0; i < parents.length; i++) {
            var obj = parents[i];
            path = $(obj).children("span").text() + "/" + path;
        }
        e.path = path + e.name;
        this.model.trigger("select", e);
        e.stopPropagation();
    },


    startAdding: function () {
        // TODO: convert to Backbone
        // var removed = false;
        // $("#tree-add-api").on('click',function (e) {
        //     $tree.find("> li > ul").append("<li><input/></li>")
        //     removed = false;
        //     $tree.find('input').focus();
        // });
        // var addApi = function (e) {
        //     if(!removed){
        //         removed = true;
        //         var $input = $tree.find('input');
        //         $input.parent('li').remove();
        //         var name = $input.val();
        //         if(name != ""){
        //             $tree.find("> li > ul").append("<li>" + name + "</li>")
        //         }
        //     }
        // };
        // $tree.on("blur", "input", addApi);
        // $tree.on('keypress', function (e) {
        //     if (e.which === 13) {
        //         addApi(e)
        //     }
        // });
    }

});

var TreeItemListView = Backbone.View.extend({
    tagName: "ul",
    render: function () {
        var $el = this.$el;
        this.collection.each(function (list) {
            var item = new TreeItemView({model: list});
            $el.append(item.render().el);
        });

        return this;
    }
});

var TreeView = Backbone.View.extend({
    el: "#tree",

    render: function () {
        var $el = this.$el;
        var model = this.model;
        var rootModel = model.attributes.root;
        var root = new TreeItemView({model: rootModel}).render();
        //TODO: move style to tree.css
        $el.html('<div id="tree-highlight" style="background-color: #373737; position: absolute; width: 100%;"></div>');
        var $highlight = $el.children();
        $el.append(root.el);

        rootModel.on("select", function (e) {
            model.trigger("select", e);
            this.selectedEl = e.currentTarget;
            $highlight.offset({top: $(e.currentTarget).offset().top});
            $highlight.height(32);
            e.stopPropagation();
        });
        rootModel.on("viewChange", function (e) {
            if (this.selectedEl) {
                var top = $(this.selectedEl).offset().top;
                if (top == 0) {
                    $highlight.height(0);
                }
                $highlight.offset({top: top});
            }
        });
    }
});

