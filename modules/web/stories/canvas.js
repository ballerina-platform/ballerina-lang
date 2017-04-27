import React from 'react';
import { storiesOf, action, linkTo } from '@kadira/storybook';
import CanvasDecorator from '../js/ballerina/components/canvas-decorator';
import StatementContainer from '../js/ballerina/components/statement-container';
import components from '../js/ballerina/components/components';
import '../css/diagram/diagram.css';

storiesOf('CanvasDecorator', module)
  .add('default view', () => {
    const functionInvocation = React.createElement(components['FunctionInvocationStatement'],
        {model : { viewState: { bBox: { x: 0, y: 0, w: 200, h: 50}},
         expression: "this is the expression"}}, null);
    return (
      <CanvasDecorator>
          <StatementContainer>
                  {functionInvocation}
          </StatementContainer>
      </CanvasDecorator>
    );
  });
