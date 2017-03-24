var wgxpath = require('wgxpath');
var jsdom = require('jsdom');

var url = 'http://www.merriam-webster.com/word-of-the-day/';
var expressionString = '//*/div[@class="wod_headword"]';

jsdom.env({
  url: url,
  done: function(errors, window) {
    wgxpath.install(window);
    var expression = window.document.createExpression(expressionString);
    var result = expression.evaluate(window.document,
        wgxpath.XPathResultType.STRING_TYPE);
    if (result.stringValue.length <= 0)
      console.log('Failed to get the word of the day.');
    else
      console.log('The Word of the Day is "' + result.stringValue + '."');
  }
});
