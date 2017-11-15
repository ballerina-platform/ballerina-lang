package org.ballerinalang.composer.service.workspace.app;

/**
 * Created by natasha on 11/15/17.
 */
public class StartBrowser {

    public static void startInDefaultBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {

            if (os.indexOf("win") >= 0) {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

            } else if (os.indexOf("mac") >= 0) {
                rt.exec("open " + url);

            } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
                rt.exec("xdg-open " + url);

            } else {
                return;
            }
        } catch (Exception e) {
            return;
        }
    }

}
