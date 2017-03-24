node-wgxpath
============

[Wicked Good XPath](https://github.com/google/wicked-good-xpath) is a fast implementation of [document.createExpression](https://developer.mozilla.org/en-US/docs/DOM/document.createExpression) and [document.evaluate](https://developer.mozilla.org/en-US/docs/DOM/document.evaluate) ([DOM3-XPath](http://www.w3.org/TR/2004/NOTE-DOM-Level-3-XPath-20040226/DOM3-XPath.html)) in pure Javascript.

Version
------

`x.y.z`: `x.y` refers to the Wicked Good XPath revision when `wgxpath.install.js` was built; `z` refers to any improvements to this package.

I'm pretty lazy, so I didn't build Wicked Good XPath myself. When the pre-compiled [wgxpath.install.js](https://github.com/google/wicked-good-xpath/releases/latest) is updated, I'll update this package.

Installation
------------

Install with [npm](http://npmjs.org/):

    npm install wgxpath

Make sure things are working:

    node node_modules/wgxpath/word_of_the_day.js

Example
-------

This example scrapes the [Merriam-Webster Word of the Day](http://www.merriam-webster.com/word-of-the-day/). This code can also be found in `word_of_the_day.js`.

```javascript
var wgxpath = require('wgxpath');
var jsdom = require('jsdom');

var url = 'http://www.merriam-webster.com/word-of-the-day/';
var expressionString = '//*[@id="content"]/div[3]/ul/li[1]/strong';

jsdom.env({
  html: url,
  done: function(errors, window) {
    wgxpath.install(window);
    var expression = window.document.createExpression(expressionString);
    var result = expression.evaluate(window.document,
        wgxpath.XPathResultType.STRING_TYPE);
    console.log('The Word of the Day is "' + result.stringValue + '."');
  }
});
```

