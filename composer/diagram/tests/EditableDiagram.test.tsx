import * as React from 'react';
import { DiagramEditor } from '../src-ts/DiagramEditor';
import { create } from 'react-test-renderer';

test('DiagramEditor renders properly', () => {
    const component = create(
        <DiagramEditor />
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});