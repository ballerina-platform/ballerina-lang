// provide global jquery object because wso2 theme expects it
window.$ = window.jQuery = require("jquery");

var Application = require('./js/main').default;
const config = require('./config').default;
var app = new Application(config);

app.render();
app.displayInitialView();
