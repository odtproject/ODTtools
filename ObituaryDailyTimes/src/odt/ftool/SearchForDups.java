package odt.ftool;
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;

import odt.tool.DirectorySetup;

import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SearchForDups {

	public static void doSearchForDups(String folderString, String[] args) {

		String fileLine;
		String fileNamePathString;
		Path folderToCheckPath;
		HashMap<String, String> uniqLinesMap = new HashMap<>();
		HashMap<String, String> dupLinesMap = new HashMap<>();
		List<Path> filePathList;

		try {			
			/* Get the path for ObitEdit files */
			DirectorySetup dirSetup = new DirectorySetup();
			
			if ( ! dirSetup.isSetupComplete() ) {
				return;
			}
						
			/* Set up report file path -- for location of output reports */
			String reportFileString = DirectorySetup.getODTSubFolderString("Reports");
			if ( reportFileString.equals("Invalid")) {
				return;
			}

			/* Set up report file path -- for location of unique files */
			String uniqFileFolderString = DirectorySetup.getODTSubFolderString("unique");
			if ( uniqFileFolderString.equals("Invalid")) {
				return;
			}
			
			/* Clean that folder of old unique file processed files */
			try {
				FileUtils.cleanDirectory(new File(uniqFileFolderString));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			/* Get the directory that contains the files to check */
			String folderPathString;
			if ( ! dirSetup.setMCCFolder() ) {
				return;
			}
			
			folderPathString = dirSetup.getMCCFolder();
			if ( folderPathString.equals("Invalid") ) {
				return;
			}
			
			folderToCheckPath = Paths.get(folderPathString, folderString);
			
			System.out.println();
			System.out.println("------------------------------");
			System.out.println("Checking files in " + folderToCheckPath.toString());

			try {
				/* get a list of files from the directory */
				filePathList = listSourceFiles(folderToCheckPath);
				Iterator<Path> filePathIterator = filePathList.iterator();
				/* fill up the array with the lines we are checking */
				
				/* loop through the files in the directory */
				while ( filePathIterator.hasNext() )  {
					
					/* get the file name and open it up */
					fileNamePathString = filePathIterator.next().toString();
					Scanner iteratorFileScanner = new Scanner(new File (fileNamePathString));
					
					/* loop through the lines in the file */
					while ( iteratorFileScanner.hasNextLine()) {
						
						fileLine = iteratorFileScanner.nextLine();			
											
						/* If the line from the file from the directory matches this line
						 * then put it in the duplicate list.
						 * Otherwise, put it in the unique list.
						 */
						if ( uniqLinesMap.containsKey(fileLine)) {							
							dupLinesMap.put(fileLine, fileNamePathString);
						}
						else {
							uniqLinesMap.put(fileLine, fileNamePathString);
						}
						
					} /* end of loop through lines in file */
					
					/* close the file */
					iteratorFileScanner.close();			

				} /* end of loop through list of files in directory */
				
				System.out.println("Finished searching files for duplicates");
				
				System.out.println("Putting unique entries into files in");
				System.out.println(uniqFileFolderString);
					
				Path uniqFilePath;
				String justFileName;
				List<String> filePathNameList = new ArrayList<>();
				
				/* Make a list of the full path name of each file in the map
				 * of unique lines in the files.
				 */
				for ( String value : uniqLinesMap.values() ) {
					
					if ( ! filePathNameList.contains(value) ) {
						filePathNameList.add(value);
					}					
				}
				
				/* Sort the list so that we'll go through the unique lines in the
				 * files in order by file name.
				 */
				Collections.sort(filePathNameList);
				
				/* We need a list to keep all the lines for a file so that we can
				 * sort the lines before putting them into the file.
				 */
				List<String> uniqLinesArray = new ArrayList<>();
					
				/* Go through the entries in the list of unique lines listed
				 * by file name and put them each back in their respective files
				 * but in a different folder.
				 */
				for ( int i = 0; i < filePathNameList.size(); i++ ) {
				
					/* Get the full path to the file */
					fileNamePathString = filePathNameList.get(i);
					
					/* Each key is a file name that we want to open and save
					 * all the unique lines for that file.
					 * The key is the full path to the input file.
					 * We want to write the output to a file of the same
					 * name but in the folder for files with unique entries.
					 * We have to get the filename, and then get the path to
					 * the new file name.
					 */
					justFileName = Paths.get(fileNamePathString).getFileName().toString();
					uniqFilePath = Paths.get(uniqFileFolderString, justFileName);
					
					Iterator<Map.Entry<String,String>> uniqMapIterator = uniqLinesMap.entrySet().iterator();
					
					/* While there are entries in the map, check to see if they belong
					 * to this file.
					 */
					while ( uniqMapIterator.hasNext() ) {

						Map.Entry<String,String> mapEntry = uniqMapIterator.next();
						
						/* If the key (filename) is the same as the filename we are
						 * looking for, then this entry goes in array.
						 */
						if ( mapEntry.getValue() == fileNamePathString ) {
							
							uniqLinesArray.add(mapEntry.getKey());
																			
							/* remove it so we go faster the next time */
							uniqMapIterator.remove();
						}
						
					} /* end of while there are unique entries */
					
					/* Sort the list of unique lines for this file */
					Collections.sort(uniqLinesArray);
					
					/* Open a writer to the file */
					PrintStream uniqFileWriter = new PrintStream(new File(uniqFilePath.toString()));
					
					/* Go through the sorted list of unique entries for this file
					 * and write them to the new file.
					 */
					for ( String uniqLine : uniqLinesArray) {
						uniqFileWriter.println(uniqLine);
					}
					
					/* Clear out the array list */
					uniqLinesArray.clear();
					
					/* close the stream to this file */
					uniqFileWriter.close();
					
				} /* end of for all the keys in the unique file list */
				
				/* Get a report file stream going and an iterator through
				 * the list of duplicate lines.
				 */
				Path dupFilePath = Paths.get(reportFileString, "SearchDupsRpt.txt");
				PrintStream dupFileWriter = new PrintStream(new File(dupFilePath.toString()));			

				Iterator<Map.Entry<String,String>> dupMapIterator = dupLinesMap.entrySet().iterator();
				
				/* For each line, report it in the report file and count
				 * the duplicates as well.  I might be able to use size,
				 * but if it's big, HashMap may be unreliable.
				 */
				List<String> dupFileList = new ArrayList<>();
				int dupCount=0;
				while ( dupMapIterator.hasNext() ) {

					Map.Entry<String,String> mapEntry = dupMapIterator.next();

					dupFileWriter.println(mapEntry.getValue());
					dupFileWriter.println(mapEntry.getKey());
					dupFileWriter.println();
					
					/* Save the list of files with duplicates, but eliminate
					 * duplicates in the list of files itself!
					 */
					if ( !dupFileList.contains(mapEntry.getValue()) ) {
						dupFileList.add(mapEntry.getValue());
					}
					
					dupCount++;					
					
				} /* end of while there are duplicate entries */				
				
				/* If we had duplicates, introduce them in the 
				 * report file.
				 */
				if ( dupFileList.size() > 0) {
					dupFileWriter.println();
					dupFileWriter.println("Files that had duplicates:");
					Collections.sort(dupFileList);
					System.out.println("Duplicates can be found in " + dupFilePath.toString());
				}
				
				for (int i=0; i<dupFileList.size(); i++ ) {
					dupFileWriter.println(dupFileList.get(i));
				}
				dupFileWriter.close();

				System.out.println("Duplicate count = " + dupCount);
				
			} /* end of try */
			
			catch (FileNotFoundException fnfound) {
				System.err.println(fnfound.getMessage());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
			finally {		
			}
				
		} /* end of try  */
		finally {		
			System.out.println("Search completed.");
		}			
	
	} /* end of main */
	
	/* this method gets a list of file names in a directory */
	static List<Path> listSourceFiles(Path dir) throws IOException {
		
	    List<Path> result = new ArrayList<>();
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*")) {
	    	for (Path entry: stream) {
	    		result.add(entry);
	    	}
	    } catch (DirectoryIteratorException ex) {
	    	/* I/O error encountered during the iteration, the cause is an IOException */
	    	throw ex.getCause();
	    }
	    
	    return result;
	    
	} /* end of listSourceFiles method */

} /* end of CheckForDups class */
