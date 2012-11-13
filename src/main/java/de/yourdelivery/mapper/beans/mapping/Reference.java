package de.yourdelivery.mapper.beans.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reference")
public class Reference {

	@XmlAttribute
	public String placeholder;
	
	@XmlAttribute
	public String map;
	
}
