


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
 *         &lt;element ref="{}img"/>
 *       &lt;/sequence>
 *       &lt;attribute name="owner" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "img"
})
@XmlRootElement(name = "thumbnail")
public class Thumbnail {

    @XmlElement(required = true)
    protected Img img;
    @XmlAttribute
    protected String owner;

    /**
     * Gets the value of the img property.
     *
     * @return possible object is
     * {@link Img }
     */
    public Img getImg() {
        return img;
    }

    /**
     * Sets the value of the img property.
     *
     * @param value allowed object is
     *              {@link Img }
     */
    public void setImg(Img value) {
        this.img = value;
    }

    /**
     * Gets the value of the owner property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOwner(String value) {
        this.owner = value;
    }

}
