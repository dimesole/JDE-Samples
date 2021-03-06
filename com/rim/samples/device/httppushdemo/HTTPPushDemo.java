/**
 * HTTPPushDemo.java
 *
 * Copyright � 1998-2011 Research In Motion Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.httppushdemo;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import net.rim.device.api.io.http.HttpServerConnection;
import net.rim.device.api.io.http.MDSPushInputStream;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.DataBuffer;

/**
 * The client side of a simple HTTP Push system. This application will listen
 * for data on the specified port and render the data when it arrives.
 */
public class HTTPPushDemo extends UiApplication {
    // Constants
    // ----------------------------------------------------------------
    private static final String URL = "http://:100"; // PORT 100.
    private static final int CHUNK_SIZE = 256;

    // Members
    // ------------------------------------------------------------------
    private ListeningThread _listeningThread;
    private HTTPPushDemoScreen _mainScreen;
    private RichTextField _infoField;
    private RichTextField _imageField;

    /**
     * Entry point for application.
     * 
     * @param args
     *            Command line arguments.
     */
    public static void main(final String[] args) {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        final HTTPPushDemo theApp = new HTTPPushDemo();
        theApp.enterEventDispatcher();
    }

    /**
     * Creates a new HTTPPushDemo object
     */
    public HTTPPushDemo() {
        // Make sure that the device is a simulator.
        // If it isn't display a dialog and exit the application.
        if (!DeviceInfo.isSimulator()) {
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    Dialog.alert("This application must be run on a simulator. Exiting application...");
                    System.exit(0);
                }
            });
        } else {
            _mainScreen = new HTTPPushDemoScreen();
            _mainScreen.setTitle(new LabelField("HTTP Push Demo",
                    Field.USE_ALL_WIDTH));

            _infoField = new RichTextField();
            _mainScreen.add(_infoField);

            _mainScreen.add(new SeparatorField());

            _imageField = new RichTextField();
            _mainScreen.add(_imageField);

            // Start the listening thread
            _listeningThread = new ListeningThread();
            _listeningThread.start();

            _infoField.setText("HTTP Listen object started");

            pushScreen(_mainScreen);
        }
    }

    // Inner Classes
    // ------------------------------------------------------------
    /**
     * This class implements a Thread object which trys to connect to a HTTP url
     * and retrieve the url's contents to render to the screen.
     */
    private class ListeningThread extends Thread {
        private boolean _stop = false;
        private StreamConnectionNotifier _notify;

        /**
         * Stops the thread from listening
         */
        private synchronized void stop() {
            _stop = true;

            if (_notify != null) {
                try {
                    _notify.close();
                } catch (final Exception e) {
                }
            }
        }

        /**
         * Listen for data from the HTTP url. After the data has been read,
         * render the data onto the screen.
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            // Wait for the app's event thread to start
            while (!HTTPPushDemo.this.hasEventThread()) {
                Thread.yield();
            }

            StreamConnection stream = null;
            InputStream input = null;
            MDSPushInputStream pushInputStream = null;

            try {
                _notify =
                        (StreamConnectionNotifier) Connector.open(URL
                                + ";deviceside=false");

                while (!_stop) {
                    // NOTE: the following will block until data is received
                    stream = _notify.acceptAndOpen();

                    try {
                        input = stream.openInputStream();
                        pushInputStream =
                                new MDSPushInputStream(
                                        (HttpServerConnection) stream, input);

                        // Extract the data from the input stream
                        final DataBuffer db = new DataBuffer();
                        byte[] data = new byte[CHUNK_SIZE];
                        int chunk = 0;

                        while (-1 != (chunk = input.read(data))) {
                            db.write(data, 0, chunk);
                        }

                        updateMessage(data);

                        // If the push server has application level reliabilty
                        // enabled, this method call will acknowledge receipt
                        // of the push.
                        pushInputStream.accept();

                        data = db.getArray();
                    } catch (final IOException ioe) {
                        // A problem occurred with the input stream , however,
                        // the original
                        // StreamConnectionNotifier is still valid.
                        errorDialog(ioe.toString());
                    } finally {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (final IOException ioe) {
                            }
                        }

                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (final IOException ioe) {
                            }
                        }
                    }
                }
            } catch (final IOException ioe) {
                errorDialog(ioe.toString());
            } finally {
                if (_notify != null) {
                    try {
                        _notify.close();
                        _notify = null;
                    } catch (final IOException e) {
                    }
                }
            }
            UiApplication.getUiApplication().invokeLater(new Runnable() {
                public void run() {
                    _mainScreen.close();
                }
            });
        }
    }

    /**
     * Updates the message currently displayed with new data.
     * 
     * @param data
     *            The data to display
     */
    private void updateMessage(final byte[] data) {
        Application.getApplication().invokeLater(new Runnable() {
            public void run() {
                // Query the user to load the received message
                final String[] choices = { "Ok", "Cancel" };

                if (0 != Dialog.ask(
                        "New message received. Do you want to render it?",
                        choices, 0)) {
                    return;
                }

                _infoField.setText("Text received - size:  " + data.length);

                try {
                    _imageField.setText(new String(data));
                } catch (final Exception e) {
                    errorDialog("RichTextField#setText(String) threw "
                            + e.toString());
                }
            }
        });
    }

    /**
     * Presents a dialog to the user with a given message
     * 
     * @param message
     *            The text to display
     */
    public static void errorDialog(final String message) {
        UiApplication.getUiApplication().invokeAndWait(new Runnable() {
            public void run() {
                Dialog.alert(message);
            }
        });
    }

    /**
     * This class acts as the MainScreen and calls the HTTPPushDemo.onExit()
     * when closing.
     */
    private class HTTPPushDemoScreen extends MainScreen {
        /**
         * @see net.rim.device.api.ui.Screen#close()
         */
        public void close() {
            _listeningThread.stop();

            super.close();
        }
    }
}
