import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { SamplesList } from './../src';
import getBBEs from './bbes';

function openSample(url: string) {
}

storiesOf('Button', module)
  .add("", () => (
    <SamplesList getSamples={getBBEs} openSample={openSample} />
  ));
  