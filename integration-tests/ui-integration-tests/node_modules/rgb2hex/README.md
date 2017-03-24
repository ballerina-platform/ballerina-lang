rgb2hex [![Build Status](https://travis-ci.org/christian-bromann/rgb2hex.svg?branch=master)](https://travis-ci.org/christian-bromann/rgb2hex) [![Coverage Status](https://coveralls.io/repos/christian-bromann/rgb2hex/badge.png)](https://coveralls.io/r/christian-bromann/rgb2hex)
=======

[![Selenium Test Status](https://saucelabs.com/browser-matrix/rgb2hex.svg)](https://saucelabs.com/u/rgb2hex)

Parse any rgb or rgba string into a hex color. Lightweight library, no dependencies!


## Installation

via NPM:
```
$ npm install rgb2hex
```

via Bower
```
$ bower install rgb2hex
```

## Usage

Include `rgb2hex.js` in your web app, by loading it as usual:

```html
<script src="rgb2hex.js"></script>
```

### Using NodeJS

```js
var rgb2hex = require('rgb2hex');

console.log(rgb2hex('rgb(210,43,2525)'));
/**
 * returns:
 * {
 *    hex: '#d22bff',
 *    alpha: 1
 * }
 */

console.log(rgb2hex('rgba(12,173,22,.67)'));
/**
 * returns:
 * {
 *    hex: '#d22bff',
 *    alpha: 0.67
 * }
 */
```

### Using RequireJS

rgb2hex can be also loaded with AMD:

```js
require(['rgb2hex'], function (rgb2hex) {
    // ...
});
```

## Contributing
Please fork, add specs, and send pull requests! In lieu of a formal styleguide, take care to
maintain the existing coding style.

## Release History
* 2013-04-22   v0.1.0   first working version