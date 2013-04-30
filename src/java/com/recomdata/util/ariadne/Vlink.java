/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.11.13 at 10:06:04 AM EST 
//


package com.recomdata.util.ariadne;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}attr" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="src_ref" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="dst_ref" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attr"
})
@XmlRootElement(name = "vlink")
public class Vlink {

    protected List<Attr> attr;
    @XmlAttribute(name = "src_ref")
    protected String srcRef;
    @XmlAttribute(name = "dst_ref")
    protected String dstRef;

    /**
     * Gets the value of the attr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attr }
     * 
     * 
     */
    public List<Attr> getAttr() {
        if (attr == null) {
            attr = new ArrayList<Attr>();
        }
        return this.attr;
    }

    /**
     * Gets the value of the srcRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcRef() {
        return srcRef;
    }

    /**
     * Sets the value of the srcRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcRef(String value) {
        this.srcRef = value;
    }

    /**
     * Gets the value of the dstRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDstRef() {
        return dstRef;
    }

    /**
     * Sets the value of the dstRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDstRef(String value) {
        this.dstRef = value;
    }

}
