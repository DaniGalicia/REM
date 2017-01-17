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

package net.sf.rem.project;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 *
 * @author fjean
 */
public class ZKWebModuleExtender extends WebModuleExtender {
    
    private final ZKFrameworkProvider framework;
    private final WebModule wm;
    private final ExtenderController controller;
    private boolean customizer;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
   
    public ZKWebModuleExtender(ZKFrameworkProvider framework, WebModule wm, ExtenderController controller, boolean customizer) {
        this.wm = wm;
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;
    }
    
    @Override
    public void addChangeListener(ChangeListener listener) {
        synchronized(listeners) {
           listeners.add(listener); 
        }
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent event = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(event);
        }
    }

    @Override
    public JComponent getComponent() {
        return new JPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void update() { }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Set<FileObject> extend(WebModule wm) {
        return framework.extendImpl(wm);
    }

}
