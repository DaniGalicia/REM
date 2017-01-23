/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author galicia
 */
public class HtmlTag {
    private String name;
    private List<HtmlAtribute> attributes=new ArrayList<HtmlAtribute>();
    private List<HtmlTag> children = new ArrayList<HtmlTag>();
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        
    }

    public List<HtmlAtribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<HtmlAtribute> attributes) {
        this.attributes = attributes;
    }

    public List<HtmlTag> getChildren() {
        return children;
    }

    public void setChildren(List<HtmlTag> children) {
        this.children = children;
    }   
}
