package odt.tool;

import java.nio.file.Path;
import java.nio.file.Paths;


/* This class contains all sorts of wonderful things about the ODT
 * directory structure.  There are methods to set it up and get folder
 * information.
 * Plus there's a whole section about updating lib files from the website.
 */
public class DirectorySetup {
	
	public static Path odtFilePath;
	public static Path libFilesPath;
	private static String checkPathString;
	private static String mccFilesPathString;	
	public boolean setupComplete;
	
	public DirectorySetup () {
		
		setupComplete = false;
		
		try {
			/* Get the path for ODT files */
			odtFilePath = Paths.get(System.getProperty("user.home"),"ODT");
			if ( ! odtFilePath.toFile().isDirectory() ) {
				/* try to make the folder */
				odtFilePath.toFile().mkdir();
				
				if ( ! odtFilePath.toFile().isDirectory() ) {
					/* We can't use the folder we're used to, so default to the user's
					 * current folder.
					 */
					System.err.println("Unable to use ODT path: " + odtFilePath.toString());
					System.err.println("Cannot proceed");
					return;
				}
			}
			
			/* Set up library files path */
			Path libPath = Paths.get(odtFilePath.toString(), "lib");
			if ( ! libPath.toFile().isDirectory() ) {
				
				/* try to make the folder */
				libPath.toFile().mkdir();
				
				if ( ! libPath.toFile().isDirectory() ) {			
					/* We can't work without the library files path.
					 * We need the files in that folder.				
					 */
					System.err.println("Unable to find ODT library path: " + libPath.toString());
					System.err.println("Cannot proceed");
					return;
				}
			}
			
			/* Save the path we successfully got */
			setLibFilesPath(libPath);
			
			/* Set up Check file path */
			Path checkPath = Paths.get(odtFilePath.toString(), "Check");
			if ( ! checkPath.toFile().isDirectory() ) {
				
				/* try to make the folder */
				checkPath.toFile().mkdir();
				
				if ( ! checkPath.toFile().isDirectory() ) {			
					/* We can't work without the library files path.
					 * We need the files in that folder.				
					 */
					System.err.println("Unable to find ODT Check path: " + checkPath.toString());
					System.err.println("Cannot proceed");
					return;
				}
			}
			
			/* Save the path string we successfully got */			
			setCheckPathString(checkPath.toString());

		} catch ( Exception e ) {
			System.err.println(e.getMessage());
		}
				
		setupComplete = true;
		
		return;
	
	} /* end of constructor */

	/* Set up the folder used by the final moderator */
	public static boolean setMCCFolder() {
		
		boolean setupOK = false;
		
		Path mccFilesPath;
		mccFilesPathString = "Invalid";
		
		try {
			mccFilesPath = Paths.get(System.getProperty("user.home"),"Obits");
			if ( ! mccFilesPath.toFile().isDirectory() ) {
				System.err.println("Invalid path: " + mccFilesPath.toString());
				System.err.println("Cannot proceed");
				mccFilesPathString = "Invalid";
			}
			else {
				mccFilesPathString = mccFilesPath.toString();
				setupOK = true;
			}
			
			return(setupOK);

		} catch ( Exception e ) {
			System.err.println(e.getMessage());
			return (setupOK);
		}
	}
	
	/* Get  the folder used by the final moderator */
	public String getMCCFolder() {
				
		return ( mccFilesPathString );
	}

	/* Less Java version of setting up the directory structure */
	public DirectorySetup getDirectorySetup (DirectorySetup ODTPaths) {
		
		ODTPaths.setupComplete = false;
		setupComplete = false;
		
		/* Get the path for ODT files */
		DirectorySetup.odtFilePath = Paths.get(System.getProperty("user.home"),"ODT");
		odtFilePath = Paths.get(System.getProperty("user.home"),"ODT");
		if ( ! DirectorySetup.odtFilePath.toFile().isDirectory() ) {
			/* The directory is undefined */
			System.err.println("Unable to use ODT path: " + DirectorySetup.odtFilePath.toString());
			System.err.println("Cannot proceed");
			return (ODTPaths);				
		}
		
		/* Get library files path */
		DirectorySetup.libFilesPath = Paths.get(DirectorySetup.odtFilePath.toString(), "lib");
		if ( ! DirectorySetup.libFilesPath.toFile().isDirectory() ) {			
				/* We can't work without the library files path.
				 * We need the files in that folder.				
				 */
				System.err.println("Unable to find ODT library path: " + DirectorySetup.libFilesPath.toString());
				System.err.println("Cannot proceed");
				return(ODTPaths);
		}
		
		ODTPaths.setupComplete = true;
		
		return(ODTPaths);
	
	} /* end of getDirectorySetup */
	
	/* Get the requested sub-folder under the ODT main path */
	public static String getODTSubFolderString ( String subFolderName ) {
		
		String folderPathString = "Invalid";
		
		try {
			Path folderPath = Paths.get(odtFilePath.toString(), subFolderName);
			if ( ! folderPath.toFile().isDirectory() ) {
				/* try to make the folder */
				folderPath.toFile().mkdir();
				
				if ( ! folderPath.toFile().isDirectory() ) {

					System.err.println("Unable to use subfolder: " + folderPath.toString());
					return(folderPathString);
				}				
			}
			
			folderPathString = folderPath.toString();

		} catch ( Exception e ) {
			System.err.println(e.getMessage());
		}
		
		return (folderPathString);
		
	} /* end of getODTSubFolderString */
	
	public static Path getLibFilesPath() {
		return libFilesPath;
	}

	public void setLibFilesPath(Path libFilesPath) {
		DirectorySetup.libFilesPath = libFilesPath;
	}

	public boolean isSetupComplete() {
		return setupComplete;
	}

	public void setSetupComplete(boolean setupComplete) {
		this.setupComplete = setupComplete;
	}

	public static Path getOdtFilePath() {
		return odtFilePath;
	}

	public static String getCheckPathString() {
		return checkPathString;
	}

	public void setCheckPathString(String checkPathString) {
		DirectorySetup.checkPathString = checkPathString;	
	}
	
	public static String getMccFilesPathString() {
		return mccFilesPathString;
	}


	
} /* end of class DirectorySetup */
