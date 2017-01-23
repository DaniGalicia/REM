/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author galicia
 */
public class ZulCompletionProvider implements CompletionProvider {
    private List<String> packages;
    public int getAutoQueryTypes(JTextComponent jtc, String string) {
        return 0;
    }

    public CompletionTask createTask(int queryType, JTextComponent jtc) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet crs, Document document, int i) {
                FileObject fo = GlobalPathRegistry.getDefault().findResource("/");
                
                ClassPath bootCp = ClassPath.getClassPath(fo, ClassPath.BOOT);
                ClassPath compileCp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
               
                final ClasspathInfo info = ClasspathInfo.create(bootCp, compileCp, sourcePath);
                final Set<ElementHandle<TypeElement>> result = info.getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
                HtmlAtribute htmlAtribute=getAttribute(document, i);

                for (ElementHandle<TypeElement> te : result) {
                   
                    
                    String binaryName = te.getBinaryName();
                    String[] text=binaryName.split("\\.");
                    String 
                    className=text[text.length-1];
                    if(!binaryName.equals("") && className.startsWith(htmlAtribute.getValue()) ){
                            //Removiendo el paquete
                            ZulCompletionItem item =  new ZulCompletionItem(text[text.length-1], i);                         
                            crs.setDocumentation(new ZulCompletionDocumentation(item));
                            crs.addItem(item);
                    }
                        
                }
                
                crs.finish();
            }
        }, jtc);
        
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
    
    private static HtmlAtribute getAttribute(Document doc, int offset) {
        String attribute = null;
        String value = null;
        int valueOffset;

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
                                    return new HtmlAtribute(attribute, value);
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
            return new HtmlAtribute(attribute, value);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void getProjectPackages(){
        packages = new ArrayList<String>();
        FileObject foClass = GlobalPathRegistry.getDefault().findResource("/");
        File ruta= new File(foClass.getPath());
        ruta = ruta.getParentFile();
        
        ruta = new File(ruta.getPath() + "/src/java/");
        
        for(File file:ruta.listFiles()){
            if(file.isDirectory()){
                
            }
        }

        
        
    }
}