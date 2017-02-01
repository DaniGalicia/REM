package net.sf.rem.editor;

import com.sun.org.apache.bcel.internal.generic.RETURN;

/**
 *
 * @author galicia
 */
public class HtmlAtribute{
    private String name;
    private String value;
    private HtmlTag etiqueta;

    public HtmlAtribute(String name) {
        this.name=name;
    }

    public HtmlAtribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HtmlAtribute{" + "name=" + name + ", etiqueta=" + etiqueta + '}';
    }



    public HtmlTag getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(HtmlTag etiqueta) {
        this.etiqueta = etiqueta;
    }


    
}
