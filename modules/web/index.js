// provide global jquery object because wso2 theme expects it
window.$ = window.jQuery = require('jquery');
window.simulate = window.jQuerySimulate = require('./lib/jquery-simulate-1.0.0/jquery.simulate.js');
window.scope = window.location.pathname;


const Application = require('./js/main').default;
const config = require('./config').default;
const app = new Application(config);

if (!app.getLangserverClientController().initialized()) {
    waitForLangserverInitialization(app, () => {
        app.render();
        app.displayInitialView();
        window.composer = app;
    });
}

/**
 * Wait until the language server initialization completes
 * @param app
 * @param callback
 */
function waitForLangserverInitialization(app, callback) {
    setTimeout(
        () => {
            if (app.getLangserverClientController().initialized()) {
                console.log('Language Server Connection initialized...');
                callback();
            } else {
                console.log('Wait for Language Server Connection...');
                waitForLangserverInitialization(app, callback);
            }
        }, 100);
}
