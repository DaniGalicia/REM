
package net.sf.rem.editor;

public class HtmlTag {
    private String name;
    private HtmlTag padre;

    public HtmlTag(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        
    }

    public HtmlTag getPadre() {
        return padre;
    }

    public void setPadre(HtmlTag padre) {
        this.padre = padre;
    }

    @Override
    public String toString() {
        return "HtmlTag{" + "name=" + name + ", padre=" + padre + '}';
    }

    
}
