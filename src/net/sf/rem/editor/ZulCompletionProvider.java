/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.CompilationInfo;
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

    private List<String> clases = new ArrayList<String>();
    private String JavaClassPath;

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
                if (clases.isEmpty()) {
                    findClasses();
                }
                               
                FileObject foClass = getFO(document); //GlobalPathRegistry.getDefault().findResource("/");
                ClassPath sourcePath = ClassPath.getClassPath(foClass, ClassPath.SOURCE);
                
                for(FileObject root:sourcePath.getRoots()){
                    if(root.getPath().endsWith("java")){
                        JavaClassPath=root.getPath();
                    }
                }
               
                
                

                // clases.clear();
                HtmlAtribute htmlAtribute = getAttribute(document, i);
                if (htmlAtribute == null) {
                    crs.finish();
                    return;
                }

                String value = htmlAtribute.getValue();

                value = value.replace("{", "");
                value = value.replace("}", "");
                value = value.replace("$", "");

                for (String clase : clases) {
                    int point = clase.lastIndexOf(".");

                    String className = clase.substring(0, point);
                    point = className.lastIndexOf(".");
                    className = className = clase.substring(point + 1);
                    point = className.lastIndexOf(".");
                    className = className.substring(0, point);
                    //FileObject fileObject = GlobalPathRegistry.getDefault().findResource(clase.replaceAll("\\.", "/")+".java");
                    //JavaSource javaSource = JavaSource.forFileObject(fileObject);

                    ZulCompletionItem item;
                    value = value.toLowerCase();
                    if (value != null && !value.equals("") && className.toLowerCase().startsWith(value)) {
                        item = new ZulCompletionItem(className, i, "class.png");
                        crs.addItem(item);
                        crs.setDocumentation(new ZulCompletionDocumentation(item));
                    }
// ClassFile c=null;
//                    try {
//                        //Codigo para buscar metodos
//                        c = new ClassFile(new File("C:/Users/galicia/Documents/NetBeansProjects/REM/build/classes/net/sf/rem/editor/ZulCompletionItem.class"),false);
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                    c=c;


                }

                crs.finish();
            }
        }, jtc);

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
                    "href", // "href" in <?link ?> 
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

    private void getProjectPackages() {
        clases = new ArrayList<String>();
        FileObject foClass = GlobalPathRegistry.getDefault().findResource("/");
        File ruta = new File(foClass.getPath());
        ruta = ruta.getParentFile();

        ruta = new File(ruta.getPath() + "/src/java/");

        for (File file : ruta.listFiles()) {
            if (file.isDirectory()) {
                String className = file.getName();

            }
        }

    }

    //Buscando los archivos java
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

    public void findClasses() {
        File folder = new File(JavaClassPath);

        if (folder.getPath().endsWith("web")) {
            folder = new File(folder.getParent() + "/src/java/");
        }

        for (File file : getJavaFileList(folder)) {
            String name = file.getAbsolutePath();

            String busq = "\\src\\java\\";
            int i;
            if (name.contains(busq)) {
                i = name.indexOf(busq);
                name = name.substring(i + busq.length());
                name = name.replaceAll("\\\\", ".");
            }
            clases.add(name);
        }
    }

    private class MemberVisitor extends TreePathScanner<Void, Void> {

        private CompilationInfo info;

        public MemberVisitor(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            Element el = (Element) info.getTrees().getElement(getCurrentPath());

//            if (el == null) {
//                StatusDisplayer.getDefault().setStatusText("No se puede resolver la clase");
//
//            } else {
//                TypeElement te = (TypeElement) el;
//
//                List<? extends Element> enclosedElements = te.getEnclosedElements();
//
//                InputOutput io = IOProvider.getDefault().getIO("Analysis of "
//                        + info.getFileObject().getName(), true);
//                
//                for (int i = 0; i < enclosedElements.size(); i++) {
//                    Element enclosedElement = (Element) enclosedElements.get(i);
//                    if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
//                        io.getOut().println("Constructor: " + enclosedElement.getSimpleName());
//                    } else if (enclosedElement.getKind() == ElementKind.METHOD) {
//                        ExecutableElement ex = (ExecutableElement) enclosedElement;
//                        io.getOut().println("Method: " + enclosedElement.getSimpleName() + " " + ex.getReturnType().toString());
//                    } else if (enclosedElement.getKind().isField()) {
//                        io.getOut().println("Field: " + enclosedElement.getSimpleName());
//                    } else {
//                        System.out.println("Other: " + enclosedElement.getSimpleName());
//                    }
//                }
//                io.getOut().close();
//            }
            return null;
        }

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
