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

import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  magic
 */
public class ZFilePanelVisual extends javax.swing.JPanel implements DocumentListener, ActionListener{
    private ZFilePanel wizardPanel;
    private Project project;
    private SourceGroup[] folders;
    private FileObject template;
    private FileType fileType;
    
    private WebModule wm;
    
    /** Creates new form ZFilePanelVisual */
    public ZFilePanelVisual(final ZFilePanel wizardPanel,Project project,SourceGroup[] folders,
            FileObject template,FileType fileType) {
        this.wizardPanel = wizardPanel;
        this.project = project;
        this.folders = folders;
        this.template = template;
        this.fileType = fileType;
        
        initComponents();
        // set text for nameLabel
        if(fileType.equals(FileType.ZUL)){
            nameLabel.setText(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("LBL_ZulName"));
        }else if(fileType.equals(FileType.ZS)){
            nameLabel.setText(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("LBL_ZsName"));
        }
        
        // set initial values for new file panel
        initValues(project);
        documentNameTextField.getDocument().addDocumentListener(this);
        folderTextField.getDocument().addDocumentListener(this);
        browseButton.addActionListener(this);
    }
    
    public String getDocumentName(){
        String text = documentNameTextField.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }
    
    public File getDocumentFile() {
        String text = getRelativeTargetFolder();
        
        if ( text.length() == 0 ) {
            if (wm==null)
                return FileUtil.toFile(getLocationRoot());
            else
                return FileUtil.toFile( wm.getDocumentBase());
        }
        else {
            // XXX have to account for FU.tF returning null
            if (wm==null) {
                return new File( FileUtil.toFile(getLocationRoot()), text );
            } else {
                return new File( FileUtil.toFile( wm.getDocumentBase() ), text );
            }
        }
    }
    
    private String getRelativeSourcesFolder() {
        String sourceDir="";
        if (wm!=null) {
            FileObject docBase = wm.getDocumentBase();
            FileObject sourcesBase = ((LocationItem)locationCB.getModel().getSelectedItem()).getFileObject();
            sourceDir = FileUtil.getRelativePath( docBase, sourcesBase );
            
            //just for source roots
            if (sourceDir == null)
                sourceDir = "";
        }
        return sourceDir.length()==0?"":sourceDir+"/";        
    }
    
    public String getRelativeTargetFolder() {
        return getRelativeSourcesFolder()+getNormalizedFolder();
    }
    
    public String getNormalizedFolder() {
        String norm = folderTextField.getText().trim();
        if (norm.length()==0) return "";       
        norm = norm.replace('\\','/');
        // removing leading slashes
        int i=0;
        while (i<norm.length() && norm.charAt(i)=='/') i++;
        if (i==norm.length()) return ""; //only slashes  
        norm = norm.substring(i);

        // removing multiple slashes
        java.util.StringTokenizer tokens = new java.util.StringTokenizer(norm,"/");
        java.util.List list = new java.util.ArrayList();
        StringBuffer buf = new StringBuffer(tokens.nextToken());
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.length()>0) buf.append("/"+token);
        }     
        return buf.toString();
    }
    
    public String getCreatedFilePath() {
        return fileTextField.getText();
    }
    
    private void initValues(Project project){
        // set project name
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        // set location field and find web module
        if (folders!=null && folders.length>0) {
            locationCB.setModel(new javax.swing.DefaultComboBoxModel(getLocations(folders)));
            wm = WebModule.getWebModule(folders[0].getRootFolder());
        } else{
            locationCB.setModel(new javax.swing.DefaultComboBoxModel(
                new Object[]{new LocationItem(project.getProjectDirectory())}));
        }
        
        // filling the folder field
        String target=null;
        FileObject docBase = getLocationRoot();
        FileObject preselectedFolder = Templates.getTargetFolder(wizardPanel.getTemplateWizard());
        if ( preselectedFolder != null && FileUtil.isParentOf( docBase, preselectedFolder ) ) {
            target = FileUtil.getRelativePath( docBase, preselectedFolder );
        }
        folderTextField.setText( target == null ? "" : target );
    }
    
    private Object[] getLocations(SourceGroup[] folders) {
        Object[] loc = new Object[folders.length];
        for (int i=0;i<folders.length;i++) loc[i] = new LocationItem(folders[i]);
        return loc;
    }
    
    private FileObject getLocationRoot() {
        return ((LocationItem)locationCB.getModel().getSelectedItem()).getFileObject();
    }
    
    public static class LocationItem {
        FileObject fo;
        SourceGroup group;
        public LocationItem(FileObject fo) {
            this.fo=fo;
        }
        public LocationItem(SourceGroup group) {
            this.fo=group.getRootFolder();
            this.group=group;
        }        
        public FileObject getFileObject() {
            return fo;
        }
        
        public String toString() {
            return (group==null?fo.getName():group.getDisplayName());
        }
    }
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(DocumentEvent e) {
        File rootDirFile = FileUtil.toFile(((LocationItem)locationCB.getSelectedItem()).getFileObject());
        if (rootDirFile != null) {
            String documentName = documentNameTextField.getText().trim();
            if (documentName.length() == 0) {
                fileTextField.setText(""); // NOI18N
            } else {
                File newFile = new File(new File(rootDirFile, folderTextField.getText().replace('/', File.separatorChar)),
                        documentName + "." + template.getExt()); //NOI18N
                fileTextField.setText(newFile.getAbsolutePath());
            }
        } else {
            // Not on disk.
            fileTextField.setText(""); // NOI18N
        }
        wizardPanel.fireChange();
    }
        
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            FileObject fo=null;
            // Show the browse dialog 
            if (folders!=null) fo = BrowseFolders.showDialog(folders,
                    org.openide.loaders.DataFolder.class,
                    folderTextField.getText().replace( File.separatorChar, '/' ) );
            else {		           
                Sources sources = ProjectUtils.getSources(project);
                fo = BrowseFolders.showDialog( sources.getSourceGroups( Sources.TYPE_GENERIC ),
                        org.openide.loaders.DataFolder.class,
                        folderTextField.getText().replace( File.separatorChar, '/' ) );
            }
            
            if ( fo != null && fo.isFolder() ) {
                FileObject root = ((LocationItem)locationCB.getSelectedItem()).getFileObject();
                folderTextField.setText( FileUtil.getRelativePath( root, fo ) );
            }
                        
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationCB = new javax.swing.JComboBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        pathLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        fillerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setName(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("TITLE_name_location"));
        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_FileName_mnem").charAt(0));
        nameLabel.setLabelFor(documentNameTextField);
        nameLabel.setText("New File Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(documentNameTextField, gridBagConstraints);
        documentNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_DESC_FileName"));

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_Project_mnem").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("LBL_Project"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(projectLabel, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_DESC_Project"));

        locationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_Location_mnem").charAt(0));
        locationLabel.setLabelFor(locationCB);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(ZFilePanelVisual.class, "LBL_Location"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(locationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(locationCB, gridBagConstraints);
        locationCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_DESC_Location"));

        folderLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_Folder_mnem").charAt(0));
        folderLabel.setLabelFor(folderTextField);
        folderLabel.setText(org.openide.util.NbBundle.getMessage(ZFilePanelVisual.class, "LBL_Folder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(folderLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_DESC_Folder"));

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("LBL_Browse_Mnemonic").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(ZFilePanelVisual.class, "LBL_Browse"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("ACSD_Browse"));

        pathLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_CreatedFile_mnem").charAt(0));
        pathLabel.setLabelFor(fileTextField);
        pathLabel.setText(org.openide.util.NbBundle.getMessage(ZFilePanelVisual.class, "LBL_CreatedFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(pathLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("net/sf/rem/loaders/wizards/Bundle").getString("A11Y_DESC_CreatedFile"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(targetSeparator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(fillerPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JComboBox locationCB;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables
    
}
