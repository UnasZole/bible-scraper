package com.github.unaszole.bible.osisbuilder;

import org.crosswire.jsword.versification.Versification;

/**
This interface exposes all information to identify a specific bible,
and methods for retrieving its content from anywhere.
*/
public interface BibleDataSource {
	
	/**
		@return A unique ID for this bible.
	*/
	String getOsisIDWork();
	
	/**
		@return The title of this bible.
	*/
	String getTitle();
	
	/**
		@return The IETF language code for this bible (ISO 639-1 or ISO 639-3).
	*/
	String getIetfLanguage();
	
	/**
		@return The versification system used by this bible.
		
		The list of supported versification systems is here :
		https://wiki.crosswire.org/Alternate_Versification#Supported_versification_systems
		
		Their java implementations are here :
		https://github.com/crosswire/jsword/tree/master/src/main/java/org/crosswire/jsword/versification/system
	*/
	Versification getVersificationSystem();
}