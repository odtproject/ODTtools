package odt.pub;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import odt.tool.DirectorySetup;
import odt.pub.UploadOdtFile;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class BuildPubFiles {

	/* This main method processes the publications file and generates the HTML files
	 * for the website.  There are two files: (1) listing the publications and including
	 * links to the website for the publication, if applicable, and (2) listing the 
	 * adoptable publications, sorted by place, also including the website link.
	 */
	public static void doBuildPubFiles(String[] args) 
	{
		String fileLine, delimiter, url, htmlLine;
		String abbreviation, publicationName, location, tagname;
		String[] lineParts;
		List<String> adoptableLines = new ArrayList<>();
		boolean forAdoptable;
		
		/* Go through the file line by line and build the publications HTML file and the 
		 * HTML adoptabless list.
		 */
		try {
			
			/* Get the path for ObitEdit files */
			DirectorySetup ODTpaths = new DirectorySetup();
			
			/* Set up the ODT directory structure */
			if ( ! ODTpaths.isSetupComplete() ) {
				return;
			}

			/* Set up website files path */
			String webFileString = DirectorySetup.getODTSubFolderString("website");
			if ( webFileString.equals("Invalid") ) {
				return;
			}
			Path webFilesPath = Paths.get(webFileString);
			
			/* Get the folder for the file to check */
			/* Add on the publication file name */
			Path pubFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "pubs.txt");
			String pubFile = pubFilePath.toString();
			
			System.out.println("-----------------------");
			System.out.println("Processing file " + pubFile);

			/* Connect to the publication file */
			Scanner fileScanner = new Scanner(new File(pubFile));
			
			/* Add on the website publication file name */
			Path webPubFilePath = Paths.get(webFilesPath.toString(), "publications.html");
			String webPubFile = webPubFilePath.toString();
			PrintStream htmlFileWriter = new PrintStream(webPubFile);
			
			/* BEGIN PUBLICATIONS HTML FILE */

			/* Fill in the beginning part of the HTML file */
			initHTMLFile(htmlFileWriter);
			
			delimiter = " - ";
			
			/* Find the first line of data, which comes after a line of dashes */
			do {
				/* get the next line */
				fileLine = fileScanner.nextLine();

			} while (! fileLine.contains("--------"));
			
			/* get rid of the line of dashes */
			if (fileLine.charAt(0) == '-') {
				fileLine = fileScanner.nextLine();
			}
				
			/* while there are lines in the publications file */
			while (fileScanner.hasNextLine()) {
				
				/* get the next line of publication information */
				fileLine = fileScanner.nextLine();

				forAdoptable = true;
				url = null;				
	
				/* We may not need to do any processing at all to the line, so 
				 * start out with the same thing we just read in.
				 */
				htmlLine = fileLine + "<br>";
			
				/* If the word "(see" appears in the line, then it won't go 
				 * in the adoptables list.
				 */
				if ( fileLine.contains("[\\(]see") ) {
					forAdoptable = false;
				}
				
				Pattern matchPattern;
				Matcher myMatcher;

				/* Check for missing spaces before dashes */
				matchPattern = Pattern.compile("[\\w]- ");
				myMatcher = matchPattern.matcher(fileLine);

				if ( myMatcher.find() ) {				
					System.err.println("main: Space missing before dash");
					System.err.println(fileLine);
				}

				/* Check for missing spaces after dashes */
				matchPattern = Pattern.compile(" -[\\w]");
				myMatcher = matchPattern.matcher(fileLine);

				if ( myMatcher.find() ) {				
					System.err.println("main: Space missing after dash");
					System.err.println(fileLine);
				}

				/* split the line into parts */
				lineParts = fileLine.split("[\\s][-][\\s]");
				
				/* If there are at least four parts, then it could need special 
				 * treatment of the HTML stuff.
				 */
				if (lineParts.length >= 4) {					

					/* get the line parts */
					abbreviation = lineParts[0];
					publicationName = lineParts[1];
					location = lineParts[2];
					tagname = lineParts[3];
				
					/* If the tag name contains "Adopt" then it goes in the
					 * adoptables file. (This might be redundant, but it's safe.)
					*/
					if (tagname.contains("Adopt")) {
						forAdoptable = true;				
					}
					else {
						forAdoptable = false;
					}
					
					int parenIndex = tagname.indexOf('(');
					int endParenIndex = tagname.indexOf(')');
					
					/* If there are parentheses in the tagname, then there is a website address. */
					if (parenIndex >= 0 && endParenIndex >=0 ) {
						
						/* Split the tagname at the first parenthesis and that's the tagname.
						 * It may contain "defunct" or "Adopt me", but it's not part of the URL.
						 */
						url = tagname.substring((parenIndex+1), (endParenIndex));
						
						/* remove the URL from the tagname */
						tagname = tagname.substring(0, (parenIndex));
						
						/* if the "http://" is in the URL (which is shouldn't be), then fix it */
						if ( url.startsWith("http") ) {
							url.substring(7, url.length());
							System.err.println("\"http:\" found in publication " + publicationName);
						}						
						
						/* put together the publication name so that there is a link to the URL */
						publicationName = "<a href=\"http://" + url + "\">" + publicationName + "</a>";
						
						/* assemble the line for the HTML file */
						htmlLine = abbreviation + delimiter + publicationName + delimiter + location + delimiter + tagname + "<br>";

					} /* end of if there is a URL */
					
					/* If there's an opening parenthesis but not a closing one, or vice versa, 
					 * then report the error.
					 */
					if (parenIndex >=0) {
						if (endParenIndex < 0 ) {
							System.err.println("main error: Missing endParen: "+ abbreviation);
						}
					}
					if (endParenIndex >= 0) {
						if (parenIndex < 0) {
							System.err.println("main error: Missing opening Paren: "+ abbreviation);
						}
					}					
					
					/* The adoptable lines will need more processing so save them for 
					 * later.  We only need the location and the pub name.
					 */			
					if ( forAdoptable == true ) {
						adoptableLines.add(location + delimiter + publicationName + "<br>");					
					}
					
				} /* end of if this is a valid publication line */
				
				/* Write the formatted line to the file. */
				htmlFileWriter.println(htmlLine);				
			
			} /* end of while there are lines in the publications file */
			
			/* close the files we don't need anymore */
			if (htmlFileWriter != null) {
				htmlFileWriter.close();			
			}

			if (fileScanner != null) {
				fileScanner.close();			
			}
			
			/* END PUBLICATIONS HTML FILE */
			
			/* BEGIN ADOPTABLESS HTML FILE */
			
			/* Sort the list of lines so that we'll get the locations in
			 * alphabetic order.
			 */
			Collections.sort(adoptableLines);
			
			/* We need to know which locations are Canada provinces (sorted
			 * alphabetically)
			 */
			String[] caProvinces = {"AB","BC","MB","NB","NL","NS","NT","NU","ON","PE","QC","SK","YT"};
			
			/* and which locations are US states */
			String[] usStates = {"AK","AL","AR","AS","AZ","CA","CO","CT","DC","DE","FL","FM","GA","GU","HI","IA","ID","IL","IN","KS","KY","LA","MA","MD","ME","MH","MI","MN","MO","MP","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK","OR","PA","PR","PW","RI","SC","SD","TN","TX","UM","UT","VA","VI","VT","WA","WI","WV","WY"};
	
			/* We have to build a list of country abbreviations and header lines */
			ArrayList<String> intlCountries = new ArrayList<>();
			ArrayList<String> headerLines = new ArrayList<>();
			String abbrevLine;
			String[] abbrevLineParts, abbrevParts;
			
			/* Get the path to the location abbreviations file name and open
			 * a scanner to it.
			 */
			Path abbrevFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "abbrev.txt");
			String abbrevFile = abbrevFilePath.toString();

			Scanner abbrevFileScanner = new Scanner(new File(abbrevFile));
			
			/* Find the first line of data, which comes after a line of dashes */
			do {
				/* get the next line */
				abbrevLine = abbrevFileScanner.nextLine();

			} while (! abbrevLine.contains("-----"));

			/* skip the line of dashes */
			if ( abbrevLine.charAt(0) == '-' ) {
				abbrevLine = abbrevFileScanner.nextLine();
			}			
			
			/* Go through the abbreviations file and find possible international abbreviations
			 * and header line candidates.
			 */
			while (abbrevFileScanner.hasNextLine()) {
				abbrevLine = abbrevFileScanner.nextLine();
				abbrevLineParts = abbrevLine.split(delimiter);
				abbrevParts = abbrevLineParts[1].split(" ");
				
				/* If the abbreviation is only one word, then it is a candidate for 
				 * an International abbreviation, and for a header line.
				 */
				if (abbrevParts.length == 1) {
					/* If the abbreviation is three capital letters, it's international. */
					if (abbrevParts[0].matches("[A-Z][A-Z][A-Z]") ) {
						intlCountries.add(abbrevParts[0]);
					}
					
					/* either way, it is a possible header line */
					headerLines.add(abbrevLineParts[1] + delimiter + abbrevLineParts[0]);
				}
			}
			
			/* close the file scanner */
			if (abbrevFileScanner != null) {
				abbrevFileScanner.close();			
			}

			/* Now we're going to organize the adoptable publications by location.
			 * We have to put the actual listings in an array, because we are
			 * putting together buttons to get to each location section in the
			 * file.  So the actual listings will be put in an array first.
			 * The button lines will be put in the file.
			 * We need a file to write the final output to.
			 */
			ArrayList<String> adoptHtmLines = new ArrayList<>();
			
			/* Put together the website adoptable publication file name and open a
			 * print stream to it.
			 */
			Path webAdoptFilePath = Paths.get(webFilesPath.toString(), "adoptables.html");
			String webAdoptFile = webAdoptFilePath.toString();

			PrintStream adoptFileWriter = new PrintStream(webAdoptFile);
			
			int arraysize; /* size of the adoptables line array */
			int i, j;
			boolean haveHeader;
			String header, headerButton, locAbbrev;
			String[] adoptParts, headerParts;
			String abbrevLocation; /* province, state, or country */
			
			/* initialize the adoptables file with the heading stuff */
			initAdoptableFile(adoptFileWriter);
			
			arraysize = adoptableLines.size();			
			
			/* The adoptable publications are sorted by location, so we have to go through
			 * each array of locations for Canada, then the US, then International countries.
			 * When we find publications for each of those locations, we put them in the file
			 * under the appropriate heading.
			 */
			
			/* Write the button for Canada to the file and save the header for 
			 * Canada in the array.
			 */
			adoptFileWriter.println("</div>\n<div class=\"w3-panel\">");
			adoptFileWriter.println("<a class=\"w3-btn w3-white w3-border w3-border-teal w3-round w3-text-theme w3-margin-bottom\" href=\"#Canada\"><b>Canada</b></a>");
			adoptHtmLines.add("<div class=\"w3-panel\">");
			adoptHtmLines.add("<h1 id=\"Canada\" class=\"w3-text-theme\">Canada</h1>");
			adoptHtmLines.add("<article class=\"w3-container\">");
			
			/* First go through the array of Canada provinces */
			for ( i=0; i < caProvinces.length; i++) {
				
				/* get the province abbreviation */
				abbrevLocation = caProvinces[i];
				haveHeader = false;
				
				/* go through adoptable lines */
				for ( j=0; j < arraysize; j++) {
					
					fileLine = adoptableLines.get(j);
					adoptParts = fileLine.split(delimiter);
					location = adoptParts[0];
					
					/* If the location for this adoptable publication matches the province,
					 * check to see if we have a header for this province.
					 */
					locAbbrev = checkLocation(location, abbrevLocation);
					if ( locAbbrev != "Invalid") {
						
						/* This is the Canada province, do we have a header for 
						 * that province yet?
						 */
						if (haveHeader == false) {
							
							/* We need a header, so get the header line */
							header = getHeaderLine(locAbbrev, headerLines);
							headerParts = header.split(delimiter);
							headerButton = headerParts[0];
							
							/* If we found a header, add its button to the file
							 * and the header to the array of lines.
							 * If not, report the error.
							 */
							if (header != "Not found") 
							{
								adoptFileWriter.println("<a class=\"w3-btn w3-theme-d1 w3-small w3-round-large w3-margin-bottom\" href=\"#"+headerButton+"\">"+headerButton+"</a>");
								adoptHtmLines.add("</article>");
								adoptHtmLines.add("<article class=\"w3-container\">");
								adoptHtmLines.add("<h3 id=\"" + headerButton + "\">" + header + "</h3>");
								haveHeader = true;						
							}
							else {
								System.err.println("Cannot find header for " + location);
							}
							
						} /* end of if we do not have a header */
						
						/* Either way, we want to add this line to the file */
						adoptHtmLines.add(fileLine);
						
					} /* end of if this is the province */

				} /* end of loop through adoptable lines */
				
			} /* end of loop through Canada provinces */

			/* On to US states.
			 * Add a button for the USA and its header to the array of file lines.
			 */
			adoptFileWriter.println("</div>\n<div class=\"w3-panel\">");
			adoptFileWriter.println("<a class=\"w3-btn w3-white w3-border w3-border-teal w3-round w3-text-theme w3-margin-bottom\" href=\"#UnitedStates\"><b>United States</b></a>");
			adoptHtmLines.add("</div>\n<div class=\"w3-panel\">");
			adoptHtmLines.add("<h1 id=\"UnitedStates\" class=\"w3-text-theme\">United States</h1>");
			adoptHtmLines.add("<article class=\"w3-container\">");
			
			/* Now go through the array of US states */
			for ( i=0; i < usStates.length; i++) {
				
				/* get the state abbreviation */
				abbrevLocation = usStates[i];
				haveHeader = false;
				
				/* go through adoptable lines */
				for ( j=0; j < arraysize; j++) {
					
					/* get the line and split into parts */
					fileLine = adoptableLines.get(j);
					adoptParts = fileLine.split(delimiter);
					location = adoptParts[0];
					
					/* If the location for this adoptable publication matches the state
					 * check to see if we have a header for this state.
					 */
					locAbbrev = checkLocation(location, abbrevLocation);
					if ( locAbbrev != "Invalid") {
						
						/* This is the state, do we have a header yet? */
						if (haveHeader == false) {
							
							/* we need a header for this state */
							header = getHeaderLine(locAbbrev, headerLines);
							headerParts = header.split(delimiter);
							headerButton = headerParts[0];
							
							/* If we found the header, then add its button and
							 * its header to the list of lines.
							 * If not, then report the error.
							 */
							if (header != "Not found") 
							{
								adoptFileWriter.println("<a class=\"w3-btn w3-theme-d1 w3-small w3-round-large w3-margin-bottom\" href=\"#"+headerButton+"\">"+headerButton+"</a>");
								adoptHtmLines.add("</article>");
								adoptHtmLines.add("<article class=\"w3-container\">");
								adoptHtmLines.add("<h3 id=\"" + headerButton + "\">" + header + "</h3>");
								haveHeader = true;						
							}
							else {
								System.err.println("Cannot find header for" + location);
							}
							
						} /* end of if we do not have a header yet */
						
						/* Either way, we want to add this line to the file */
						adoptHtmLines.add(fileLine);
						
					}  /* end of this is for the state we're interested in */

				} /* end of loop through adoptable lines */
				
			} /* end of loop through US states */
			
			/* Finally do International.
			 * Add the "International" button to the file and its header to the
			 * list of file lines.
			 */
			adoptFileWriter.println("</div>\n<div class=\"w3-panel\">");
			adoptFileWriter.println("<a class=\"w3-btn w3-white w3-border w3-border-teal w3-round w3-text-theme w3-margin-bottom\" href=\"#International\"><b>International</b></a>");
			adoptHtmLines.add("</div>\n<div class=\"w3-panel\">");
			adoptHtmLines.add("<h1 id=\"International\" class=\"w3-text-theme\">International</h1>");
			adoptHtmLines.add("<article class=\"w3-container\">");
			
			/* Now go through the array of countries */
			for ( i=0; i < intlCountries.size(); i++) {
				
				/* get the country abbreviation */
				abbrevLocation = intlCountries.get(i);
				haveHeader = false;
				
				/* go through adoptable lines */
				for ( j=0; j < arraysize; j++) {
					
					/* get the line and split it into parts */
					fileLine = adoptableLines.get(j);
					adoptParts = fileLine.split(delimiter);
					location = adoptParts[0];
					
					/* If the location for this adoptable publication matches a country
					 * check to see if we have a header for this country.
					 */
					locAbbrev = checkLocation(location, abbrevLocation);
					if ( locAbbrev != "Invalid") {
						
						/* This is the country, do we have a header yet? */
						if (haveHeader == false) {
							
							/* We do not have a header yet, so get the header for this country */
							header = getHeaderLine(locAbbrev, headerLines);
							headerParts = header.split(delimiter);
							headerButton = headerParts[0];
							
							/* If we found a header for this country, add its button
							 * and add its header to the list of file lines.
							 * If not, then report the error.
							 */
							if (header != "Not found") 
							{
								adoptFileWriter.println("<a class=\"w3-btn w3-theme-d1 w3-small w3-round-large w3-margin-bottom\" href=\"#"+headerButton+"\">"+headerButton+"</a>");
								adoptHtmLines.add("</article>");
								adoptHtmLines.add("<article class=\"w3-container\">");
								adoptHtmLines.add("<h3 id=\""+headerButton+"\">" + header + "</h3>");
								haveHeader = true;						
							}
							else {
								System.err.println("Cannot find header for" + location);
							}
							
						} /* end of if we do not have a header for this country yet */
						
						/* Either way, we want to add this line to the file */
						adoptHtmLines.add(fileLine);
						
					}  /* end of if this line is for the country in question */

				} /* end of loop through adoptable lines */
				
			} /* end of loop through International countries */
			
			/* Now go through the array of lines and write them to the file. */
			for ( i=0; i < adoptHtmLines.size(); i++) 
			{
				fileLine = adoptHtmLines.get(i);
				adoptFileWriter.println(fileLine);		
			}

			/* put the last lines into the file */
			adoptFileWriter.println("</div>\n</body>\n</html>");
			
			/* Close out the file writer */
			if (adoptFileWriter != null) {
				adoptFileWriter.close();			
			}
			
			System.out.println("Processing complete");
			System.out.println("Upload files to website: ");
			System.out.println(pubFile);
			System.out.println(webPubFile);
			System.out.println(webAdoptFile);

		} /* end of try */
		
		catch (Exception e) {
			System.err.println(e.getMessage());
		}

	} /* end of main */
	
	/* This method checks whether the location string contains the location
	 * represented by the abbreviation in the argument. 
	 */
	public static String checkLocation (String locationstring, String abbrevstring) 
	{
		String[] locParts, newParts;
		int i;
		int arraysize;
		
		/* If there's nothing in the string, we're done. */
		if (locationstring.isEmpty() || abbrevstring.isEmpty()) {
			locationstring = "Invalid";
			System.err.println("checklocation: empty string: " + locationstring + " or " + abbrevstring);
			return(locationstring);
		}
		
		/* split the location string into parts and find out how many parts there are */
		locParts = locationstring.split(" ");
		newParts = locParts;
		arraysize = locParts.length;
		
		/* While there are more than three words in the string, shift everything up,
		 * until we have three words in the string.
		 */
		while ( arraysize > 3 ) {
			for (i=0; i<arraysize-1; i++) {
				newParts[i] = locParts[i+1];
			}
			locParts = newParts;
			arraysize--;
		}
		
		/* Check the string to see if it's a genuine location */
		do {
			locationstring=locParts[0];
			
			/* put together the parts of the string */
			for (i=1; i<arraysize; i++) 
			{
				locationstring = locationstring + " " + locParts[i];
			}
			
			/* If the string is in the list of recognized locations, then return it */
			if (locationstring.equals(abbrevstring)) 
			{
				return (locationstring);
			}
			else if (arraysize == 1) 
			{
				/* If there are no more string parts to check, then it is not a location */
				return ("Invalid");
			}
			
			/* Shift everything over and try again */
			for (i=0; i<arraysize-1; i++) {
				newParts[i] = locParts[i+1];
			}
			locParts = newParts;
			arraysize--;

		} while (arraysize >= 1);
		
		return("Invalid");
		
	} /* end of checkLocation method */

	/* This method gets the header lines for the section in the adoptables page.
	 */
	public static String getHeaderLine(String abbreviation, ArrayList<String> headerLinesList)
	{
		int arraysize, i;
		String headerLine;
		String[] headerParts;
		arraysize = headerLinesList.size();
		
		for (i=0; i < arraysize; i++) {
			headerLine = headerLinesList.get(i);
			
			headerParts = headerLine.split(" - ");
			
			if (headerParts[0].equals(abbreviation)) {
				return(headerLine);
			}
		}
		
		return("Not found");
		
	} /* end of getHeaderLine */

	/* This method initializes the publications HTML file, including the date
	 * and instructions for use.
	 */
	public static void initHTMLFile(PrintStream fileWriter) {
		String updateDate;
		
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yyyy");
		updateDate = sdf.format(date);

		try {
			/* get the path to the publications file header lines */
			Path pubHeaderFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "pub_hdr.txt");
			String pubHeaderFile = pubHeaderFilePath.toString();

			/* Connect to the publication header file */
			Scanner fileHeaderScanner = new Scanner(new File(pubHeaderFile));
			
			/* Get the publication description file name */
			Path pubDescFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "pub_desc.txt");
			String pubDescFile = pubDescFilePath.toString();

			/* Connect to the publication description file */
			Scanner fileDescScanner = new Scanner(new File(pubDescFile));
			
			/* put the information from the header file into the HTML file */
			while (fileHeaderScanner.hasNextLine()) {
				fileWriter.println(fileHeaderScanner.nextLine());
			}
			
			/* add the title with the date it is being updated */
			fileWriter.println("<div class=\"w3-panel w3-center w3-teal \">");
			fileWriter.println("<h2>Publications List</h2>");
			fileWriter.println("<h5>Updated "+updateDate+"</h5>");
			fileWriter.println("</div>");
		
			/* add the rest of the information from the description file */
			while (fileDescScanner.hasNextLine()) {
				fileWriter.println(fileDescScanner.nextLine());
			}
			
			/* Close out the header scanner */
			if (fileHeaderScanner != null) {
				fileHeaderScanner.close();			
			}
			
			/* Close the description scanner */
			if ( fileDescScanner != null ) {
				fileDescScanner.close();
			}

		} /* end of try */
		
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		return;
		
	} /* end of initHTMLFile */

	/* This method initializes the content of the adoptable publications file, 
	 * including the date.
	 */
	public static void initAdoptableFile(PrintStream fileWriter) 
	{
		String updateDate;
		
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yyyy");
		updateDate = sdf.format(date);
		
		try {
			/* get the path to the adoptables file header lines */
			Path adoptHdrFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "adopt_hdr.txt");
			String adoptHdrFile = adoptHdrFilePath.toString();

			/* Connect to the adoptable header file */
			Scanner fileHeaderScanner = new Scanner(new File(adoptHdrFile));
			
			/* put the information from the header file into the HTML file */
			while (fileHeaderScanner.hasNextLine()) {
				fileWriter.println(fileHeaderScanner.nextLine());
			}
			
			/* add the title with the date it is being updated */
			fileWriter.println("<div class=\"w3-panel w3-center w3-teal \">");
			fileWriter.println("<h2>Adoptable Publications</h2>");
			fileWriter.println("<h5>Updated "+updateDate+"</h5>");
			fileWriter.println("</div>");
			fileWriter.println("<div class=\"w3-container\">");
			fileWriter.println("<p>These publications were once indexed and are looking for someone to index them again.</p>");
			fileWriter.println("<p>They are sorted geographically.  Use the buttons below to jump to the area you are interested in indexing, or use your browser search function.</p>");
			fileWriter.println("<p>Each publication is listed with its location and full publication name, including a link if we know the publication website.</p>");
			fileWriter.println("</div>");
			
			/* Close out the header scanner */
			if (fileHeaderScanner != null) {
				fileHeaderScanner.close();			
			}
			
		} /* end of try */
		
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		return;
		
	} /* end initAdoptFile */
	
	/* This method uploads the publication files to the web site. */
	public static void uploadPubFiles(DirectorySetup dirSetup) 
	{		
		ArrayList<String> pubFileList = new ArrayList<>();
				
		/* Set up website files path */
		String webFileString = DirectorySetup.getODTSubFolderString("website");
		if ( webFileString.equals("Invalid") ) {
			return;
		}
		
		Path webFilesPath = Paths.get(webFileString);
		
		/* Get the full path for the publication file */
		Path pubFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "pubs.txt");
		pubFileList.add(pubFilePath.toString());		

		/* Get the website publication file name and path */
		Path webPubFilePath = Paths.get(webFilesPath.toString(), "publications.html");
		pubFileList.add(webPubFilePath.toString());

		/* Put together the website adopt publication file name and path */
		Path webAdoptFilePath = Paths.get(webFilesPath.toString(), "adoptables.html");
		pubFileList.add(webAdoptFilePath.toString());
		
		/* Try to upload files and tell user the result */
		if ( UploadOdtFile.uploadOdtFiles( pubFileList ) ) {
			JOptionPane.showMessageDialog(null, "Files uploaded");
		}
		else {
			JOptionPane.showMessageDialog(null, "No files were uploaded.");
		}
		
	} /* end uploadPubFiles */
	
} /* end of BuildPubFiles class */
