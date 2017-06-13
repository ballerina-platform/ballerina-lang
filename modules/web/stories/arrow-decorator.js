/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import { storiesOf, action, linkTo } from '@kadira/storybook';
import CanvasDecorator from '../js/ballerina/components/canvas-decorator';
import ArrowDecorator from '../js/ballerina/components/arrow-decorator';
import components from '../js/ballerina/components/components';
import '../css/diagram/diagram.css';

const canvasBbox = {
    w: 500,
    h: 500,
};

storiesOf('ArrowDecorator', module)
  .add('default view', () => (
    <CanvasDecorator bBox={canvasBbox}>
      <ArrowDecorator start={{ x: 10, y: 10 }} end={{ x: 300, y: 10 }} />
    </CanvasDecorator>
    ))
  .add('angled arrow', () => (
    <CanvasDecorator bBox={canvasBbox}>
      <ArrowDecorator start={{ x: 0, y: 0 }} end={{ x: 320, y: 100 }} />
    </CanvasDecorator>
    ))
  .add('dashed arrow', () => (
    <CanvasDecorator bBox={canvasBbox}>
      <ArrowDecorator start={{ x: 10, y: 10 }} end={{ x: 300, y: 10 }} dashed />
    </CanvasDecorator>
    ));
