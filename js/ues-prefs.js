(function () {
    //Initializing opensocial gadget specific environment
    var getPrefs = function (site, url, done) {
        //TODO save userprefs in the registry
        console.log(Array.prototype.slice.call(arguments));
        done({
            username: 'ruchira.wageesha@gmail.com'
        });
    };

    var setPrefs = function (site, url, prefs) {
        console.log(Array.prototype.slice.call(arguments));
    };

    var configs = ues.configs || (ues.configs = {});
    var container = configs.container || (configs.container = {});

    //These callbacks will be used if and only if osapi.container.RenderParam.USER_PREFS is not specified
    // at container.navigateGadget
    container[osapi.container.ContainerConfig.GET_PREFERENCES] = getPrefs;
    container[osapi.container.ContainerConfig.SET_PREFERENCES] = setPrefs;

}());