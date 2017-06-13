ace.define('ace/worker/mirror', ['require', 'exports', 'module', 'ace/range', 'ace/document', 'ace/lib/lang'], (require, exports, module) => {
    const Range = require('ace/range').Range;
    const Document = require('ace/document').Document;
    const lang = require('ace/lib/lang');

    const Mirror = exports.Mirror = function (sender) {
        this.sender = sender;
        const doc = this.doc = new Document('');

        const deferredUpdate = this.deferredUpdate = lang.delayedCall(this.onUpdate.bind(this));

        const _self = this;
        sender.on('change', (e) => {
            const data = e.data;
            if (data[0].start) {
                doc.applyDeltas(data);
            } else {
                for (let i = 0; i < data.length; i += 2) {
                    if (Array.isArray(data[i + 1])) {
                        var d = { action: 'insert', start: data[i], lines: data[i + 1] };
                    } else {
                        var d = { action: 'remove', start: data[i], end: data[i + 1] };
                    }
                    doc.applyDelta(d, true);
                }
            }
            if (_self.$timeout) { return deferredUpdate.schedule(_self.$timeout); }
            _self.onUpdate();
        });
    };

    (function () {
        this.$timeout = 500;

        this.setTimeout = function (timeout) {
            this.$timeout = timeout;
        };

        this.setValue = function (value) {
            this.doc.setValue(value);
            this.deferredUpdate.schedule(this.$timeout);
        };

        this.getValue = function (callbackId) {
            this.sender.callback(this.doc.getValue(), callbackId);
        };

        this.onUpdate = function () {
        };

        this.isPending = function () {
            return this.deferredUpdate.isPending();
        };
    }).call(Mirror.prototype);
});
