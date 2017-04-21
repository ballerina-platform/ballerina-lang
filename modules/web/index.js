// provide global jquery object because wso2 theme expects it
window.$ = window.jQuery = require("jquery");
window.simulate = window.jQuerySimulate = require("./lib/jquery-simulate-1.0.0/jquery.simulate.js");
window.scope = window.location.pathname;


var Application = require('./js/main').default;
const config = require('./config').default;
var app = new Application(config);

app.render();
app.displayInitialView();
window.composer = app;
