WDIO Dot reporter [![Build Status](https://travis-ci.org/webdriverio/wdio-dot-reporter.svg?branch=master)](https://travis-ci.org/webdriverio/wdio-dot-reporter) [![Code Climate](https://codeclimate.com/github/webdriverio/wdio-dot-reporter/badges/gpa.svg)](https://codeclimate.com/github/webdriverio/wdio-dot-reporter) [![Test Coverage](https://codeclimate.com/github/webdriverio/wdio-dot-reporter/badges/coverage.svg?)](https://codeclimate.com/github/webdriverio/wdio-dot-reporter/coverage)
==========

> A WebdriverIO plugin to report in dot style.

![Dot Reporter](http://webdriver.io/images/dot.png "Dot Reporter")

## Installation

The easiest way is to keep `wdio-dot-reporter` as a devDependency in your `package.json`.

```json
{
  "devDependencies": {
    "wdio-dot-reporter": "~0.4.x"
  }
}
```

You can simple do it by:

```bash
npm install wdio-dot-reporter --save-dev
```

Instructions on how to install `WebdriverIO` can be found [here](http://webdriver.io/guide/getstarted/install.html).

## Configuration

Following code shows the default wdio test runner configuration. Just add `'dot'` as reporter
to the array.

```js
// wdio.conf.js
module.exports = {
  // ...
  reporters: ['dot'],
  // ...
};
```

----

For more information on WebdriverIO see the [homepage](http://webdriver.io).
