/*
 * SpellCheckDemoScreen.java
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

package com.rim.samples.device.spellcheckdemo;

import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.StringProvider;

/**
 * The MainScreen class for our application. It displays a TestField instance to
 * allow the user to input words. The words can either be added to the
 * dictionary by 'learning' the word or the user may choose to spell check the
 * word.
 */
public class SpellCheckDemoScreen extends MainScreen {
    private final TestField _testField;
    private EditField _correction;
    private PopupScreen _popUp;
    private final SpellCheckDemo _app;

    /**
     * Creates a SpellCheckDemoScreen based on a SpellCheckDemo application.
     * 
     * @param app
     *            Reference to the SpellCheckDemo UiApplication.
     */
    public SpellCheckDemoScreen(final SpellCheckDemo app) {
        _app = app;

        // Add UI components to the screen.
        setTitle(new LabelField("Spell Check Demo", DrawStyle.ELLIPSIS
                | Field.USE_ALL_WIDTH));
        final RichTextField infoField =
                new RichTextField(
                        "Type a misspelled word into the test field (eg. blackbery).  Select menu items to perform spell check operations.",
                        Field.NON_FOCUSABLE);
        add(infoField);
        final SeparatorField separator = new SeparatorField();
        add(separator);
        _testField = new TestField("Test Field: ", "");
        add(_testField);

        _spellCheckItem =
                new MenuItem(new StringProvider("Spell check"), 0x230010, 1);
        _spellCheckItem.setCommand(new Command(new CommandHandler() {
            /**
             * Checks the spelling in the TestField.
             * 
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata,
             *      Object)
             */
            public void execute(final ReadOnlyCommandMetadata metadata,
                    final Object context) {
                if (_testField.getText().length() == 0) {
                    Dialog.alert("Test field cannot be empty");
                } else {
                    _app.spellCheck(_testField);
                }
            }
        }));

        _learnWordItem =
                new MenuItem(new StringProvider("Learn word"), 0x230020, 1);
        _learnWordItem.setCommand(new Command(new CommandHandler() {
            /**
             * Learns the word in the TestField.
             * 
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata,
             *      Object)
             */
            public void execute(final ReadOnlyCommandMetadata metadata,
                    final Object context) {
                if (_testField.getText().length() == 0) {
                    Dialog.alert("Test field cannot be empty");
                } else {
                    _app.learnWord(_testField.getText());
                }
            }
        }));

        _learnCorrectionItem =
                new MenuItem(new StringProvider("Learn correction"), 0x230030,
                        2);
        _learnCorrectionItem.setCommand(new Command(new CommandHandler() {
            /**
             * Shows the user a list of possible corrections for the word in the
             * TestField.
             * 
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata,
             *      Object)
             */
            public void execute(final ReadOnlyCommandMetadata metadata,
                    final Object context) {
                if (_testField.getText().length() == 0) {
                    Dialog.alert("Test field cannot be empty");
                } else {
                    final VerticalFieldManager vfm = new VerticalFieldManager();
                    _popUp = new PopupScreen(vfm);
                    final LabelField popUpLabel =
                            new LabelField("Correction for "
                                    + _testField.getText() + ":");
                    _correction = new EditField();
                    _popUp.add(popUpLabel);
                    _popUp.add(_correction);
                    final HorizontalFieldManager hfm =
                            new HorizontalFieldManager(Field.FIELD_HCENTER);
                    hfm.add(new OkButton());
                    hfm.add(new CancelButton());
                    _popUp.add(hfm);
                    _app.pushScreen(_popUp);
                }
            }
        }));
    }

    /**
     * Prevent the save dialog from being displayed.
     * 
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    public boolean onSavePrompt() {
        return true;
    }

    // Menu item classes -------------------------------------------------------

    /**
     * Menu item to invoke the spellCheck() method of the SpellCheckDemo.
     * application, passing in our TestField as the field to be spell checked.
     */
    private final MenuItem _spellCheckItem;

    /**
     * The run() method of this menu item calls the learnWord() method of the
     * SpellCheckDemo application, passing in the word specified in the
     * TestField.
     */
    private final MenuItem _learnWordItem;

    /**
     * This menu item displays a PopupScreen containing an EditField in which to
     * enter a correction for the word specified in the TestField.
     */
    private final MenuItem _learnCorrectionItem;

    /**
     * This inner class represents the OK button in our 'Learn correction'
     * PopupScreen.
     */
    private final class OkButton extends ButtonField {
        /**
         * Default constructor.
         */
        private OkButton() {
            super("OK", ButtonField.CONSUME_CLICK);
        }

        /**
         * @see Field#fieldChangeNotify(int)
         */
        protected void fieldChangeNotify(final int context) {
            if ((context & FieldChangeListener.PROGRAMMATIC) == 0) {
                if (_correction.getText().length() == 0) {
                    Dialog.alert("Correction field cannot be empty");
                    _correction.setFocus();
                } else {
                    _app.learnCorrection(_testField.getText(), _correction
                            .getText());
                    _popUp.close();
                }
            }
        }
    }

    /**
     * This inner class simply closes our 'Learn correction' PopupScreen.
     */
    private final class CancelButton extends ButtonField {
        /**
         * Default constructor.
         */
        private CancelButton() {
            super("Cancel", ButtonField.CONSUME_CLICK);
        }

        /**
         * @see Field#fieldChangeNotify(int)
         */
        protected void fieldChangeNotify(final int context) {
            if ((context & FieldChangeListener.PROGRAMMATIC) == 0) {
                _popUp.close();
            }
        }
    }

    /**
     * We are extending the EditField class in order to provide our customized
     * context menu.
     */
    private final class TestField extends EditField {
        /**
         * Construct a custom EditField.
         * 
         * @param label
         *            The TestField's label
         * @param initialValue
         *            The initial contents of the TestField
         */
        private TestField(final String label, final String initialValue) {
            super(label, initialValue);
        }

        /**
         * @see net.rim.device.api.ui.Field#makeContextMenu(ContextMenu
         *      contextMenu)
         */
        public void makeContextMenu(final ContextMenu contextMenu) {
            contextMenu.addItem(_spellCheckItem);
            contextMenu.addItem(_learnWordItem);
            contextMenu.addItem(_learnCorrectionItem);
        }

        /**
         * @see net.rim.device.api.ui.Field#getContextMenu()
         */
        public ContextMenu getContextMenu() {
            final ContextMenu contextMenu = ContextMenu.getInstance();
            contextMenu.setTarget(this);
            makeContextMenu(contextMenu);
            return contextMenu;
        }
    }
}
