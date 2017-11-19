package odt.ftool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import odt.tool.DirectorySetup;

public class IssueConfig {
	private static String IssueVol;
	private static String IssueNum;
	private static String IssueYear;
	
	public IssueConfig () {
		setIssueVol("0");
		setIssueNum("0");
		setIssueYear("0");
	}

	public static String getIssueVol() {
		return IssueVol;
	}

	public static void setIssueVol(String issueVol) {
		IssueVol = issueVol;
	}

	public static String getIssueNum() {
		return IssueNum;
	}

	public static void setIssueNum(String issueNum) {
		IssueNum = issueNum;
	}

	public static String getIssueYear() {
		return IssueYear;
	}

	public static void setIssueYear(String issueYear) {
		IssueYear = issueYear;
	}

	/* This method will read the issue configuration from the ODT
	 * folder and set them in the class object.
	 * DirectorySetup needs to be called before this.
	 */
	public static boolean readIssueConfig() {
		
		Properties configFile = new Properties();
		InputStream input = null;
		boolean result = true;

		try {
			/* Set up ODT files path */
			Path ODTPath = DirectorySetup.getOdtFilePath();
			Path issueConfigPath = Paths.get(ODTPath.toString(),"issue.config");
			
			input = new FileInputStream(issueConfigPath.toFile());
			
			/* load config properties file */
			configFile.load(input);
			
			/* Get the config values */
			setIssueVol(configFile.getProperty("volume"));
			setIssueNum(configFile.getProperty("issue"));
			setIssueYear(configFile.getProperty("year"));
			
		} /* end of try */
		
		catch (FileNotFoundException fnf) 
		{
			result = false;
			System.err.println("readIssueConfig: File not found: " + fnf.getMessage());
		}
		
		catch (Exception e)
		{
			result = false;
			System.err.println("readIssueConfig: Exception: " + e.getMessage());
		}
		finally {
			if ( input != null ) {
				try {
					input.close();
				}
				catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		}
		return result;

	} /* end of readIssueConfig() */

	public static boolean storeIssConfig( String issueVol, String issueNum, String issueYear) 
	{
		Properties config = new Properties();
		OutputStream output = null;
		boolean result = true;
		
		try {
			/* Set up website files path */
			Path ODTPath = DirectorySetup.getOdtFilePath();
			Path FTPConfigPath = Paths.get(ODTPath.toString(),"issue.config");
			
			output = new FileOutputStream(FTPConfigPath.toFile());
			
			/* set config properties values */
			config.setProperty("volume", issueVol);
			config.setProperty("issue", issueNum);
			config.setProperty("year", issueYear);
			
			/* save properties to config file */
			config.store(output, "Issue information");
		}
		catch ( Exception e ) {
			result = false;
			System.err.println("Error in storeIssConfig trying to write config file: " + e.getMessage());
		}
		finally {
			if (output != null) {
				try {
					output.close();
				}
				catch ( final Exception e ) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
		
	} /* end of storeIssConfig */

}
