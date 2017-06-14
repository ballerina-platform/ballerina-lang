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
import StatementContainer from '../js/ballerina/components/statement-container';
import { components } from '../js/ballerina/components/components';
import '../css/diagram/diagram.css';

storiesOf('BlockStatementDecorator', module)
  .add('while box', () => {
      const whileStatement = React.createElement(components.BlockStatementDecorator,
          { bBox: { x: 0, y: 0, w: 200, h: 100 },
              title: 'while' }, null);
      const canvusbBox = { h: 800 };
      return (
        <CanvasDecorator bBox={canvusbBox}>
          <StatementContainer>
            {whileStatement}
          </StatementContainer>
        </CanvasDecorator>
      );
  })
  .add('if box', () => {
      const blockStatement = React.createElement(components.BlockStatementDecorator,
          { bBox: { x: 0, y: 0, w: 200, h: 100 },
              title: 'if' }, null);
      const canvusbBox = { h: 800 };
      return (
        <CanvasDecorator bBox={canvusbBox}>
          <StatementContainer>
            {blockStatement}
          </StatementContainer>
        </CanvasDecorator>
      );
  })
  .add('if box', () => {
      const ifst = React.createElement(components.BlockStatementDecorator,
          { bBox: { x: 0, y: 0, w: 200, h: 100 },
              title: 'if' }, null);
      const elsest = React.createElement(components.BlockStatementDecorator,
          { bBox: { x: 0, y: 100, w: 200, h: 100 },
              title: 'else' }, null);
      const canvusbBox = { h: 800 };
      return (
        <CanvasDecorator bBox={canvusbBox}>
          <StatementContainer>
            { [ifst, elsest]}
          </StatementContainer>
        </CanvasDecorator>
      );
  })
  .add('try box', () => {
      const tryst = React.createElement(components.BlockStatementDecorator,
          { bBox: { x: 0, y: 0, w: 200, h: 100 },
              title: 'try' }, null);
      const canvusbBox = { h: 800 };
      return (
        <CanvasDecorator bBox={canvusbBox}>
          <StatementContainer>
            {tryst}
          </StatementContainer>
        </CanvasDecorator>
      );
  });
