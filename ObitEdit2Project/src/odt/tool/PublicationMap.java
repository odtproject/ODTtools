package odt.tool;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

/* This class will be for publication stuff */
public class PublicationMap {
	
	HashMap<String, String> publicationsMap = new HashMap<>();
	
	/* this method will initialize the publication map */
	public static PublicationMap initPublicationMap(LocationArray locsArray, String libFilesPath) {
		
		String[] pubsParts;		
		char firstchar;
		String pubsLine, pubLocation, pubName;
		PublicationMap pubsMap = new PublicationMap();
		
		try {
			/* Get the file name path from the library folder */
			Path pubsFilePath = Paths.get(libFilesPath, "pubs.txt");
			
			Scanner fileScanner = new Scanner(new File(pubsFilePath.toString()));
			
			/* Find the first line of data, which comes after a line of dashes */
			do {
				/* get the next line */
				pubsLine = fileScanner.nextLine();

			} while (! pubsLine.contains("-----"));

			/* while there are lines in the file, keep going */
			while (fileScanner.hasNextLine()) {
			
				/* get the next line */
				pubsLine = fileScanner.nextLine();
				
				/* get the first character on the line */
				firstchar = pubsLine.charAt(0);
			
				/* split the line into parts delimited by a dash surrounded by spaces */
				pubsParts = pubsLine.split(" - ");
			
				/* if there are at least four parts, then it could be a valid publication */
				if (pubsParts.length >= 3 && firstchar != ' ' && firstchar != '-') {
				
					/* get the first delimited string on the line and set it as the publication */
					pubName = pubsParts[0].trim();
					
					/* get the third string and verify that it is a location */
					pubLocation = pubsParts[2].trim();
					pubLocation = LocationArray.verifyLocation(pubLocation, locsArray);
			
					/* If the name is not the heading, the location is valid, then we
					 * could be putting it in the map.
					 */
					if ( ! pubName.equals("ABBREVIATION") && ! pubLocation.equals("Invalid") ) {
						
						pubsMap.publicationsMap.put(pubName,pubLocation);	
									
					} /* end of if the indicators mean a valid publication */
				
				} /* end of if there are enough parts in the line */
			
			} /* end of while there are lines in the file */
		
			/* If the file handlers are still open, close them. */
			if(fileScanner!=null) {
				fileScanner.close();
			}

		} /* end of try */
		
		catch (FileNotFoundException fnf) {
			/* If we were unable to open the file, then log the error. */
			System.err.println("FileNotFoundException: " + fnf.getMessage());
		}
		
		return (pubsMap);
		
	} /* end of inittPublicationArray */
	
	/* this method will determine whether a publication is valid */
	public static boolean checkPublication(PublicationMap pMap, String publication) {
		
		boolean goodPub;
		goodPub = pMap.publicationsMap.containsKey(publication);
		
		String pubMapKey;
		for ( String publicationName: pMap.publicationsMap.keySet() ) {
			
			pubMapKey = publicationName.toString();
			if (pubMapKey.equals(publication)) {
				return( true );
			}
		}
		return(goodPub);	
	
	} /* end of checkPublication method */
	
	/* This method will return the location associated with a publication */
	public static String getPubLocation(PublicationMap pMap, String publication) {
		
		String location;
		
		location = pMap.publicationsMap.get(publication);
		
		return( location );
		
	} /* end of getPubLocation */
	
	/* This method will get a publication from a string 
	 * that has location without parentheses
	 */
	public static String fixPubLocation ( String originalPublication, String locationCode ) {
		
		String workingPublication;
		int strIndex;
		
		/* Split the string into its parts." */
		String[] origParts = originalPublication.split(" ");
		
		/* If the last part of the publication is the location code,
		 * then put the string back together without the location code
		 * at the end, essentially removing the location code from
		 * the publication field.
		 * Otherwise, leave the publication alone.
		 */
		if ( locationCode.equals(origParts[origParts.length-1]) )
		{
			workingPublication = "";
			for ( strIndex = 0; strIndex < origParts.length-1; strIndex++ )
			{
				workingPublication = workingPublication + " " + origParts[strIndex];
			}
		}
		else
		{
			workingPublication = originalPublication;
		}
		
		/* trim any extra space */
		workingPublication = workingPublication.trim();
		
		/* that should do it */
		return ( workingPublication );
		
	} /* end of fixPubLocation */

} /* end of PublicationMap class */
