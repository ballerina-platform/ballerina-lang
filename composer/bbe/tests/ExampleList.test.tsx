import * as React from 'react';
import { SamplesList } from '../src';
import { create } from 'react-test-renderer';
import getBBEs from './../stories/bbes';

test('SamplesList renders properly', () => {
    function openSample(url: string) {

    }
    var focusOnInputSpy = jest.fn();
    jest
        .spyOn(SamplesList.prototype, 'focusOnSearchInput')
        .mockImplementation(focusOnInputSpy);

    const component = create(
        <SamplesList getSamples={getBBEs} openSample={openSample} />
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
    expect(focusOnInputSpy).toHaveBeenCalled();
});