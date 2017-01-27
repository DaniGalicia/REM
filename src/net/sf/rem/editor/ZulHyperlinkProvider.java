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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import net.sf.rem.loaders.ZulDataObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Provide code hyperlink for zul files. 
 * @author magic
 */
public class ZulHyperlinkProvider implements HyperlinkProvider {

    private static Hashtable<String, Integer> hyperlinkTable;
    private final int JAVA_CLASS = 0;
    private String javaClassPath;
    private final int RESOURCE_PATH = 2;

    {
        hyperlinkTable = new Hashtable<String, Integer>();
        hyperlinkTable.put("apply", JAVA_CLASS);
        hyperlinkTable.put("use", new Integer(JAVA_CLASS));
        hyperlinkTable.put("image", new Integer(RESOURCE_PATH));
        hyperlinkTable.put("src", new Integer(RESOURCE_PATH));
        hyperlinkTable.put("href", new Integer(RESOURCE_PATH));
        hyperlinkTable.put("macro-uri", new Integer(RESOURCE_PATH));
        hyperlinkTable.put("class", new Integer(JAVA_CLASS));
        hyperlinkTable.put("zscript", new Integer(RESOURCE_PATH));
        hyperlinkTable.put("uri", new Integer(RESOURCE_PATH));
    }
    private int valueOffset;
    private String[] av = null;

    /** Creates a new instance of ZulHyperlinkProvider */
    public ZulHyperlinkProvider() {
    }

    public boolean isHyperlinkPoint(Document doc, int offset) {
        DataObject dObject = NbEditorUtilities.getDataObject(doc);
        if (!(dObject instanceof ZulDataObject)) {
            return false;
        }

        av = getAttrValue(doc, offset);
        if (av != null) {
            if (hyperlinkTable.get(av[0]) != null) {
                return true;
            }
        }
        return false;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (av != null) {
            return new int[]{valueOffset, valueOffset + av[1].length() - 1};
        }
        return null;
    }

    public void performClickAction(Document doc, int offset) {
                       FileObject foClass = getFO(doc); //GlobalPathRegistry.getDefault().findResource("/");
                ClassPath sourcePath = ClassPath.getClassPath(foClass, ClassPath.SOURCE);
                
                for(FileObject root:sourcePath.getRoots()){
                    if(root.getPath().endsWith("java")){
                        javaClassPath=root.getPath();
                    }
                }
               
        if (hyperlinkTable.get(av[0]) != null) {
            int type = ((Integer) hyperlinkTable.get(av[0])).intValue();
            switch (type) {
                case JAVA_CLASS:
                    //FileObject foClass = GlobalPathRegistry.getDefault().findResource("/");
                    String oldAttrValue = av[1];
                    
                    oldAttrValue = oldAttrValue.replace("${","");
                    oldAttrValue = oldAttrValue.replace("}", "");
                    oldAttrValue = oldAttrValue.substring(0,1).toUpperCase() + oldAttrValue.substring(1);
                    oldAttrValue = oldAttrValue.replace("/", ".");
                    List<String> archivos = getJavaFiles(javaClassPath,oldAttrValue +".java");
         
                    if(!archivos.isEmpty()){
                                            
                    String clase = archivos.get(0).replace("\\",".");
                    clase =clase.replace(".java", "");
           
                        av[1]=clase;
                    }             
                    findJavaClass(av[1], doc);
                    break;
                case RESOURCE_PATH:
                    findResourcePath(av[1], (BaseDocument) doc);
                    break;
            }
        }
    }

    private String[] getAttrValue(Document doc, int offset) {
        String attribute = null;
        String value = null;

        BaseDocument bdoc = (BaseDocument) doc;
        JTextComponent target = Utilities.getFocusedComponent();

        if (target == null || target.getDocument() != bdoc) {
            return null;
        }

        ExtSyntaxSupport sup = (ExtSyntaxSupport) bdoc.getSyntaxSupport();
        try {
            TokenItem token = sup.getTokenChain(offset, offset + 1);

            // For "macro-uri","class","zscript","uri","href" in PI
            if (token != null && token.getTokenID().getNumericID() == ZulEditorUtilities.XML_PI_CONTENT) {

                final String[] ATTRS = new String[]{
                    "macro-uri", // "macro-uri" in <?component ?>
                    "class", // "class" in <?component ?>, <?init ?> or <?variable-resolver ?>
                    "zscript", // "zscript" in <?init ?>
                    "uri", // "uri" in <?import ?>
                    "href"
                , // "href" in <?link ?> 
                   };
                
                String content = token.getImage().trim();
                int index = -1;
                for (String attr : ATTRS) {
                    if ((index = content.indexOf(attr)) >= 0) {
                        attribute = attr;
                        int startIndex = -1;
                        int endIndex = -1;
                        if ((startIndex = content.indexOf('"', index + 1)) >= 0) {
                            valueOffset = token.getOffset() + startIndex + 1;
                            if ((endIndex = content.indexOf('"', startIndex + 1)) >= 0) {
                                if (startIndex < (offset - token.getOffset()) && (offset - token.getOffset()) < endIndex) {
                                    value = content.substring(startIndex + 1, endIndex).trim();
                                    //System.out.println("attr:" + attribute);
                                    //System.out.println("value:" + value);
                                    return new String[]{attribute, value};
                                }
                            }
                        }
                    }
                }
                return null;
            }

            if (token == null || token.getTokenID().getNumericID() != ZulEditorUtilities.XML_ATTRIBUTE_VALUE) {
                return null;
            }

            // Find value
            value = token.getImage();
            //System.out.println("value:" + value);
            if (value != null) {
                value = value.trim();
                valueOffset = token.getOffset();
                if (value.charAt(0) == '"') {
                    value = value.substring(1);
                    valueOffset++;
                }

                if (value.length() > 0 && value.charAt(value.length() - 1) == '"') {
                    value = value.substring(0, value.length() - 1);
                }
                value = value.trim();
            }

            // Find attribute
            while (token != null && token.getTokenID().getNumericID() != ZulEditorUtilities.XML_ATTRIBUTE) {
                token = token.getPrevious();
            }
            if (token != null && token.getTokenID().getNumericID() == ZulEditorUtilities.XML_ATTRIBUTE) {
                attribute = token.getImage();
            }

            if (attribute == null) {
                return null;
            }

            //System.out.println("attr:" + attribute);
            //System.out.println("value:" + value);
            return new String[]{attribute, value};
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void findJavaClass(String fqn, Document doc) { 
        OpenJavaClassThread run = new OpenJavaClassThread(fqn, (BaseDocument) doc);
        RequestProcessor.getDefault().post(run);
    }

    private void findResourcePath(String path, BaseDocument doc) {
        path = path.trim();
        if (path.indexOf('?') > 0) {
            path = path.substring(0, path.indexOf('?'));
        }
        WebModule wm = WebModule.getWebModule(NbEditorUtilities.getFileObject(doc));
        if (wm != null) {
            FileObject docBase = wm.getDocumentBase();
            FileObject fo = docBase.getFileObject(path);
            if (fo != null) {
                openInEditor(fo);
            } else {
                String key = "goto_resource_not_found"; // NOI18N
                String msg = NbBundle.getBundle(ZulHyperlinkProvider.class).getString(key);
                org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{path}));
            }
        }
    }

    private void openInEditor(FileObject fo) {
        if (fo != null) {
            DataObject dobj = null;
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().notify(e);
                return;
            }
            if (dobj != null) {
                Node.Cookie cookie = dobj.getCookie(OpenCookie.class);
                if (cookie != null) {
                    ((OpenCookie) cookie).open();
                }
            }
        }
    }

    private class OpenJavaClassThread implements Runnable {

        private String fqn;
        private BaseDocument doc;

        public OpenJavaClassThread(String name, BaseDocument doc) {
            super();
            this.fqn = name;
            this.doc = doc;
        }

        public void run() {
            FileObject foClass = GlobalPathRegistry.getDefault().findResource(fqn.replaceAll("\\.", "/")+".java");
         
            if (foClass != null) {
                openInEditor(foClass);
            } else {
                String key = "goto_resource_not_found"; // NOI18N
                String msg = NbBundle.getBundle(ZulHyperlinkProvider.class).getString(key);
                org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{fqn}));
            }

        }
    }
    
    //Buscando los archivos java
    private static List<File> getJavaFileList(String ruta,String filtro){
        List resultado = new ArrayList();
        File folder = new File(ruta);
        if(!folder.exists() || !folder.isDirectory())
            return resultado;
        
        for(File archivo:folder.listFiles()){
            System.out.println(archivo.getAbsolutePath());
            if(archivo.isFile()){
                if(archivo.getName().endsWith(".java") || archivo.getName().endsWith(".JAVA")){
                    if(archivo.getName().equals(filtro)){
                        resultado.add(archivo);
                    }
                }
            }else{
                resultado.addAll(getJavaFileList(archivo.getAbsolutePath(), filtro));
            }
        }
        
        
        return resultado;
    }
    
    public static List<String> getJavaFiles(String ruta, String filtro){
        List<String> resultado=new ArrayList<String>();
        List<File> x = getJavaFileList(ruta, filtro);
        
        for(File file:x){
            String name=file.getAbsolutePath();
            
            String busq="\\src\\java\\";
             int i;
            if(name.contains(busq)){
                i=name.indexOf(busq);          
                name = name.substring(i + busq.length());
                
            }
            resultado.add(name);
        }
        
        
        return resultado;
    }
    
        private FileObject getFO(Document doc) {
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
}
