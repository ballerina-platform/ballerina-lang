import 'mocha';
import { expect } from 'chai';

import foo from '../src/foo'

describe('Foo', () => {
  it('should work', () => {
    expect(typeof foo).to.equal('function');
    expect(foo('bar')).to.equal('Hello bar');
  });
});
