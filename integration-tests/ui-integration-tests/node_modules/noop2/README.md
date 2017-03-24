# noop2
[![NPM version][npm-image]][npm-url]
[![build status][travis-image]][travis-url]
[![Test coverage][coveralls-image]][coveralls-url]
[![Downloads][downloads-image]][downloads-url]

No operation as a module™. The long awaited successor to [`noop`][noop1]. Noop2
does even less than its predecessor, and is 100% tested to guarantee nothing
happens.

## Installation
```bash
npm install noop2
```

## Usage
```js
var noop = require('noop2');

noop();
// nothing happened
```

## Why?
This is both a joke and actually useful. Obviously we _might_ be taking npm
a bit too far by bundling this as a module, but when running tests it's
sometimes nice to be able to import an empty function if only to keep style
consistent. So here you have it.

## See also
- [the original noop][noop1]
- [nop][nop]
- [no-op][no-op]

## License
[MIT](https://tldrlegal.com/license/mit-license)

[npm-image]: https://img.shields.io/npm/v/noop2.svg?style=flat-square
[npm-url]: https://npmjs.org/package/noop2
[travis-image]: https://img.shields.io/travis/yoshuawuyts/noop2.svg?style=flat-square
[travis-url]: https://travis-ci.org/yoshuawuyts/noop2
[coveralls-image]: https://img.shields.io/coveralls/yoshuawuyts/noop2.svg?style=flat-square
[coveralls-url]: https://coveralls.io/r/yoshuawuyts/noop2?branch=master
[downloads-image]: http://img.shields.io/npm/dm/noop2.svg?style=flat-square
[downloads-url]: https://npmjs.org/package/noop2

[noop1]: http://ghub.io/noop
[nop]: https://github.com/supershabam/nop
[no-op]: https://github.com/mattdesl/no-op
