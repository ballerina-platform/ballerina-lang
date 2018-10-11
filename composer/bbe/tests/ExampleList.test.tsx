import * as React from 'react';
import { SamplesList, BallerinaExampleCategory} from '../src';
import { create } from 'react-test-renderer';

test('SamplesList renders properly', () => {
    function getSamples() {
        return Promise.resolve(new Array<BallerinaExampleCategory>());
    }
    function openSample(url: string) {

    }
    const component = create(
        <SamplesList getSamples={getSamples} openSample={openSample} />
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});