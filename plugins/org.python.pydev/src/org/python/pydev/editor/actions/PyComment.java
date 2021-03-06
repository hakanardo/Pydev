/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * @author: fabioz
 * Created: January 2004
 */

package org.python.pydev.editor.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.actions.PyFormatStd.FormatStd;

import com.aptana.shared_core.string.FastStringBuffer;
import com.aptana.shared_core.structure.Tuple;

/**
 * Creates a bulk comment. Comments all selected lines
 * 
 * @author Fabio Zadrozny
 * @author Parhaum Toofanian
 */
public class PyComment extends PyAction {

    protected FormatStd std;

    public PyComment(FormatStd std) {
        super();
        this.std = std;
    }

    public PyComment() {
        this(null);
    }

    /**
     * Grabs the selection information and performs the action.
     */
    public void run(IAction action) {
        try {
            if (!canModifyEditor()) {
                return;
            }

            PyEdit pyEdit = getPyEdit();
            this.std = pyEdit.getFormatStd();

            // Select from text editor
            PySelection ps = new PySelection(pyEdit);
            // Perform the action
            Tuple<Integer, Integer> repRegion = perform(ps);

            // Put cursor at the first area of the selection
            pyEdit.selectAndReveal(repRegion.o1, repRegion.o2);
        } catch (Exception e) {
            beep(e);
        }
    }

    public Tuple<Integer, Integer> perform(PySelection ps) throws BadLocationException {
        return performComment(ps);
    }

    /**
     * Performs the action with a given PySelection
     * 
     * @param ps Given PySelection
     * @return the new selection
     * @throws BadLocationException 
     */
    protected Tuple<Integer, Integer> performComment(PySelection ps) throws BadLocationException {
        // What we'll be replacing the selected text with

        // If they selected a partial line, count it as a full one
        ps.selectCompleteLine();

        String selectedText = ps.getSelectedText();

        FastStringBuffer strbuf = commentLines(selectedText);
        ITextSelection txtSel = ps.getTextSelection();
        int start = txtSel.getOffset();
        int len = txtSel.getLength();

        String replacement = strbuf.toString();
        // Replace the text with the modified information
        ps.getDoc().replace(start, len, replacement);
        return new Tuple<Integer, Integer>(start, replacement.length());
    }

    protected FastStringBuffer commentLines(String selectedText) {
        List<String> ret = StringUtils.splitInLines(selectedText);

        FastStringBuffer strbuf = new FastStringBuffer(selectedText.length() + ret.size() + 2);
        String spacesInStartComment = null;
        if (this.std.spacesInStartComment > 0) {
            spacesInStartComment = StringUtils.createSpaceString(this.std.spacesInStartComment);
        }

        for (String line : ret) {
            strbuf.append('#');
            if (spacesInStartComment != null) {
                strbuf.append(spacesInStartComment);
            }
            strbuf.append(line);
        }
        return strbuf;
    }
}
