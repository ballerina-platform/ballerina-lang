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

define(['./child', './children', './message', './life-line', './processor', './message-point', './message-link',
        './unit-processor', './complex-processor', './dynamic-containable-processor',
       './custom-processor', './action-processor', './containable-processor-element', './containable-processor-elements', './resource', './service'],

    function (Child, Children, Message, Processor, LifeLine, MessagePoint, MessageLink,
              UnitProcessor, ComplexProcessor, DynamicContainableProcessor,
              CustomProcessor, ActionProcessor, ContainableProcessorElement, ContainableProcessorElements, Resource, Service) {

        var models = {};
        // set models
        models.Child = Child;
        models.Children = Children;
        models.Message = Message;
        models.LifeLine = LifeLine;
        models.Processor = Processor;
        models.MessagePoint = MessagePoint;
        models.MessageLink = MessageLink;
        models.UnitProcessor = UnitProcessor;
        models.ComplexProcessor = ComplexProcessor;
        models.DynamicContainableProcessor = DynamicContainableProcessor;
        models.ActionProcessor = ActionProcessor;
        models.CustomProcessor = CustomProcessor;
        models.ContainableProcessorElement = ContainableProcessorElement;
        models.ContainableProcessorElements = ContainableProcessorElements;
        models.Resource = Resource;
        models.Service = Service;

        return models;

    });
