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

package net.sf.rem.loaders.wizards;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard Panel for new ZUL/ZS file.
 * @author magic
 */
public class ZFilePanel implements WizardDescriptor.Panel{
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    private ZFilePanelVisual component;
    private Project project;
    private SourceGroup[] folders;
    private TemplateWizard templateWizard;
    private FileType fileType;
    
    private static final Pattern INVALID_FILENAME_CHARACTERS = Pattern.compile("[`~!@#$%^&*()=+\\|{};:'\",<>/?]"); // NOI18N
    
    /** Creates a new instance of ZFilePanel */
    public ZFilePanel(Project project,SourceGroup[] folders,TemplateWizard templateWizard,
            FileType fileType) {
        this.project = project;
        this.folders = folders;
        this.templateWizard = templateWizard;
        this.fileType = fileType;
    }

    public Component getComponent() {
        if (component == null){
            component = new ZFilePanelVisual(this,project,folders,Templates.getTemplate(templateWizard),fileType);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ZFilePanel.class);
    }

    public TemplateWizard getTemplateWizard(){
        return templateWizard;
    }
    
    public void readSettings(Object settings) {
        templateWizard = (TemplateWizard)settings;
        // set wizard title
        if(component!=null){
            if(fileType.equals(FileType.ZUL)){
                templateWizard.putProperty("NewFileWizard_Title", // NOI18N
                        NbBundle.getMessage(ZFilePanel.class, "TITLE_ZulFile"));
            }else if(fileType.equals(FileType.ZS)){
                templateWizard.putProperty("NewFileWizard_Title", // NOI18N
                        NbBundle.getMessage(ZFilePanel.class, "TITLE_ZsFile"));
            }
        }
    }

    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if( isValid() ) {
            File f = new File(component.getCreatedFilePath());
            File ff = new File(f.getParentFile().getPath());
            if ( !ff.exists() ) {
                ff.mkdir();
            }
            FileObject folder = FileUtil.toFileObject(ff);                

            Templates.setTargetFolder( (WizardDescriptor)settings, folder );
            Templates.setTargetName( (WizardDescriptor)settings, component.getDocumentName() );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }

    public boolean isValid() {
        // check whether the document name is input
        if(component==null || component.getDocumentName()==null){
            templateWizard.putProperty ("WizardPanel_errorMessage", null); // NOI18N
            return false;
        }
        
        // check whether the document name contains invalid characters
        String filename = component.getDocumentName();
        if (INVALID_FILENAME_CHARACTERS.matcher(filename).find()) {
            templateWizard.putProperty ("WizardPanel_errorMessage", NbBundle.getMessage(ZFilePanel.class, "MSG_invalid_filename", filename)); // NOI18N
            return false;
        }
        
        // check whether can create file
        File file = component.getDocumentFile();
        FileObject template = Templates.getTemplate( templateWizard );
        String ext = template.getExt ();
        
        String errorMessage = canUseFileName (file, component.getRelativeTargetFolder(), filename, ext);
        if (errorMessage!=null){
            templateWizard.putProperty ("WizardPanel_errorMessage", errorMessage); // NOI18N
        }else {
            templateWizard.putProperty ("WizardPanel_errorMessage", null); // NOI18N
        }
        
        boolean valid = (errorMessage==null);
        
        // check whether the file name contians '.'
        if (valid && filename.indexOf(".")>=0) {
            // warning when file name contains dots
            templateWizard.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(ZFilePanel.class, "MSG_dotsInName",filename+"."+ext));
        }
        return valid;
    }

    public void addChangeListener(ChangeListener changeListener) {
        listeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }
    
    protected void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
    
    // the following copied from module org/netbeans/web/core
    
    /** Checks if the given file name can be created in the target folder.
     *
     * @param dir target directory
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    final public static String canUseFileName (java.io.File dir, String relativePath, String objectName, String extension) {
        String newObjectName=objectName;
        if (extension != null && extension.length () > 0) {
            StringBuffer sb = new StringBuffer ();
            sb.append (objectName);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        
        // check file name
        
        if (!checkFileName(objectName)) {
            return NbBundle.getMessage (ZFilePanel.class, "MSG_invalid_filename", newObjectName); // NOI18N
        }
        // test if the directory is correctly specified
        FileObject folder = null;
        if (dir!=null) {
            try {
                 folder = org.openide.filesystems.FileUtil.toFileObject(dir);
            } catch(java.lang.IllegalArgumentException ex) {
                 return NbBundle.getMessage (ZFilePanel.class, "MSG_invalid_path", relativePath); // NOI18N
            }
        }
            
        // test whether the selected folder on selected filesystem is read-only or exists
        if (folder!=  null) {
            // target filesystem should be writable
            if (!folder.canWrite ()) {
                return NbBundle.getMessage (ZFilePanel.class, "MSG_fs_is_readonly"); // NOI18N
            }

            if (folder.getFileObject (newObjectName) != null) {
                return NbBundle.getMessage (ZFilePanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
            }

            if (org.openide.util.Utilities.isWindows ()) {
                if (checkCaseInsensitiveName (folder, newObjectName)) {
                    return NbBundle.getMessage (ZFilePanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
                }
            }
        }

        // all ok
        return null;
    }
    
    // helper check for windows, its filesystem is case insensitive (workaround the bug #33612)
    /** Check existence of file on case insensitive filesystem.
     * Returns true if folder contains file with given name and extension.
     * @param folder folder for search
     * @param name name of file
     * @param extension extension of file
     * @return true if file with name and extension exists, false otherwise.
     */    
    private static boolean checkCaseInsensitiveName (FileObject folder, String name) {
        // bugfix #41277, check only direct children
        java.util.Enumeration children = folder.getChildren (false);
        FileObject fo;
        while (children.hasMoreElements ()) {
            fo = (FileObject) children.nextElement ();
            if (name.equalsIgnoreCase (fo.getName ())) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean checkFileName(String str) {
        char c[] = str.toCharArray();
        for (int i=0;i<c.length;i++) {
            if (c[i]=='\\') return false;
            if (c[i]=='/') return false;
        }
        return true;
    }
}
