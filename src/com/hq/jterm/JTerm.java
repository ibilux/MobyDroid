package com.hq.jterm;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class JTerm {

    private final JTextPane jTermTextPane;
    private final JTermDocument jTermDoc;		// Holder of all text on the window
    private final JTermInputProcessor processor;	// Processor of input, as name implies.
    private final MutableAttributeSet defaultStyle;

    private final ArrayList<String> cmds = new ArrayList<>();        // List of previously run commands
    private String cmd = "";                                         // The current command being written, constantly being updated
    private int cmdNum = 0;                                          // The current command number, as referenced in "cmds," that the user is accessing, based on arrow keys.

    public JTerm(JTextPane jTextPane, JTermInputProcessor process, Color background, Color text, Font font) {
        super();
        jTermTextPane = jTextPane;
        processor = process;
        jTermDoc = new JTermDocument();
        jTermTextPane.setDocument(jTermDoc);

        jTermTextPane.setBackground(background);

        jTermTextPane.setCaretColor(text);
        jTermTextPane.addCaretListener(jTermDoc);
        jTermDoc.setCaret(jTermTextPane.getCaret());

        defaultStyle = jTermTextPane.getInputAttributes();
        StyleConstants.setFontFamily(defaultStyle, font.getFamily());
        StyleConstants.setFontSize(defaultStyle, font.getSize());
        StyleConstants.setItalic(defaultStyle, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(defaultStyle, (font.getStyle() & Font.BOLD) != 0);
        StyleConstants.setForeground(defaultStyle, text);
        jTermDoc.setCharacterAttributes(0, jTermDoc.getLength() + 1, defaultStyle, false);

        jTermTextPane.addKeyListener(new JTermKeyListener()); //catch tabs, enters, and up/down arrows for autocomplete and input processing
        jTermTextPane.addMouseListener(new JTermMouseListener());
    }

    /**
     * Clears the terminal window...
     */
    public void cls() {
        try {
            jTermDoc.clear();
        } catch (BadLocationException e) {
        }
    }

    public void write(String text, boolean doFocus) {
        jTermDoc.write(text, defaultStyle);
        if (doFocus) {
            //focus();
        }
    }

    public void writeUser(String text, boolean doFocus) {
        jTermDoc.writeUser(text, defaultStyle);
        if (doFocus) {
            //focus();
        }
    }

    public String read() {
        try {
            return jTermDoc.read();
        } catch (BadLocationException e) {
            return null;
        }
    }

    public void remove(int length) {
        try {
            jTermDoc.remove(length);
        } catch (BadLocationException e) {
        }
    }

    public void remove(int offset, int length) {
        try {
            jTermDoc.remove(offset, length);
        } catch (BadLocationException e) {
        }
    }

    private void process(String text) {
        processor.process(text, this);
    }

    private void doPop(MouseEvent e) {
        JTermPopUp menu = new JTermPopUp();
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private class JTermKeyListener extends KeyAdapter {

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == '\t') {
                //don't append autocomplete tabs to the document
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Is the cursor in a valid position?
            if (!jTermDoc.isCursorValid()) {
                jTermDoc.makeCursorValid();
            }

            // Is the screen focused on the proper line?
            //focus();
            //TAB -> AUTOCOMPLETE
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                e.consume();
                /*
                String input = jTermDoc.getUserInput().trim();
                ArrayList<String> completions = new ArrayList<>();

                if (completions.isEmpty()) {
                    //no completions
                    Toolkit.getDefaultToolkit().beep();
                } else if (completions.size() == 1) //only one match - print it
                {
                    String toInsert = completions.get(0);
                    toInsert = toInsert.substring(input.length());
                    writeUser(toInsert, true);
                    //don't trigger processing because the user might not agree with the autocomplete
                } else {
                    StringBuilder help = new StringBuilder();
                    help.append('\n');
                    completions.forEach((str) -> {
                        help.append(' ');
                        help.append(str);
                    });
                    jTermDoc.write(help.toString(), defaultStyle);
                    writeUser(input, true);
                }*/

            }

            //UP ARROW -> FILL IN A PREV COMMAND
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                e.consume(); //Don't actually go up a row

                //Get current input
                String currentInput = jTermDoc.getUserInput().trim();

                //If there's no previous commands, beep and return
                if (cmdNum <= 0) {
                    cmdNum = 0; //It should never be less than zero, but you never know...
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                //remove the current input from console and, if it's null, initialize it to an empty string.
                if (currentInput != null && !currentInput.isEmpty()) { //not sure which one it returns, but it doesn't really matter
                    remove(jTermDoc.getLimit(), currentInput.length());
                } else {
                    currentInput = "";                            //In case it's null... this may be unnecessary
                }

                //If it's something the user just typed, save it for later, just in case.
                if (cmdNum >= cmds.size()) {
                    cmdNum = cmds.size();
                    cmd = currentInput;      //save the current command, for down arrow use.
                }

                //move on to actually processing the command, now that all extraneous cases are taken care of.
                //based on previous checks, cmdNum should be in the range of 1 to cmds.size() before change.
                //after change, it should be in the range of 0 to (cmds.size() - 1), valid for indexing cmds.
                cmdNum--; //update command number. (lower num = older command)

                //Index cmds and write the replacement.
                String replacementCommand = cmds.get(cmdNum);
                writeUser(replacementCommand, true);

                //Similar to tab, don't trigger processing because the user might not agree with the autocomplete
            }

            //DOWN ARROW -> FILL IN A NEWER COMMAND
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                e.consume(); //pretty sure you can't go down, but if you can... don't.

                //If you've exhausted the list and replaced the line with the current command, beep and return
                if (cmdNum >= cmds.size()) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                cmdNum++;

                //Now, regardless of where you are in the list of commands, you're going to need to replace text.
                String currentInput = jTermDoc.getUserInput().trim();
                if (currentInput != null && !currentInput.isEmpty()) { //not sure which one it returns, but it doesn't really matter
                    remove(jTermDoc.getLimit(), currentInput.length());
                }

                //If you've exhausted the list but not yet replaced the line with the current command...
                if (cmdNum == cmds.size()) {
                    writeUser(cmd, true);
                    return;
                }

                //If, for some reason, the list is not in range (lower bound), make it in range.
                if (cmdNum < 0) {
                    cmdNum = 0;
                }

                //finally, write in the new command.
                writeUser(cmds.get(cmdNum), true);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                String input = jTermDoc.getUserInput();
                cmds.add(input.trim());
                cmdNum = cmds.size();
                process(input);
            }
        }
    }

    private class JTermMouseListener implements MouseListener {

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }
    }

    private class JTermPopUp extends JPopupMenu {

        JMenuItem copyButton;

        public JTermPopUp() {
            copyButton = new JMenuItem(new AbstractAction("copy") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedText = jTermTextPane.getSelectedText();
                    if (selectedText != null) // See if they selected something 
                    {
                        StringSelection selection = new StringSelection(selectedText);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                    }
                }
            });
            add(copyButton);
        }
    }
}
