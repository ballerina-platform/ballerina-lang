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
define(
    ['processor_defs/flow_controllers/if_else_mediator',
    'processor_defs/flow_controllers/invoke_mediator',
    'processor_defs/flow_controllers/switch_mediator',
    'processor_defs/flow_controllers/try_block_mediator',

    'processor_defs/manipulators/fork_processor',
    'processor_defs/manipulators/header_processor',
    'processor_defs/manipulators/log_mediator',
    'processor_defs/manipulators/payload_factory_mediator',
    'processor_defs/manipulators/payload_processor'],
    function (ifElseMediator, invokeMediator, switchMediator, tryBlockMediator,
            forkProcessor, headerProcessor, logMediator, payloadFactoryMediator, payloadProcessor) {
        var flowControllers = {};
        var manipulators = {};

        flowControllers[ifElseMediator.id] = ifElseMediator;
        flowControllers[invokeMediator.id] = invokeMediator;
        flowControllers[switchMediator.id] = switchMediator;
        flowControllers[tryBlockMediator.id] = tryBlockMediator;

        manipulators[forkProcessor.id] = forkProcessor;
        manipulators[headerProcessor.id] = headerProcessor;
        manipulators[logMediator.id] = logMediator;
        manipulators[payloadFactoryMediator.id] = payloadFactoryMediator;
        manipulators[payloadProcessor.id] = payloadProcessor;

        return {
            flowControllers: flowControllers,
            manipulators: manipulators
        };
    }
);

