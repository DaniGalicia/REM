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

package net.sf.rem.loaders;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Iterator;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author magic
 */
public class ZulCatalog implements CatalogReader, CatalogDescriptor, org.xml.sax.EntityResolver{
    
    private static final String ZUL_2_3_0_XSD="zul.xsd"; // NOI18N
    private static final String ZUL_2_3_0="http://www.zkoss.org/2005/zul"; // NOI18N
    public static final String ZUL_ID_2_3_0="SCHEMA:"+ZUL_2_3_0; // NOI18N
    private static final String URL_ZUL_2_3_0="nbres:/net/sf/rem/resources/zul.xsd"; // NOI18N
    private static final String URL_ZUL_2_3_0_DTD="nbres:/net/sf/rem/resources/zul.dtd"; // NOI18N
    
    /** Creates a new instance of ZulCatalog */
    public ZulCatalog() {
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if(ZUL_ID_2_3_0.equals(publicId)){
            return new InputSource(URL_ZUL_2_3_0);
        }
        if (systemId != null && systemId.equals(ZUL_2_3_0)) {
            return new InputSource(URL_ZUL_2_3_0);
        }
        if (systemId != null && systemId.endsWith(ZUL_2_3_0_XSD)) {
            // Pleasing DTDUtil by passing it the URL to the dtd instead of the 
            // schema. This is a temporary fall back until I figure out how to 
            // convince NB to use the schema for code completion.
           return new InputSource(URL_ZUL_2_3_0_DTD);
        }
        return null;
    }

    public Iterator getPublicIDs() {
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add(ZUL_ID_2_3_0);
        return list.listIterator();
    }

    public void refresh() {
    }

    public String getSystemID(String string) {
        return URL_ZUL_2_3_0;
    }

    public String resolveURI(String string) {
        return null;
    }

    public String resolvePublic(String string) {
        return null;
    }

    public void addCatalogListener(CatalogListener catalogListener) {
    }

    public void removeCatalogListener(CatalogListener catalogListener) {
    }

    public Image getIcon(int type) {
        return Utilities.loadImage("net/sf/rem/resources/zulCatalog.png");
    }

    public String getDisplayName() {
        return "ZUL Catalog";
    }

    public String getShortDescription() {
        return "XML Catalog for ZUL 2.3.0";
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }
    
}
