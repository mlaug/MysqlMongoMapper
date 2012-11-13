package de.yourdelivery.mapper.beans.mapping;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "documents")
public class Documents {

	@XmlElement
	private ArrayList<Document> document;

	public ArrayList<Document> getDocuments() {
		return document;
	}

}
