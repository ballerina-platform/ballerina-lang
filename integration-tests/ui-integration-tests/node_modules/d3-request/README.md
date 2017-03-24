# d3-request

This module provides a convenient alternative to XMLHttpRequest. For example, to load a text file:

```js
d3.text("/path/to/file.txt", function(error, text) {
  if (error) throw error;
  console.log(text); // Hello, world!
});
```

To load and parse a CSV file:

```js
d3.csv("/path/to/file.csv", function(error, data) {
  if (error) throw error;
  console.log(data); // [{"Hello": "world"}, …]
});
```

To post some query parameters:

```js
d3.request("/path/to/resource")
    .header("X-Requested-With", "XMLHttpRequest")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .post("a=2&b=3", callback);
```

This module has built-in support for parsing [JSON](#json), [CSV](#csv) and [TSV](#tsv); in browsers, but not in Node, [HTML](#html) and [XML](#xml) are also supported. You can parse additional formats by using [request](#request) or [text](#text) directly.

## Installing

If you use NPM, `npm install d3-request`. Otherwise, download the [latest release](https://github.com/d3/d3-request/releases/latest). You can also load directly from [d3js.org](https://d3js.org), either as a [standalone library](https://d3js.org/d3-request.v1.min.js) or as part of [D3 4.0](https://github.com/d3/d3). AMD, CommonJS, and vanilla environments are supported. In vanilla, a `d3` global is exported:

```html
<script src="https://d3js.org/d3-collection.v1.min.js"></script>
<script src="https://d3js.org/d3-dispatch.v1.min.js"></script>
<script src="https://d3js.org/d3-dsv.v1.min.js"></script>
<script src="https://d3js.org/d3-request.v1.min.js"></script>
<script>

d3.csv("/path/to/file.csv", callback);

</script>
```

[Try d3-request in your browser.](https://tonicdev.com/npm/d3-request)

## API Reference

<a name="request" href="#request">#</a> d3.<b>request</b>(<i>url</i>[, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L4 "Source")

Returns a new *request* for specified *url*. If no *callback* is specified, the returned *request* is not yet [sent](#request_send) and can be further configured. If a *callback* is specified, it is equivalent to calling [*request*.get](#request_get) immediately after construction:

```js
d3.request(url)
    .get(callback);
```

If you wish to specify a request header or a mime type, you must *not* specify a callback to the constructor. Use [*request*.header](#request_header) or [*request*.mimeType](#request_mimeType) followed by [*request*.get](#request_get) instead. See [d3.json](#json), [d3.csv](#csv), [d3.tsv](#tsv), [d3.html](#html) and [d3.xml](#xml) for content-specific convenience constructors.

<a name="request_header" href="#request_header">#</a> <i>request</i>.<b>header</b>(<i>name</i>[, <i>value</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L51 "Source")

If *value* is specified, sets the request header with the specified *name* to the specified value and returns this request instance. If *value* is null, removes the request header with the specified *name* instead. If *value* is not specified, returns the current value of the request header with the specified *name*. Header names are case-insensitive.

Request headers can only be modified before the request is [sent](#request_send). Therefore, you cannot pass a callback to the [request constructor](#request) if you wish to specify a header; use [*request*.get](#request_get) or similar instead. For example:

```js
d3.request(url)
    .header("Accept-Language", "en-US")
    .header("X-Requested-With", "XMLHttpRequest")
    .get(callback);
```

Note: this library does not set the X-Requested-With header to `XMLHttpRequest` by default. Some servers require this header to mitigate unwanted requests, but the presence of the header triggers CORS preflight checks; if necessary, set this header before sending the request.

<a name="request_mimeType" href="#request_mimeType">#</a> <i>request</i>.<b>mimeType</b>([<i>type</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L60 "Source")

If *type* is specified, sets the request mime type to the specified value and returns this request instance. If *type* is null, clears the current mime type (if any) instead. If *type* is not specified, returns the current mime type, which defaults to null. The mime type is used to both set the ["Accept" request header](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html) and for [overrideMimeType](http://www.w3.org/TR/XMLHttpRequest/#the-overridemimetype%28%29-method), where supported.

The request mime type can only be modified before the request is [sent](#request_send). Therefore, you cannot pass a callback to the [request constructor](#request) if you wish to override the mime type; use [*request*.get](#request_get) or similar instead. For example:

```js
d3.request(url)
    .mimeType("text/csv")
    .get(callback);
```

<a name="request_user" href="#request_user">#</a> <i>request</i>.<b>user</b>([<i>value</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L80 "Source")

If *value* is specified, sets the user name for authentication to the specified string and returns this request instance. If *value* is not specified, returns the current user name, which defaults to null.

<a name="request_password" href="#request_password">#</a> <i>request</i>.<b>password</b>([<i>value</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L84 "Source")

If *value* is specified, sets the password for authentication to the specified string and returns this request instance. If *value* is not specified, returns the current password, which defaults to null.

<a name="request_timeout" href="#request_timeout">#</a> <i>request</i>.<b>timeout</b>([<i>timeout</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L74 "Source")

If *timeout* is specified, sets the [timeout](http://www.w3.org/TR/XMLHttpRequest/#the-timeout-attribute) attribute of the request to the specified number of milliseconds and returns this request instance. If *timeout* is not specified, returns the current response timeout, which defaults to 0.

<a name="request_responseType" href="#request_responseType">#</a> <i>request</i>.<b>responseType</b>([<i>type</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L68 "Source")

If *type* is specified, sets the [response type](http://www.w3.org/TR/XMLHttpRequest/#the-responsetype-attribute) attribute of the request and returns this request instance. Typical values are: `​` (the empty string), `arraybuffer`, `blob`, `document`, and `text`. If *type* is not specified, returns the current response type, which defaults to `​`.

<a name="request_response" href="#request_response">#</a> <i>request</i>.<b>response</b>(<i>value</i>) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L90 "Source")

Sets the response value function to the specified function and returns this request instance. The response value function is used to map the response XMLHttpRequest object to a useful data value. See the convenience methods [json](#json) and [text](#text) for examples.

<a name="request_get" href="#request_get">#</a> <i>request</i>.<b>get</b>([<i>data</i>][, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L96 "Source")

Equivalent to [*request*.send](#request_send) with the GET method:

```js
request.send("GET", data, callback);
```

<a name="request_post" href="#request_post">#</a> <i>request</i>.<b>post</b>([<i>data</i>][, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L101 "Source")

Equivalent to [*request*.send](#request_send) with the POST method:

```js
request.send("POST", data, callback);
```

<a name="request_send" href="#request_send">#</a> <i>request</i>.<b>send</b>(<i>method</i>[, <i>data</i>][, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L106 "Source")

Issues this request using the specified *method* (such as `GET` or `POST`), optionally posting the specified *data* in the request body, and returns this request instance. If a *callback* is specified, the callback will be invoked asynchronously when the request succeeds or fails. The callback is invoked with two arguments: the error, if any, and the [response value](#request_response). The response value is undefined if an error occurs. This is equivalent to:

```js
request
    .on("error", function(error) { callback(error); })
    .on("load", function(xhr) { callback(null, xhr); })
    .send(method, data);
```

If no *callback* is specified, then "load" and "error" listeners should be registered via [*request*.on](#request_on).

<a name="request_abort" href="#request_abort">#</a> <i>request</i>.<b>abort</b>() [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L121 "Source")

Aborts this request, if it is currently in-flight, and returns this request instance. See [XMLHttpRequest’s abort](http://www.w3.org/TR/XMLHttpRequest/#the-abort%28%29-method).

<a name="request_on" href="#request_on">#</a> <i>request</i>.<b>on</b>(<i>type</i>[, <i>listener</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/request.js#L126 "Source")

If *listener* is specified, sets the event *listener* for the specified *type* and returns this request instance. If an event listener was already registered for the same type, the existing listener is removed before the new listener is added. If *listener* is null, removes the current event *listener* for the specified *type* (if any) instead. If *listener* is not specified, returns the currently-assigned listener for the specified type, if any.

The type must be one of the following:

* `beforesend` - to allow custom headers and the like to be set before the request is [sent](#request_send).
* `progress` - to monitor the [progress of the request](http://www.w3.org/TR/progress-events/).
* `load` - when the request completes successfully.
* `error` - when the request completes unsuccessfully; this includes 4xx and 5xx response codes.

To register multiple listeners for the same *type*, the type may be followed by an optional name, such as `load.foo` and `load.bar`. See [d3-dispatch](https://github.com/d3/d3-dispatch) for details.

<a name="csv" href="#csv">#</a> d3.<b>csv</b>(<i>url</i>[[, <i>row</i>], <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/csv.js "Source")

Returns a new [*request*](#request) for the [CSV](https://github.com/d3/d3-dsv#csvParse) file at the specified *url* with the default mime type `text/csv`. If no *callback* is specified, this is equivalent to:

```js
d3.request(url)
    .mimeType("text/csv")
    .response(function(xhr) { return d3.csvParse(xhr.responseText, row); });
```

If a *callback* is specified, a [GET](#request_get) request is sent, making it equivalent to:

```js
d3.request(url)
    .mimeType("text/csv")
    .response(function(xhr) { return d3.csvParse(xhr.responseText, row); })
    .get(callback);
```

An optional *row* conversion function may be specified to map and filter row objects to a more-specific representation; see [*dsv*.parse](https://github.com/d3/d3-dsv#dsv_parse) for details. For example:

```js
function row(d) {
  return {
    year: new Date(+d.Year, 0, 1), // convert "Year" column to Date
    make: d.Make,
    model: d.Model,
    length: +d.Length // convert "Length" column to number
  };
}
```

The returned *request* exposes an additional *request*.row method as an alternative to passing the *row* conversion function to d3.csv, allowing you to configure the request before sending it. For example, this:

```js
d3.csv(url, row, callback);
```

Is equivalent to this:

```js
d3.csv(url)
    .row(row)
    .get(callback);
```

<a name="html" href="#html">#</a> d3.<b>html</b>(<i>url</i>[, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/html.js "Source")

Returns a new [*request*](#request) for the HTML file at the specified *url* with the default mime type `text/html`. The HTML file is returned as a [document fragment](https://developer.mozilla.org/en-US/docs/DOM/range.createContextualFragment). If no *callback* is specified, this is equivalent to:

```js
d3.request(url)
    .mimeType("text/html")
    .response(function(xhr) { return document.createRange().createContextualFragment(xhr.responseText); });
```

If a *callback* is specified, a [GET](#request_get) request is sent, making it equivalent to:

```js
d3.request(url)
    .mimeType("text/html")
    .response(function(xhr) { return document.createRange().createContextualFragment(xhr.responseText); })
    .get(callback);
```

HTML parsing requires a global document and relies on [DOM Ranges](https://dom.spec.whatwg.org/#ranges), which are [not supported by JSDOM](https://github.com/tmpvar/jsdom/issues/317) as of version 8.3; thus, this method is supported in browsers but not in Node.

<a name="json" href="#json">#</a> d3.<b>json</b>(<i>url</i>[, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/json.js "Source")

Returns a new [*request*](#request) to [get](#request_get) the [JSON](http://json.org) file at the specified *url* with the default mime type `application/json`. If no *callback* is specified, this is equivalent to:

```js
d3.request(url)
    .mimeType("application/json")
    .response(function(xhr) { return JSON.parse(xhr.responseText); });
```

If a *callback* is specified, a [GET](#request_get) request is sent, making it equivalent to:

```js
d3.request(url)
    .mimeType("application/json")
    .response(function(xhr) { return JSON.parse(xhr.responseText); })
    .get(callback);
```

<a name="text" href="#text">#</a> d3.<b>text</b>(<i>url</i>[, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/text.js "Source")

Returns a new [*request*](#request) to [get](#request_get) the text file at the specified *url* with the default mime type `text/plain`. If no *callback* is specified, this is equivalent to:

```js
d3.request(url)
    .mimeType("text/plain")
    .response(function(xhr) { return xhr.responseText; });
```

If a *callback* is specified, a [GET](#request_get) request is sent, making it equivalent to:

```js
d3.request(url)
    .mimeType("text/plain")
    .response(function(xhr) { return xhr.responseText; })
    .get(callback);
```

<a name="tsv" href="#tsv">#</a> d3.<b>tsv</b>(<i>url</i>[[, <i>row</i>], <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/tsv.js "Source")

Returns a new [*request*](#request) for a [TSV](https://github.com/d3/d3-dsv#tsvParse) file at the specified *url* with the default mime type `text/tab-separated-values`. If no *callback* is specified, this is equivalent to:

```js
d3.request(url)
    .mimeType("text/tab-separated-values")
    .response(function(xhr) { return d3.tsvParse(xhr.responseText, row); });
```

If a *callback* is specified, a [GET](#request_get) request is sent, making it equivalent to:

```js
d3.request(url)
    .mimeType("text/tab-separated-values")
    .response(function(xhr) { return d3.tsvParse(xhr.responseText, row); })
    .get(callback);
```

An optional *row* conversion function may be specified to map and filter row objects to a more-specific representation; see [*dsv*.parse](https://github.com/d3/d3-dsv#dsv_parse) for details. For example:

```js
function row(d) {
  return {
    year: new Date(+d.Year, 0, 1), // convert "Year" column to Date
    make: d.Make,
    model: d.Model,
    length: +d.Length // convert "Length" column to number
  };
}
```

The returned *request* exposes an additional *request*.row method as an alternative to passing the *row* conversion function to d3.tsv, allowing you to configure the request before sending it. For example, this:

```js
d3.tsv(url, row, callback);
```

Is equivalent to this:

```js
d3.tsv(url)
    .row(row)
    .get(callback);
```

<a name="xml" href="#xml">#</a> d3.<b>xml</b>(<i>url</i>[, <i>callback</i>]) [<>](https://github.com/d3/d3-request/blob/master/src/xml.js "Source")

Returns a new [*request*](#request) to [get](#request_get) the XML file at the specified *url* with the default mime type `application/xml`. If no *callback* is specified, this is equivalent to:

```js
d3.request(url)
    .mimeType("application/xml")
    .response(function(xhr) { return xhr.responseXML; });
```

If a *callback* is specified, a [GET](#request_get) request is sent, making it equivalent to:

```js
d3.request(url)
    .mimeType("application/xml")
    .response(function(xhr) { return xhr.responseXML; })
    .get(callback);
```

XML parsing relies on [*xhr*.responseXML](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/responseXML) which is not supported by [node-XMLHttpRequest](https://github.com/driverdan/node-XMLHttpRequest/issues/8) as of version 1.8; thus, this method is supported in browsers but not in Node.
