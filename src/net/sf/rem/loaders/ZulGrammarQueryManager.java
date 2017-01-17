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

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.DTDUtil;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.openide.util.Enumerations;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author magic
 */
public class ZulGrammarQueryManager extends org.netbeans.modules.xml.api.model.GrammarQueryManager {

    /** Creates a new instance of ZulGrammarQueryManager */
    public ZulGrammarQueryManager() {
    }

    public Enumeration enabled(GrammarEnvironment grammarEnvironment) {
        return Enumerations.empty();
    }

    public GrammarQuery getGrammar(GrammarEnvironment grammarEnvironment) {
        UserCatalog catalog = UserCatalog.getDefault();
        if (catalog != null) {
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    String schema = ZulCatalog.ZUL_ID_2_3_0;
                    InputSource inputSource = resolver.resolveEntity(schema, null);
                    if (inputSource != null) {
                        // parse DTD file
                        GrammarQuery grammar = DTDUtil.parseDTD(true, inputSource);
                        return grammar;
                    }
                } catch (SAXException e) {
                    e.printStackTrace(System.out);
                } catch (java.io.IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
        return null;
    }

    public FeatureDescriptor getDescriptor() {
        return new java.beans.FeatureDescriptor();
    }
}
