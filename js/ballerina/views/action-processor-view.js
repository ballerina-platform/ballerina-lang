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
define(['lodash', 'log', 'd3', 'jquery', 'd3utils', './point'],
    function (_, log, d3, $, D3utils, Point) {

        var ActionProcessorView = function(args){
            this._viewOptions = _.get(args, 'viewOptions', {});
            this._viewOptions.parent = _.get(args, "parent","undefined");
            this._viewOptions.width = _.get(args, "processorWidth","undefined");
            this._viewOptions.height = _.get(args, "processorHeight","undefined");
            this._viewOptions.centerPoint = _.get(args, "centerPoint", {});
            this._viewOptions.centerPoint.x = _.get(args, "centerPoint.x","undefined");
            this._viewOptions.centerPoint.y = _.get(args, "centerPoint.y","undefined");

            this._viewOptions.sourcePoint = _.get(args, "sourcePoint", {});
            this._viewOptions.sourcePoint.x = _.get(args, "sourcePoint.x", "undefined");
            this._viewOptions.sourcePoint.y = _.get(args, "sourcePoint.y", "undefined");

            this._viewOptions.destinationPoint = _.get(args, "destinationPoint", {});
            this._viewOptions.destinationPoint.x = _.get(args, "destinationPoint.x", "undefined");
            this._viewOptions.destinationPoint.y = _.get(args, "destinationPoint.y", "undefined");

            this._viewOptions.inArrow =  _.get(args, "inArrow","undefined");
            this._viewOptions.outArrow =  _.get(args, "outArrow","undefined");
            this._viewOptions.arrowX = _.get(args, "arrowX","undefined");
                this._viewOptions.arrowY =_.get(args, "arrowY","undefined");
            this._viewOptions.action = _.get(args, "action","undefined");


        };
        ActionProcessorView.prototype.constructor = ActionProcessorView;
       ActionProcessorView.prototype.createPoint = function(x,y){
               this._x = x || 0;
               this._y = y || 0;

       }

        ActionProcessorView.prototype.render = function () {
            //TODO: move css to classes
            var processorRect = D3utils.centeredRect(new Point(this._viewOptions.centerPoint.x,this._viewOptions.centerPoint.y),this._viewOptions.width
            ,this._viewOptions.height,0,0,this._viewOptions.parent);
            processorRect.attr("stroke-width", 1);
            processorRect.attr("stroke" , "#000000").attr("fill","white");
            var processorConnector = D3utils.line(this._viewOptions.sourcePoint.x,this._viewOptions.sourcePoint.y,this._viewOptions.destinationPoint.x,
                this._viewOptions.destinationPoint.y,this._viewOptions.parent).classed(" client line",true);
            processorConnector.attr("stroke","#000000");
            //TODO: center text
            var processorText = D3utils.textElement((this._viewOptions.centerPoint.x + 40 - this._viewOptions.width/2), (this._viewOptions.centerPoint.y + 20  - (this._viewOptions.height/2)) ,
                this._viewOptions.action,this._viewOptions.parent);
            processorText.attr('text-anchor', "start").attr("fill","#727272");

            if(this._viewOptions.inArrow){
                D3utils.inputTriangle( this._viewOptions.arrowX, this._viewOptions.arrowY,this._viewOptions.parent);
            }
            //TODO logic
            else if (this.viewOptions.outArrow){
                D3utils.inputTriangle( this._viewOptions.sourcePoint.x, this._viewOptions.sourcePoint.y,this._viewOptions.parent);
            }

        };
        return ActionProcessorView;

    });