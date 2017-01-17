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
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * TemplateWizard.Iterator for new ZUL/ZS file.
 * @author magic
 */
public class ZFileIterator implements TemplateWizard.Iterator{
    private transient FileType fileType;
    
    public static ZFileIterator createZulIterator(){
        return new ZFileIterator(FileType.ZUL);
    }
    
    public static ZFileIterator createZsIterator(){
        return new ZFileIterator(FileType.ZS);
    }
    
    private ZFileIterator(FileType fileType){
        this.fileType = fileType;
    }
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );
        
        String targetName = Templates.getTargetName(wizard);
        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate( df, targetName  );
        
        return Collections.singleton(dobj);
    }

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    
    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject(wiz);
        
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        WizardDescriptor.Panel zfilePanel = new ZFilePanel(project,sourceGroups,wiz,fileType);
        
        panels = new WizardDescriptor.Panel[] {zfilePanel};
        
        // Creating steps.
        Object prop = wiz.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName ();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }

    public void uninitialize(TemplateWizard templateWizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage(ZFileIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    public void addChangeListener(ChangeListener changeListener) {}

    public void removeChangeListener(ChangeListener changeListener) {}
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
}
