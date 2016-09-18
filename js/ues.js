var ues = ues || {};
ues.plugins = {};
ues.global = {};

(function () {
    var relativePrefix = function () {
        var path = window.location.pathname;
        //.match(/.*(\/dashboards\/).+/ig) ? '../dashboards' : 'dashboards'
        var parts = path.split('/');
        var prefix = '';
        var i;
        var count = parts.length - 3;
        for (i = 0; i < count; i++) {
            prefix += '../';
        }
        return prefix;
    };

    var tenantPrefix = function () {
        var prefix = relativePrefix();
        var domain = ues.global.urlDomain;
        if (!domain) {
            return prefix;
        }
        return prefix + ues.global.tenantPrefix.replace(/^\//, '') + '/' + domain + '/';
    };

    ues.utils = {
        relativePrefix: relativePrefix,
        tenantPrefix: tenantPrefix
    };
}());