package net.sf.rem.editor;

import com.sun.org.apache.bcel.internal.generic.RETURN;

/**
 *
 * @author galicia
 */
public class HtmlAtribute{
    private String name;
    private String value;
    private int offset;
    private HtmlTag etiqueta;

    public HtmlAtribute(String name) {
        this.name=name;
    }

    public HtmlAtribute(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public HtmlAtribute(String name, String value,int offset) {
        this.name = name;
        this.value = value;
        this.offset=offset;
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    
}
