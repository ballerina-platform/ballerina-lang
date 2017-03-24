module.exports = {
  baseURL: 'https://selenium-release.storage.googleapis.com',
  version: '3.0.1',
  drivers: {
    chrome: {
      version: '2.28',
      arch: process.arch,
      baseURL: 'https://chromedriver.storage.googleapis.com'
    },
    ie: {
      version: '3.0.0',
      arch: process.arch,
      baseURL: 'https://selenium-release.storage.googleapis.com'
    },
    firefox: {
      version: '0.13.0',
      arch: process.arch,
      baseURL: 'https://github.com/mozilla/geckodriver/releases/download'
    }
  }
};
