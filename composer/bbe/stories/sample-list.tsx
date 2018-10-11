import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { SamplesList, BallerinaExampleCategory} from './../src';

function getSamples() {
  return Promise.resolve(new Array<BallerinaExampleCategory>());
}
function openSample(url: string) {

}

storiesOf('Button', module)
  .add("", () => (
    <SamplesList getSamples={getSamples} openSample={openSample} />
  ));
  