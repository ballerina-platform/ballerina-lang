import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { DiagramEditor } from './../src-ts/DiagramEditor';

storiesOf('Ballerina Diagram', module)
  .add("Editable Diagram", () => (
    <DiagramEditor />
  ));
  