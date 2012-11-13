package de.yourdelivery.mapper.beans.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listing")
public class Listing {

	@XmlAttribute
	public String name;
	
	@XmlElement
	public String query;
	
}
