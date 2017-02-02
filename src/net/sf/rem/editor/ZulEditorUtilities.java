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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author magic
 */
public class ZulEditorUtilities {

    /**
     * The constant from XML editor
     */
    protected static int XML_ATTRIBUTE = 5;
    protected static int XML_OPERATOR= 5;
    protected static int XML_TAG = 4;
    protected static int XML_ATTRIBUTE_VALUE = 7;
    protected static int XML_PI_CONTENT = 15;

    public static String END_LINE = System.getProperty("line.separator");

    /**
     * Creates a new instance of ZulEditorUtilities
     */
    public ZulEditorUtilities() {
    }

    /**
     * write zscript section to document.
     */
    public static int writeZscript(BaseDocument doc, int offset) {
        int position = -1;
        StringBuffer appendText = new StringBuffer();
        appendText.append("<zscript><![CDATA[" + END_LINE + END_LINE);
        appendText.append("]]></zscript>");
        try {
            position = writeString(doc, appendText.toString(), offset);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return position;
    }

    /**
     * write zscript section string to document.
     */
    private static int writeString(BaseDocument doc, String text, int offset) throws BadLocationException {
        int formatLength = 0;
        try {
            doc.atomicLock();
            // offset = doc.getFormatter().indentLine(doc, offset);
            doc.insertString(Math.min(offset, doc.getLength()), text, null);
            // formatLength = doc.getFormatter().reformat(doc, offset, offset + text.length());
        } finally {
            doc.atomicUnlock();
        }

        int length = ("<zscript><![CDATA[" + END_LINE + END_LINE).length();
        return Math.min(offset + length - 1, doc.getLength());
    }

    /**
     *
     * @param folder Directorio
     * @return Lista de archivos java contenidos en ese directorio
     */
    private static List<File> getJavaFileList(File folder) {
        List resultado = new ArrayList();
        if (!folder.exists() || !folder.isDirectory()) {
            return resultado;
        }

        for (File archivo : folder.listFiles()) {
            if (archivo.isFile()) {
                if (archivo.getName().endsWith(".java") || archivo.getName().endsWith(".JAVA")) {
                    resultado.add(archivo);
                }
            } else {
                resultado.addAll(getJavaFileList(archivo));
            }
        }

        return resultado;
    }

    /**
     * Llena la lista de archivos java contenidos en el proyecto
     */
    public static List<String> findClasses(String javaClassPath, boolean fullPath) {
        File folder = new File(javaClassPath);
        List<String> retorno = new ArrayList<String>();

        if (folder.getPath().endsWith("web")) {
            folder = new File(folder.getParent() + "/src/java/");
        }

        for (File file : getJavaFileList(folder)) {
            String name = file.getAbsolutePath();

            if (!fullPath) {
                String busq = "\\src\\java\\";
                int i;
                if (name.contains(busq)) {
                    i = name.indexOf(busq);
                    name = name.substring(i + busq.length());
                    name = name.replaceAll("\\\\", ".");
                }
            }
            retorno.add(name);
        }
        return retorno;
    }

    /**
     * @param doc documento en el cual se ha hecho click
     * @return FileObject relacionado con el documento que se hizo clic
     */
    public static FileObject getFO(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject) sdp;
        }
        if (sdp instanceof DataObject) {
            DataObject dobj = (DataObject) sdp;
            return dobj.getPrimaryFile();
        }
        return null;
    }
    
    public static int getPositionPoint(Document doc,int offset){
        return 0;
    }

}
