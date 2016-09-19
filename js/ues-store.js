(function () {

    var domain = ues.global.urlDomain || ues.global.userDomain;

    var assetsUrl = ues.utils.relativePrefix() + 'apis/assets';

    var store = (ues.store = {});

    store.asset = function (type, id, cb) {
        $.get(assetsUrl + '/' + id + '?' + (domain ? 'domain=' + domain + '&' : '') + 'type=' + type, function (data) {
            cb(false, data);
        }, 'json');
    };

    store.assets = function (type, paging, cb) {
        var query = 'type=' + type;
        query += domain ? '&domain=' + domain : '';

        if (paging) {
            query += paging.query ? '&query=' + paging.query : '';
            query += '&start=' + paging.start + '&count=' + paging.count;
        }

        $.get(assetsUrl + '?' + query, function (data) {
            cb(false, data);
        }, 'json');
    };
}());