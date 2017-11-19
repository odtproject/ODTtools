package odt.ftool;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JOptionPane;

import odt.tool.DirectorySetup;
import odt.ftool.IssueConfig;
import odt.pub.UploadOdtFile;

public class ObitIssue {
	
	private static String issueFileName;
	private static Path dbFilePath;
			
	public static Path getDbFilePath() {
		return dbFilePath;
	}

	public static String getIssueFileName() {
		return issueFileName;
	}

	public static void setIssueFileName(String issFileName) {
		issueFileName = issFileName;
	}

	public static boolean doObitIssue( String fileToCheck, String[] args) 
	{
		/* If file name is empty that's all we do */
		if ( fileToCheck.equals("") )
		{
			JOptionPane.showMessageDialog(null, "Filename empty!", "Create Issue message", JOptionPane.ERROR_MESSAGE);						
			setIssueFileName("");
			return false;
		}
		
		/* Get the path for ObitEdit files */
		DirectorySetup ODTpaths = new DirectorySetup();
		
		/* Set up the ODT directory structure.  If it fails, we're done. */
		if ( ! ODTpaths.isSetupComplete() ) {			
			return false;
		}
		
		/* If the input File does not exist, we're done */
		Path checkFilePath = Paths.get(DirectorySetup.getCheckPathString(), fileToCheck);
		String checkFileIn = checkFilePath.toString();
		if ( !checkFilePath.toFile().exists() ) 
		{
			System.err.println("File does not exist: " + checkFileIn);
			
			return false;
			
		} /* end of if file to check does not exist */	
		
		/* Set up email file path -- for location of file to be emailed */
		Path emailFilePath = Paths.get(DirectorySetup.odtFilePath.toString(), "Email");
		if ( ! emailFilePath.toFile().isDirectory() ) 
		{
			/* We can't use the folder we're used to, so return.
			 */
			System.err.println("Unable to use ODT email files path: " + emailFilePath.toString());
			return false;
		}

		/* Set up dbfiles file path -- for location of database file */
		dbFilePath = Paths.get(DirectorySetup.odtFilePath.toString(), "dbfiles");
		if ( ! dbFilePath.toFile().isDirectory() ) 
		{
			/* We can't use the folder we're used to, so return. */
			System.err.println("Unable to use ODT dbfiles path: " + dbFilePath.toString());
			return false;
		}
				
		/* Get the volume and issue number for this issue of the ODT. */
		int issue;		
		String volume, year;
		
		/* If we are able to read the issue config file, then save the values.
		 * Otherwise, we can not proceed.
		 */
		if ( IssueConfig.readIssueConfig() )
		{
			issue = Integer.parseInt(IssueConfig.getIssueNum());
			volume = IssueConfig.getIssueVol();
			year = IssueConfig.getIssueYear();
		}
		else
		{
			System.err.println("Cannot get issue configuration");
			return false;
		}	
		
		/* Increment the issue number and store it.  
		 * If we cannot store it, then we are finished.
		 */
		issue++;
		
		if ( ! IssueConfig.storeIssConfig(volume, Integer.toString(issue), year) )
		{
			System.err.println("Cannot store issue configuration");
			return false;
		}		
		
		/* Report information to the user that we're starting */
		System.out.println("---------------------------");
		System.out.println("Generating Obituary Daily times issue:");
		System.out.println("Volume " + volume + " issue " + issue); 

		/* put together the header string */
		String headerString = String.format("v%sno%03d", volume, issue);
		
		/* Save the name of this issue */
		ObitIssue.setIssueFileName(headerString);

		/* use that to make the name of the email file */
		String emailFileName = headerString + ".txt";

		/* Proceed with assembling the email issue file */
		try 
		{			
			/* Add the email output file name to the path and open it for writing */
			emailFilePath = Paths.get(emailFilePath.toString(), emailFileName);
			String emailFile = emailFilePath.toString();
			PrintStream emailFileWriter = new PrintStream(new File(emailFile));
			
			emailFileWriter.println("Obituary Daily Times " + headerString);
			
			/* Get the header lines for the email */
			Path headerFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "email_header.txt");			
			Scanner headerFileScanner = new Scanner(new File(headerFilePath.toString()));
			
			/* While there are lines in the header file, write them to the email file */
			while ( headerFileScanner.hasNextLine() ) {
				emailFileWriter.println(headerFileScanner.nextLine());
			}
			
			/* close the header file scanner */
			if (headerFileScanner!=null) {
				headerFileScanner.close();	
			}

			/* Add the database output file name to the path and open it for writing */
			dbFilePath = Paths.get(dbFilePath.toString(), headerString);
			String dbFile = dbFilePath.toString();
			PrintStream dbFileWriter = new PrintStream(new File(dbFile));
			
			/* Open the input file and copy it to the email file and the database
			 * file.  Count the lines as we go.
			 */
			Scanner inputFileScanner = new Scanner(new File(checkFileIn));
			
			String obitLine;
			int obitCount = 0;
			
			while ( inputFileScanner.hasNextLine() ) {
				/* get the line */
				obitLine = inputFileScanner.nextLine();
				
				/* write the line to the email file */
				emailFileWriter.println(obitLine);
				
				/* write the line with a different line separator for the database */
				dbFileWriter.print(obitLine + "\n");
				
				/* increment the count */
				obitCount++;				
				
			} /* end while there are lines in the input file */			
			
			/* close the database file */
			if (dbFileWriter!=null) {
				dbFileWriter.close();
			}
			
			/* Get the footer lines for the email */
			Path footerFilePath = Paths.get(DirectorySetup.libFilesPath.toString(), "email_footer.txt");			
			Scanner footerFileScanner = new Scanner(new File(footerFilePath.toString()));
			
			/* While there are lines in the footer file, write them to the email file */
			while ( footerFileScanner.hasNextLine() ) {
				emailFileWriter.println(footerFileScanner.nextLine());
			}
			
			/* close the footer file scanner */
			if (footerFileScanner!=null) {
				footerFileScanner.close();	
			}
			
			/* Write the final count to the email file */
			emailFileWriter.printf("Obitcount %d%n", obitCount);
			
			/* Tell the user how many lines there were and include a reminder */
			System.out.println(headerString);
			System.out.printf("%nObitcount: %d%n", obitCount);
			System.out.println();
			System.out.println("Turn off Word Wrap before sending email");
			System.out.println("Upload database file to website");
			
			/* Close any open resources */
			if (emailFileWriter!=null) {
				emailFileWriter.close();
			}			
			if (inputFileScanner!=null) {
				inputFileScanner.close();
			}			
			
		} /* end of try file accesses */
		
		catch (FileNotFoundException fnf) {
			System.err.println("ObitEdit: File not found: " + fnf.getMessage());
			return false;
		}
		
		return true;
		
	} /* end of doObitIssue */
	
	/* Method to distribute the database to all the right places */
	public static void distribDatabase ( )
	{
		/* Get the database file name.  If it's null then there's nothing more
		 * to do.
		 */
		String dbFile = ObitIssue.getIssueFileName();
		if ( dbFile.equals("") )
		{
			JOptionPane.showMessageDialog(null, "No database file to process.", "Database distribution message", JOptionPane.ERROR_MESSAGE);
		}
		else 
		{
			/* Get the path to the file and the year of the issue */
			Path filePath = ObitIssue.getDbFilePath();
			String year = IssueConfig.getIssueYear();
			
			/* If the file is uploaded correctly, let the user know. */
			if ( UploadOdtFile.uploadDbFile(filePath.toString(), dbFile, year) )
			{
				JOptionPane.showMessageDialog(null, "File uploaded!");
			}
			else 
			{
				JOptionPane.showMessageDialog(null, "Failed to upload database file.", "Database distribution message", JOptionPane.ERROR_MESSAGE);						
			}
			
			/* Next we want to copy the generated database file to the folder
			 * where it can be checked against in future checks.
			 * Get the path to the generated file and the path to the folder
			 * for checking against.  Then try to copy it there.
			 */
			Path fromFilePath = filePath;
			Path toFilePath = Paths.get(DirectorySetup.getMccFilesPathString(), "dbfiles");
			toFilePath = Paths.get(toFilePath.toString(), dbFile);
			
			try
			{
				Files.copy(fromFilePath, toFilePath);
				
				/* That worked so now we'll try to move it to the place where
				 * all the database files live on the final moderator's machine.
				 * There's a folder for each year.
				 */
				toFilePath = Paths.get(DirectorySetup.getMccFilesPathString(), "dbfilesall");
				toFilePath = Paths.get(toFilePath.toString(), year);
				toFilePath = Paths.get(toFilePath.toString(), dbFile);
				
				Files.move(fromFilePath, toFilePath);						
				JOptionPane.showMessageDialog(null, "File copied and moved!");
			}
			catch (IOException ioe)
			{
				System.err.println("Failed to copy or move file: " + dbFile + ioe.getMessage());
			}
			
		} /* end of else the dbFile is set */

	} /* end of distribDatabase */

} /* end of ObitIssue class */
