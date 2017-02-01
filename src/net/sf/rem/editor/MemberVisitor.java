/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author galicia
 */
public class MemberVisitor extends TreePathScanner<Void, Void> {

    private CompilationInfo info;
    private String next;
    private List<String> clases;
    private String javaClassPath;
    private int caretOfseet;
    private int removeFrom;
    CompletionResultSet completionResultSet;

    public MemberVisitor(CompilationInfo info, String next, List clases, String classPath,CompletionResultSet crs,int caretoffset) {
        this.info = info;
        this.next = next;
        this.clases = clases;
        this.javaClassPath = classPath;
        this.completionResultSet=crs;
        this.caretOfseet= caretoffset;
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {

        Element el = (Element) info.getTrees().getElement(getCurrentPath());
        if (el != null) {
            TypeElement te = (TypeElement) el;
            List<? extends Element> enclosedElements = te.getEnclosedElements();

            String atributo = null;
            String siguiente = null;

            if (next.contains(".")) {
                int pos = next.indexOf(".");
                atributo = next.substring(0, pos);
                siguiente = next.substring(pos + 1);
            } else if (!next.equals("")) {
                atributo = next;
            }

            String fieldType = getFieldType(enclosedElements, atributo);
            if (clases.contains(fieldType + ".java")) {
                //Si esta contenido en las clases sugiere
                if (siguiente != null) {
                    File file = new File(javaClassPath + "/" + fieldType.replaceAll("\\.", "/") + ".java");
                    FileObject fileObject = FileUtil.toFileObject(file);
                    JavaSource javaSource = JavaSource.forFileObject(fileObject);
                    final String next = siguiente;
                    if (javaSource != null) {
                        try {
                            javaSource.runUserActionTask(new Task<CompilationController>() {
                                @Override
                                public void run(CompilationController compilationController) throws Exception {
                                    compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                    new MemberVisitor(compilationController, next, clases, javaClassPath,completionResultSet,caretOfseet ).scan(compilationController.getCompilationUnit(), null);

                                }
                            }, true);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                //Sugiere un atributo
                InputOutput io = IOProvider.getDefault().getIO("Analysis of "
                    + info.getFileObject().getName(), true);

                    for (int i = 0; i < enclosedElements.size(); i++) {
                        Element enclosedElement = (Element) enclosedElements.get(i);
                        if (enclosedElement.getKind().isField()) {
                            if (enclosedElement.getSimpleName().toString().startsWith(atributo)) {
                                completionResultSet.addItem(new ZulCompletionItem(enclosedElement.getSimpleName().toString(), caretOfseet, "fieldPublic.png"));
                                io.getOut().println(enclosedElement.getSimpleName());
                            }
                        }
        }
                    io.getOut().close();


                }
        }

        return null;
    }

    private String getFieldType(List<? extends Element> enclosedElements, String attributeName) {
        for (int i = 0; i < enclosedElements.size(); i++) {
            Element enclosedElement = (Element) enclosedElements.get(i);
            if (enclosedElement.getKind().isField()) {
                //JOptionPane.showMessageDialog(null, enclosedElement.getSimpleName().toString());
                if (enclosedElement.getSimpleName().toString().equals(attributeName)) {

                    return enclosedElement.asType().toString();
                }
            }
        }
        return null;
    }

}
