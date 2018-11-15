import * as React from "react";
import { create } from "react-test-renderer";
import { SamplesList } from "../src";
import getBBEs from "./../stories/bbes";

test("SamplesList renders properly", () => {
    const focusOnInputSpy = jest.fn();
    jest
        .spyOn(SamplesList.prototype, "focusOnSearchInput")
        .mockImplementation(focusOnInputSpy);

    const component = create(
        <SamplesList getSamples={getBBEs} openSample={jest.fn()} />
    );
    const tree = component.toJSON();
    expect(tree).toMatchSnapshot();
    expect(focusOnInputSpy).toHaveBeenCalled();
});
