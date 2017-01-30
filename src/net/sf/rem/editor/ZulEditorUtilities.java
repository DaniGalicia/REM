/*
 *   REM - A NetBeans Module for ZK
 *   Copyright (C) 2006, 2007  Minjie Zha, Frederic Jean
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sf.rem.editor;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.openide.ErrorManager;

/**
 *
 * @author magic
 */
public class ZulEditorUtilities {
    
    /** The constant from XML editor
     */
    protected static int XML_ATTRIBUTE = 5;
    protected static int XML_TAG = 4;
    protected static int XML_ATTRIBUTE_VALUE = 7;
    protected static int XML_PI_CONTENT = 15;
    
    public static String END_LINE = System.getProperty("line.separator");
    
    /** Creates a new instance of ZulEditorUtilities */
    public ZulEditorUtilities() {
    }
    
    /**
     *  write zscript section to document.
     */
    public static int writeZscript(BaseDocument doc, int offset){
        int position = -1;
        StringBuffer appendText = new StringBuffer();
        appendText.append("<zscript><![CDATA[" + END_LINE + END_LINE);
        appendText.append("]]></zscript>");
        try {
            position = writeString(doc,appendText.toString(),offset);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return position;
    }
    
    /**
     *  write zscript section string to document.
     */
    private static int writeString(BaseDocument doc, String text, int offset) throws BadLocationException {
        int formatLength = 0;      
        try{
            doc.atomicLock();
           // offset = doc.getFormatter().indentLine(doc, offset);
            doc.insertString(Math.min(offset, doc.getLength()), text, null );
           // formatLength = doc.getFormatter().reformat(doc, offset, offset + text.length());
        }
        finally{
            doc.atomicUnlock();
        }
        
        int length = ("<zscript><![CDATA[" + END_LINE + END_LINE).length();
        return Math.min(offset + length - 1, doc.getLength());
    }
}
