/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import java.net.URL;
import javax.swing.Action;
import org.netbeans.spi.editor.completion.CompletionDocumentation;

/**
 *
 * @author galicia
 */
public class ZulCompletionDocumentation implements CompletionDocumentation{
    private ZulCompletionItem item;

    public ZulCompletionDocumentation(ZulCompletionItem item) {
        this.item = item;
    }
   
    public String getText() {
        return "Informacion de ";//+item.getText();
    }

    public URL getURL() {
        return null;
    }

    public CompletionDocumentation resolveLink(String string) {
        return null;
    }

    public Action getGotoSourceAction() {
        return null;
    }
    
}
