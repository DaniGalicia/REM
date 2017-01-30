/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
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
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author galicia
 */
public class ZulCompletionProvider implements CompletionProvider {

    private List<String> clases = new ArrayList<String>();
    private String javaClassPath;
    private List<HtmlTag> etiquetas = new ArrayList<HtmlTag>();
    private List<HtmlAtribute> atributos = new ArrayList<HtmlAtribute>();

    private static Hashtable<String, Integer> completationTable;

    {
        completationTable = new Hashtable<String, Integer>();
        completationTable.put("apply", 0);
        completationTable.put("value", 1);

    }

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
                String newJavaClassPath = "";
                FileObject foClass = getFO(document);
                ClassPath sourcePath = ClassPath.getClassPath(foClass, ClassPath.SOURCE);

                for (FileObject root : sourcePath.getRoots()) {
                    if (root.getPath().endsWith("java")) {
                        newJavaClassPath = root.getPath();
                    }
                }
                //Verificando  si no ha cambiado de proyecto
                if (javaClassPath == null) {
                    javaClassPath = newJavaClassPath;
                    findClasses();
                }
                if (!newJavaClassPath.equals(javaClassPath)) {
                    javaClassPath = newJavaClassPath;
                    findClasses();
                }

                // Recuperando el atributo donde se hizo click
                etiquetas.clear();
                atributos.clear();
                HtmlTag htmlTag = getHtmlTag(document, i);
                
                HtmlAtribute htmlAtribute = getHtmlAttribute(document, i);
                if (htmlAtribute == null || !completationTable.containsKey(htmlAtribute.getName())) {
                    crs.finish();
                    return;
                }

                String value = htmlAtribute.getValue();

                switch (completationTable.get(htmlAtribute.getName())) {
                    case 0://Solo busca la clase
                        value = value.replace("{", "");
                        value = value.replace("}", "");
                        value = value.replace("$", "");
                        for (String clase : clases) {
                            String className = getClassName(clase);
                            value = value.toLowerCase();
                            if (value != null && !value.equals("") && className.toLowerCase().startsWith(value)) {
                                crs.addItem(new ZulCompletionItem(className, i, "class.png"));
                            }
                        }
                        break;
                    case 1://Busca los atributos de esa clase
                        value = value.replace("@{win$composer.", "");
                        value = value.replace("@{winP$composer.", "");
                        value = value.replace("}", "");
                        String[] separados = value.split("\\.");
                        String text = "";
                        try {
                            text = document.getText(0, document.getLength() - 1);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        int pos = text.indexOf("apply=");
                        String bean = getHtmlAttribute(document, pos + "apply=".length()).getValue();
                        bean = bean.replace("{", "");
                        bean = bean.replace("}", "");
                        bean = bean.replace("$", "");
                        bean = findClass(bean, true);
                        bean = bean.replace(".java", "");
                        bean = bean.replaceAll("\\.", "/");

                        File file = new File(javaClassPath + "/" + bean + ".java");
                        FileObject fileObject = FileUtil.toFileObject(file);
                        JavaSource javaSource = JavaSource.forFileObject(fileObject);
                        final String next = value;
                        final CompletionResultSet fcrs = crs;
                        final int caret = i;

                        if (javaSource != null) {
                            try {
                                javaSource.runUserActionTask(new Task<CompilationController>() {
                                    @Override
                                    public void run(CompilationController compilationController) throws Exception {

                                        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        new MemberVisitor(compilationController, next, clases, javaClassPath, fcrs, caret).scan(compilationController.getCompilationUnit(), null);

                                    }
                                }, true);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        break;
                }
                crs.finish();
            }
        }, jtc
        );

    }

    private String findClass(String value, boolean exactMatch) {
        for (String clase : clases) {
            String className = getClassName(clase);
            value = value.toLowerCase();
            if (value != null && !value.equals("") && className.toLowerCase().startsWith(value)) {
                if (exactMatch && className.toLowerCase().equals(value)) {
                    return clase;
                }
                if (!exactMatch && className.toLowerCase().startsWith(value)) {
                    return clase;
                }

            }
        }
        return "";
    }

    /**
     * @param doc Documento donde se hizo click
     * @param offset Posicion del documento donde se hizo click
     * @return HtmlAtribute donde se hizo click
     */
    private  HtmlAtribute getHtmlAttribute(Document doc, int offset) {
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

    private  HtmlAtribute getHtmlAttribute(TokenItem token) {
        String attribute = null;
        String value = null;
        int valueOffset;

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
    }

    private  HtmlTag getHtmlTag(Document doc, int offset) {
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

            if (token == null || token.getTokenID().getNumericID() != ZulEditorUtilities.XML_ATTRIBUTE_VALUE) {
                return null;
            }

            while (token != null && token.getTokenID().getNumericID() != ZulEditorUtilities.XML_TAG) {
                token = token.getPrevious();
            }
            String tagName = token.getImage();
            tagName = tagName.replace("<", "");
            tagName = tagName.replace(">", "");
            HtmlTag htmlTag = new HtmlTag(tagName);

            //Obteniendo atributos
            HtmlAtribute htmlAtribute = null;
            token = token.getNext();
            while (token != null && token.getTokenID().getNumericID() != ZulEditorUtilities.XML_TAG) {
                if (token.getTokenID().getNumericID() == ZulEditorUtilities.XML_ATTRIBUTE_VALUE) {
                    htmlAtribute = getHtmlAttribute(token);
                    htmlAtribute.setEtiqueta(htmlTag);
                    atributos.add(htmlAtribute);
                }
                token = token.getNext();
            }

            boolean finish = false;
            //Comprobando si tiene hijos
            if (token.getImage().equals("/>")) {

                finish = true;
            }
            token = token.getNext();
            while (token != null && !finish) {
                if (token.getTokenID().getNumericID() == ZulEditorUtilities.XML_TAG) {
                    if (!token.getImage().startsWith("</" + htmlTag.getName()) && !token.getImage().equals(">")) {
                        token = getHtmlTag(token, htmlTag);
                    } else if (token.getImage().startsWith("</" + htmlTag.getName())) {//Fin de la etiqueta
                        finish = true;
                    }
                }
                token = token.getNext();
            }
            etiquetas.add(htmlTag);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    private TokenItem getHtmlTag(TokenItem token, HtmlTag parent) {
        String attribute = null;
        String value = null;
        int valueOffset;

        String tagName = token.getImage();
        tagName = tagName.replace("<", "");
        tagName = tagName.replace(">", "");
        HtmlTag htmlTag = new HtmlTag(tagName);
        htmlTag.setPadre(parent);

        //Obteniendo atributos
        HtmlAtribute htmlAtribute = null;
        token = token.getNext();
        while (token != null && token.getTokenID().getNumericID() != ZulEditorUtilities.XML_TAG) {
            if (token.getTokenID().getNumericID() == ZulEditorUtilities.XML_ATTRIBUTE_VALUE) {
                htmlAtribute = getHtmlAttribute(token);
                htmlAtribute.setEtiqueta(htmlTag);
                atributos.add(htmlAtribute);
            }
            token = token.getNext();
        }
        boolean finish = false;

        if (token.getImage().equals("/>")) {

            finish = true;
        }
        while (token != null && !finish) {
            if (token.getTokenID().getNumericID() == ZulEditorUtilities.XML_TAG) {
                if (!token.getImage().equals("</" + htmlTag.getName()) && !token.getImage().equals(">")) {
                    token = getHtmlTag(token, htmlTag);
                } else if (token.getImage().equals("</" + htmlTag.getName())) {//Fin de la etiqueta
                    finish = true;
                }
            }
            token = token.getNext();
        }
        etiquetas.add(htmlTag);
        return token;
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
    public void findClasses() {
        clases.clear();
        File folder = new File(javaClassPath);

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

    /**
     * @param doc documento en el cual se ha hecho click
     * @return FileObject relacionado con el documento que se hizo clic
     */
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

    /**
     * @param className nombre completo de la clase (con paquete)
     * @return el nombre de la clase (sin extension)
     */
    private String getClassName(String className) {
        String resultado = className.replace(".java", "");
        int pos = resultado.lastIndexOf(".");
        resultado = resultado.substring(pos + 1);
        return resultado;
    }

    public List getHijos(String idEtiqueta) {
        List resultado = new ArrayList();
        for (HtmlTag htmlTag : etiquetas) {
            if (htmlTag.getPadre() != null && htmlTag.getPadre().getName().equals(idEtiqueta)) {
                resultado.add(htmlTag);
            }
        }
        return resultado;
    }
    
    public List getAtributos(String idEtiqueta) {
        List resultado = new ArrayList();
        for (HtmlAtribute htmlAtribute : atributos) {
            if (htmlAtribute.getEtiqueta().getName().equals(idEtiqueta)) {
                resultado.add(htmlAtribute);
            }
        }
        return resultado;
    }
}
