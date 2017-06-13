function setActiveStyleSheet(title) {
    let i,
        a,
        main;
    for (i = 0; (a = document.getElementsByTagName('link')[i]); i++) {
        if (a.getAttribute('rel').indexOf('style') != -1 && a.getAttribute('title')) {
            a.disabled = true;
            if (a.getAttribute('title') == title) a.disabled = false;
        }
    }
}

function getActiveStyleSheet() {
    let i,
        a;
    for (i = 0; (a = document.getElementsByTagName('link')[i]); i++) {
        if (a.getAttribute('rel').indexOf('style') != -1 && a.getAttribute('title') && !a.disabled) return a.getAttribute('title');
    }
    return null;
}

function getPreferredStyleSheet() {
    let i,
        a;
    for (i = 0; (a = document.getElementsByTagName('link')[i]); i++) {
        if (a.getAttribute('rel').indexOf('style') != -1
       && a.getAttribute('rel').indexOf('alt') == -1
       && a.getAttribute('title')
       ) return a.getAttribute('title');
    }
    return null;
}

function createCookie(name, value, days) {
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = `; expires=${date.toGMTString()}`;
    } else expires = '';
    document.cookie = `${name}=${value}${expires}; path=/`;
}

function readCookie(name) {
    const nameEQ = `${name}=`;
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

window.onload = function (e) {
    const cookie = readCookie('style');
    const title = cookie || getPreferredStyleSheet();
    setActiveStyleSheet(title);
};
window.onunload = function (e) {
    const title = getActiveStyleSheet();
    createCookie('style', title, 365);
};
const cookie = readCookie('style');
const title = cookie || getPreferredStyleSheet();
setActiveStyleSheet(title);
