/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.rem.loaders.xsd;

import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSSchemaSet;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.nodes.Node.Property;
import org.openide.util.Enumerations;

/**
 *
 * @author fjean
 */
public class ZulGrammar implements GrammarQuery {
    private XSSchemaSet schemaSet;

    public ZulGrammar(XSSchemaSet schemaSet) {
        this.schemaSet = schemaSet;
    }

    public Enumeration<GrammarResult> queryElements(HintContext ctx) {
        Iterator iter = schemaSet.iterateElementDecls();
        List<GrammarResult> results = new ArrayList<GrammarResult>();
        String parentName = ctx.getParentNode().getNodeName();
        XSElementDecl parent = schemaSet.getElementDecl(ctx.getCurrentPrefix(), parentName);
        if (parent == null) {
            return Collections.enumeration(results);
        }

        return Collections.enumeration(results);
    }

    public Enumeration<GrammarResult> queryAttributes(HintContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration<GrammarResult> queryValues(HintContext ctx) {
        return Enumerations.empty();
    }

    public GrammarResult queryDefault(HintContext ctx) {
        return null;
    }

    public Enumeration<GrammarResult> queryEntities(String arg0) {
        return Enumerations.empty();
    }

    public Enumeration<GrammarResult> queryNotations(String arg0) {
        return Enumerations.empty();
    }

    public boolean isAllowed(Enumeration<GrammarResult> arg0) {
        return true;
    }

    public Component getCustomizer(HintContext ctx) {
        return null;
    }

    public boolean hasCustomizer(HintContext ctx) {
        return false;
    }

    public Property[] getProperties(HintContext ctx) {
        return new Property[0];
    }

}
