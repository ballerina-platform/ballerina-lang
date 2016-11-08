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

define(['./message','./activation', './life-line', './processor', './message-link',
        './containable-processor-element', './diagram', './preview',
        './unit-processor', './complex-processor', './dynamic-containable-processor', './custom-processor'],
    function (MessageView, ActivationView, LifeLineView, ProcessorView, MessageLinkView,
              ContainableProcessorElementView, DiagramView, DiagramPreview,
              UnitProcessorView, ComplexProcessorView, DynamicContainableProcessorView, CustomProcessorView) {
        var views = {};
        views.MessageView = MessageView;
        views.ActivationView = ActivationView;
        views.LifeLineView = LifeLineView;
        views.ProcessorView = ProcessorView;
        views.MessageLinkView = MessageLinkView;
        views.ContainableProcessorElementView = ContainableProcessorElementView;
        views.DiagramView = DiagramView;
        views.DiagramPreview = DiagramPreview;
        views.UnitProcessorView = UnitProcessorView;
        views.ComplexProcessorView = ComplexProcessorView;
        views.DynamicContainableProcessorView = DynamicContainableProcessorView;
        views.CustomProcessorView = CustomProcessorView;
        return views;
});
