package odt.ftool;
import java.io.*;
import java.util.*;

import odt.tool.DirectorySetup;

import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/* This class is for checking ODT files to see if they
 * have entries that duplicate ones that we already have
 * in our databases.
 */
public class CheckForDups {
	/*	public static void main(String[] args) {*/
	
	public static void doCheckForDups ( String fileToCheck, String folderString, String[] args) {

		String fileLine;
		String fileName;
		Path folderToCheck;
		List<Path> fileList;
		
		/* Each of these arrays is filled with ODT entries.
		 * One is used for holding the lines that are in the 
		 * file being checked.
		 * The other holds lines that are duplicates.
		 */
		ArrayList<String> checkLinesArray = new ArrayList<>();
		ArrayList<String> dupLinesArray = new ArrayList<>();

		System.out.println("-----------------------------------");
		
		try {			
			/* Get the path for ObitEdit files */
			DirectorySetup ODTpaths = new DirectorySetup();
			
			/* Set up the ODT directory structure */
			if ( ! ODTpaths.isSetupComplete() ) {
				return;
			}

			/* Get the folder for the file to check */
			String checkFileString = DirectorySetup.getODTSubFolderString("Check");
			if ( checkFileString.equals("Invalid") ) {
				return;
			}
			Path checkFilePath = Paths.get(checkFileString);
			
			/* Get the folder for the report output */
			String reportFilePathString = DirectorySetup.getODTSubFolderString("Reports");
			if ( reportFilePathString.equals("Invalid") ) {
				return;
			}
			
			/* Set up path to the folder where the final moderator keeps
			 * the database files to check against.
			 */
			String obitsFilePathString;
			if ( ODTpaths.setMCCFolder() ) {
				
				/* Get the folder for the files to be checked against */
				obitsFilePathString = ODTpaths.getMCCFolder();
				if ( obitsFilePathString.equals("Invalid") ) {
					System.err.println("Invalid test folder -- quitting");
					return;
				}	
			}
			else {
				return;
			}
			
			/* Get a prefix for the file name to use for report file names */			
			int charIndex;
			String fileCheckName = fileToCheck;
			
			charIndex = fileCheckName.indexOf('.');
			if ( charIndex < 0 ) {
				charIndex = fileCheckName.length();
			}

			/* Set up the path to the file to be checked and establish a scanner
			 * to it.
			 */
			checkFilePath = Paths.get(checkFileString, fileToCheck);
			fileToCheck = checkFilePath.toString();			
			Scanner fileScanner = new Scanner(new File (fileToCheck));
			String fileNamePrefix = fileCheckName.substring(0, charIndex);
			
			/* For unique entries */
			Path uniqFilePath = Paths.get(reportFilePathString, fileNamePrefix + "uniq.txt");
			String uniqFile = uniqFilePath.toString();			
			PrintStream uniqFileWriter = new PrintStream(new File(uniqFile));
			System.out.println("Unique lines are in " + uniqFile);
			
			/* For duplicate entries */
			Path dupFilePath = Paths.get(reportFilePathString, fileNamePrefix + "dups.txt");
			String dupFile = dupFilePath.toString();			
			PrintStream dupFileWriter = new PrintStream(new File(dupFile));			
			System.out.println("Duplicate lines are in " + dupFile);
			
			/* For the report file */
			Path rptFilePath = Paths.get(reportFilePathString, fileNamePrefix + "dupRpt.txt");
			String rptFile = rptFilePath.toString();			
			PrintStream rptFileWriter = new PrintStream(new File(rptFile));			
			System.out.println("Duplicate report is in " + rptFile);
			
			/* fill up the array with the lines we are checking */
			while (fileScanner.hasNextLine() ) {				
				checkLinesArray.add(fileScanner.nextLine());
			}
			
			System.out.println("Total lines to check = " + checkLinesArray.size());
			System.out.println();			

			folderToCheck = Paths.get(obitsFilePathString, folderString);
			System.out.println("Checking " + fileCheckName + " against files in " + folderToCheck);
			
			/* get a list of files from the directory */
			fileList = listSourceFiles(folderToCheck);
			Iterator<Path> fileIterator = fileList.iterator();
			
			/* loop through the files */
			while ( fileIterator.hasNext() )  {
				
				/* get the file name and open it up */
				fileName = fileIterator.next().toString();
				Scanner iteratorFileScanner = new Scanner(new File (fileName));

				/* loop through the lines in the file */
				while ( iteratorFileScanner.hasNextLine()) {
					
					fileLine = iteratorFileScanner.nextLine();						
										
					/* If the line from the file from the directory matches this line
					 * then report it and save it.
					 */
					if ( checkLinesArray.contains(fileLine)) {	

						dupLinesArray.add(fileLine);
						rptFileWriter.println(fileLine);
						rptFileWriter.println("in " + fileName + "\n");
					}					
					
				} /* end of loop through lines in file */
				
				/* close the file if it is still open */
				if (iteratorFileScanner != null) {
					iteratorFileScanner.close();			
				}

			} /* end of loop through list of files in directory */								
				
			int dupCount=0;
			int uniqCount=0;
			
			/* Go through lines in file being checked and check against list of duplicates.
			 * If there's no match, it's unique and will be stored in a separate file.
			 */				
			Iterator<String> checkFileIterator = checkLinesArray.iterator();
				
			/* while there are lines in the array of lines in the check file */
			while ( checkFileIterator.hasNext() ) {					
				
				/* get the line to check */
				fileLine = checkFileIterator.next();
				
				/* If this line in the file to check is in the 
				 * list of duplicate lines, then save it in the
				 * file of duplicates.
				 * Otherwise, it is unique and should be saved
				 * in the file of unique lines.
				 */
				if ( dupLinesArray.contains(fileLine) ) {
					dupFileWriter.println(fileLine);	
					dupCount++;
				}
				else {
					uniqFileWriter.println(fileLine);
					uniqCount++;
				}
				
			} /* end of while there are lines in the check file array */
			
			System.out.println("Unique count = " + uniqCount);
			System.out.println("Duplicate count = " + dupCount);
			
			/* close files if they are still open */
			if (dupFileWriter != null) {
				dupFileWriter.close();			
			}
			if (uniqFileWriter != null) {
				uniqFileWriter.close();			
			}			
			if (rptFileWriter != null) {
				rptFileWriter.close();
			}
			if (fileScanner != null) {
				fileScanner.close();			
			}			
			
			System.out.println("Finished successfully.");
				
		} /* end of try opening output files and console scanner */
			
		catch (Exception e) {
			System.err.println("CheckForDups exception: " + e.getMessage());
		}
		
		finally {

		}
	
	} /* end of main */
	
	/* this method gets a list of file names in a directory */
	static List<Path> listSourceFiles(Path dir) throws IOException {
		
	    List<Path> result = new ArrayList<>();
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*")) {
	    	for (Path entry: stream) {
	    		
	    		/* We only want files */
	    		if ( entry.toFile().isFile() ) {
	    			result.add(entry);
	    		}	    		
	    	}
	    } catch (DirectoryIteratorException ex) {
	    	/* I/O error encountered during the iteration, the cause is an IOException */
	    	throw ex.getCause();
	    }
	    
	    return result;
	    
	} /* end of listSourceFiles method */

} /* end of CheckForDups class */
