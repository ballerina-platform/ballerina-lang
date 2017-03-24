# npm-install-package
[![NPM version][npm-image]][npm-url]
[![build status][travis-image]][travis-url]
[![Test coverage][codecov-image]][codecov-url]
[![Downloads][downloads-image]][downloads-url]
[![js-standard-style][standard-image]][standard-url]

Install an npm package.

## Installation
```sh
$ npm install npm-install-package
```

## Usage
```js
const npmInstallPackage = require('npm-install-package')

npmInstallPackage('cliclopts', opts, err => {
  if (err) throw err
})
```

With multiple installs, offline-first fetching and saving to dev dependencies:
```js
const npmInstallPackage = require('npm-install-package')

const devDeps = [ 'map-limit', 'minimist', 'cliclopts' ]
const opts = { saveDev: true, cache: true }
npmInstallPackage(devDeps, opts, err => {
  if (err) throw err
})
```

## API
### npmInstallPackage(dependencies, options, cb)
Install an array of dependencies. Opts can contain the following values:
- __save__: save a value to `dependencies`. Defaults to `false`
- __saveDev__: save a value to `devDependencies`. Defaults to `false`
- __cache__: attempt to get packages from the local cache first. Defaults to `false`
- __silent__: install packages silently without writing to stdout. Defaults to `false`

## License
[MIT](https://tldrlegal.com/license/mit-license)

[npm-image]: https://img.shields.io/npm/v/npm-install-package.svg?style=flat-square
[npm-url]: https://npmjs.org/package/npm-install-package
[travis-image]: https://img.shields.io/travis/yoshuawuyts/npm-install-package/master.svg?style=flat-square
[travis-url]: https://travis-ci.org/yoshuawuyts/npm-install-package
[codecov-image]: https://img.shields.io/codecov/c/github/yoshuawuyts/npm-install-package/master.svg?style=flat-square
[codecov-url]: https://codecov.io/github/yoshuawuyts/npm-install-package
[downloads-image]: http://img.shields.io/npm/dm/npm-install-package.svg?style=flat-square
[downloads-url]: https://npmjs.org/package/npm-install-package
[standard-image]: https://img.shields.io/badge/code%20style-standard-brightgreen.svg?style=flat-square
[standard-url]: https://github.com/feross/standard
