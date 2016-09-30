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

var MainElements = (function (mainElements) {
    var lifelines = mainElements.lifelines || {};

    var resourceLifeline = {
        id: "Resource",
        title: "Resource",
        icon: "images/icon1.png",
        colour : "#998844",
        dragCursorOffset : { left: 30, top: 40 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var group= view.createSVGForDraggable();
                group.attr("class", 'tool resource');
                group.draw.line(30, 10, 30, 60, group);
                group.draw.basicRect(0, 0, 60, 20, 0, 0, group);
                return group.getDraggableRoot();
            }
            return cloneCallBack;
        }
    };

    lifelines.ResourceLifeline = resourceLifeline;
    mainElements.lifelines = lifelines;

    return mainElements;
})(MainElements || {});