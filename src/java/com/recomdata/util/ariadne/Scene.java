


//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.11.13 at 10:06:04 AM EST 
//


package com.recomdata.util.ariadne;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}vobjs"/>
 *         &lt;element ref="{}vlinks"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "vobjs",
        "vlinks"
})
@XmlRootElement(name = "scene")
public class Scene {

    @XmlElement(required = true)
    protected Vobjs vobjs;
    @XmlElement(required = true)
    protected Vlinks vlinks;

    /**
     * Gets the value of the vobjs property.
     *
     * @return possible object is
     * {@link Vobjs }
     */
    public Vobjs getVobjs() {
        return vobjs;
    }

    /**
     * Sets the value of the vobjs property.
     *
     * @param value allowed object is
     *              {@link Vobjs }
     */
    public void setVobjs(Vobjs value) {
        this.vobjs = value;
    }

    /**
     * Gets the value of the vlinks property.
     *
     * @return possible object is
     * {@link Vlinks }
     */
    public Vlinks getVlinks() {
        return vlinks;
    }

    /**
     * Sets the value of the vlinks property.
     *
     * @param value allowed object is
     *              {@link Vlinks }
     */
    public void setVlinks(Vlinks value) {
        this.vlinks = value;
    }

}
