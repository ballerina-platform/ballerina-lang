define(['lodash', 'd3', 'backbone'], function(_, d3, Backbone)
{
        var TextController = Backbone.Model.extend(

        /** @lends Text controller.prototype */
        {
            idAttribute: this.cid,
            modelName: "TextController",
            /**
             * @augments Backbone.Model
             * @constructs
             * @class Handles text expand/change of elements
             */
            initialize: function (attrs, options) {
                this.dynamicRectWidth();
                this.dynamicTextPosition();
                //set this to true when adding parent elements
                this.hasParent = false;
                this.parentObject();
            },
            textChanged: function (length) {
                id = this.cid;
                var rects = d3.selectAll("[id=" + id + "]").filter(".genericR");
                var texts = d3.selectAll("[id=" + id + "]").filter(".genericT");

                var computedWidth;
                var finalTextWidth;

                var minimumValue = 130;
                var dynamic = length;
                var rectX = rects.attr('x');


                // TODO: add methods to store these in TextController for future use
                var rectHeight = rects.attr('height');
                var textYPosition = texts.attr('y');

                // storing rect width and text 'x' position in textmodel
                if (dynamic < minimumValue) {
                    this.dynamicRectWidth(minimumValue);
                    computedWidth = (minimumValue / 2);
                    finalTextWidth = parseFloat(rectX) + parseFloat(computedWidth);
                    this.dynamicTextPosition(finalTextWidth);
                    rects.attr('width', function () { return minimumValue});
                } else {
                    this.dynamicRectWidth(dynamic);
                    computedWidth = (dynamic / 2);
                    finalTextWidth = parseFloat(rectX) + parseFloat(computedWidth);
                    this.dynamicTextPosition(finalTextWidth);
                    rects.attr('width', function () {return dynamic;});
                }

                // setting text element position on change
                texts.attr('x', function () {
                    return finalTextWidth;
                });

            },
            //keep the current width of the rectangle
            dynamicRectWidth: function (length) {
                if (_.isUndefined(length)) {
                    return this.get('dynamicRectWidth');
                } else {
                    this.set('dynamicRectWidth', length);
                }
            },
            //keep the current x position of the text element
            dynamicTextPosition: function (xPos) {
                if (_.isUndefined(xPos)) {
                    return this.get('dynamicTextPosition');
                } else {
                    this.set('dynamicTextPosition', xPos);
                }
            },
            // When a parent object needs notification add here : TODO: store list of parents
            parentObject: function (parent) {
                if (_.isUndefined(parent)) {
                    return this.get('parentObject');
                } else {
                    this.set('parentObject', parent);
                }
            }
        });
        return TextController;
});