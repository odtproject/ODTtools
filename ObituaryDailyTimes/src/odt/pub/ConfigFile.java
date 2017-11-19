package odt.pub;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import odt.tool.DirectorySetup;

public class ConfigFile {
	private static String FTPhost;
	private static String FTPuser;
	private static String FTPpasswd;
	
	public ConfigFile () {
		setFTPhost(null);
		setFTPuser(null);
		setFTPpasswd(null);
	}
	
	public static String getFTPhost() {
		return FTPhost;
	}

	public static String getFTPuser() {
		return FTPuser;
	}

	public static String getFTPpasswd() {
		return FTPpasswd;
	}

	public static void setFTPhost(String fTPhost) {
		FTPhost = fTPhost;
	}

	public static void setFTPuser(String fTPuser) {
		FTPuser = fTPuser;
	}

	public static void setFTPpasswd(String fTPpasswd) {
		FTPpasswd = fTPpasswd;
	}
	
	public static boolean readConfig () {
		Properties configFile = new Properties();
		InputStream input = null;
		boolean result = true;
		
		try {
			/* Set up website files path */
			Path ODTPath = DirectorySetup.getOdtFilePath();
			Path FTPConfigPath = Paths.get(ODTPath.toString(),"ftp.config");
			
			input = new FileInputStream(FTPConfigPath.toFile());
			
			/* load config properties file */
			configFile.load(input);
			
			/* Get the config values */
			setFTPhost(configFile.getProperty("host"));
			setFTPuser(configFile.getProperty("username"));
			setFTPpasswd(configFile.getProperty("password"));
		}
		catch ( Exception e ) {
			result = false;
			System.err.println("Exception during readConfig " + e.getMessage());
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
		
	} /* end of readConfig */
	
	public static boolean storeConfig( String FTPhost, String FTPusername, String FTPpasswd) {
		Properties config = new Properties();
		OutputStream output = null;
		boolean result = true;
		
		try {
			/* Set up website files path */
			Path ODTPath = DirectorySetup.getOdtFilePath();
			Path FTPConfigPath = Paths.get(ODTPath.toString(),"ftp.config");
			
			output = new FileOutputStream(FTPConfigPath.toFile());
			
			/* set config properties values */
			config.setProperty("host", FTPhost);
			config.setProperty("username", FTPusername);
			config.setProperty("password", FTPpasswd);
			
			/* save properties to config file */
			config.store(output, "FTP access information");
		}
		catch ( Exception e ) {
			result = false;
			System.err.println("Error in storeConfig trying to write config file: " + e.getMessage());
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
		
	} /* end of storeConfig */
	
}
