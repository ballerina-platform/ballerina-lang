import React from 'react';
import { storiesOf, action, linkTo } from '@kadira/storybook';
import CanvasDecorator from '../js/ballerina/components/canvas-decorator';
import ArrowDecorator from '../js/ballerina/components/arrow-decorator';
import components from '../js/ballerina/components/components';
import '../css/diagram/diagram.css';

storiesOf('ArrowDecorator', module)
  .add('default view', () => {
    return (
      <CanvasDecorator>
          <ArrowDecorator start={{x: 10, y: 10}} end={{x: 300, y: 10}}/>
      </CanvasDecorator>
    );
  })
  .add('angled arrow', () => {
    return (
      <CanvasDecorator>
          <ArrowDecorator start={{x: 0, y: 0}} end={{x: 320, y: 100}}/>
      </CanvasDecorator>
    );
  })
  .add('dashed arrow', () => {
    return (
      <CanvasDecorator>
          <ArrowDecorator start={{x: 10, y: 10}} end={{x: 300, y: 10}} dashed />
      </CanvasDecorator>
    );
  });
