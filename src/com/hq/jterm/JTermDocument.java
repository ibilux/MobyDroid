package com.hq.jterm;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class JTermDocument extends DefaultStyledDocument implements CaretListener {

    private Caret caret;
    private int limit;

    public JTermDocument() {
        limit = getLength();
    }

    public void write(String text, MutableAttributeSet attrs) {
        try {
            insertString(getLength(), text, attrs);
            limit = getLength();
            caret.setDot(limit);
        } catch (BadLocationException e) {
        }
    }

    public void writeUser(String text, MutableAttributeSet attrs) {
        try {
            insertString(getLength(), text, attrs);
            caret.setDot(getLength());
        } catch (BadLocationException e) {
        }
    }

    public String getUserInput() {
        try {
            return getText(limit, getLength() - limit);
        } catch (BadLocationException e) {
            return "";
        }
    }

    public String read() throws BadLocationException {
        try {
            return getText(0, getLength());
        } catch (BadLocationException e) {
            return null;
        }
    }

    public void remove(int length) throws BadLocationException {
        super.remove(getLength() - length, length);
    }

    public void clear() throws BadLocationException {
        super.remove(0, getLength());
        limit = getLength();
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        if (offs < limit) {
            return;
        }
        super.remove(offs, len);
    }

    public void setCaret(Caret caret) {
        this.caret = caret;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isCursorValid() {
        return caret.getDot() >= limit;
    }

    public void makeCursorValid() {
        if (caret.getDot() < limit) {
            caret.setDot(limit);
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
    } // Moved to "MakeCursorValid" so that the user can still copy text
}
