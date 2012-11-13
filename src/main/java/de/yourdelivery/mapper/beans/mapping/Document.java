package de.yourdelivery.mapper.beans.mapping;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "document")
public class Document {

	@XmlAttribute
	public String name;
	
	@XmlElement
	public String basequery;
	
	@XmlElement
	private ArrayList<Listing> listing;
	
	@XmlElement
	private ArrayList<Reference> reference;
	
	public ArrayList<Listing> getLists(){
		return listing;
	}
	
	public ArrayList<Reference> getReferences(){
		return reference;
	}

}
