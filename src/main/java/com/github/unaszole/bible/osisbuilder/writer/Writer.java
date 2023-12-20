package com.github.unaszole.bible.osisbuilder.writer;

import javax.xml.stream.XMLStreamWriter;

public interface Writer {
	
	String OSIS_NS = "http://www.bibletechnologies.net/2003/OSIS/namespace";
	
	XMLStreamWriter getXmlWriter();
	
}