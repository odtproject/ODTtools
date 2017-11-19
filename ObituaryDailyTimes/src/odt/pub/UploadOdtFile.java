package odt.pub;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import odt.tool.DirectorySetup;
import odt.util.LibFiles;

/* 
 * This class handles Uploading files to the website for ODT.
 */
public class UploadOdtFile 
{
	FTPClient ftp = null;
	
	/* The constructor takes care of setting up the connection to the website */
	public UploadOdtFile (String host, String user, String pwd) throws Exception 
	{
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);
        
        /* Use binary mode (even though they are ASCII files) to preserve
         * the carriage returns.
         */
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }
	
	/* This does the actual upload */
	public void uploadFile(String localFileFullName, String fileName, String hostDir)
            throws Exception 
	{
        try (InputStream input = new FileInputStream(new File(localFileFullName)))
        {
        	this.ftp.storeFile(hostDir + fileName, input);
        }
    }

	/* This handles the disconnect part */
	public void disconnect()
	{
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (Exception f) {
                System.err.println("UploadOdtFile.disconnect exception: " + f.getMessage());
            }
        }
    }
 
	/* Try to upload files in list.  The filename needs to be the full path name.
	 * If we aren't successful, report it.
	 */
	public static boolean uploadOdtFiles ( ArrayList<String> localFiles ) 
	{		
		String webFile;
		String host, user, pw;
		
		/* If the list is empty, we have nothing to do */
		if ( localFiles.size() <= 0 )
		{
			return false;
		}
		
		/* try to read the FTP config file; if it fails, we're done */
		if ( ! ConfigFile.readConfig() ) {
			return false;
		}
		
		/* get the FTP login information */
		host = ConfigFile.getFTPhost();
		user = ConfigFile.getFTPuser();
		pw = ConfigFile.getFTPpasswd();
		
		try {
			
			UploadOdtFile ftpUploader = new UploadOdtFile(host, user, pw);
			
			/* FTP server paths are relative.  In our case, we want files to go in
			 * the public_html folder (except for database files).
			 */
			for ( String localFile: localFiles ) {
				
				/* Get just the file name for the website */
				webFile = Paths.get(localFile).getFileName().toString();
				System.out.println("Uploading " + webFile);
				
				/* Call the FTP uploader code */
				ftpUploader.uploadFile(localFile, webFile, "/public_html/");
			}
	        
	        ftpUploader.disconnect();
		    
            return(true);  

		} catch (Exception e) {
			System.err.println("uploadOdtFile upload exception: " + e.getMessage());
		}
		
		return( false );

	} /* end of uploadOdtFile */	
	
	/* Try to upload a file.  The filename needs to be the full path name.
	 * The folder is the folder on the website where it should end up.
	 * If we aren't successful, report it.
	 */
	public static boolean uploadDbFile ( String localFilePath, String webFile, String folder ) 
	{		
		String host, user, pw;
		
		/* try to read the FTP config file; if it fails, we're done */
		if ( ! ConfigFile.readConfig() ) {
			return false;
		}
		
		/* get the FTP login information */
		host = ConfigFile.getFTPhost();
		user = ConfigFile.getFTPuser();
		pw = ConfigFile.getFTPpasswd();
		
		try {
			UploadOdtFile ftpUploader = new UploadOdtFile(host, user, pw);
			
			/* FTP server paths are relative.  In our case, we want files to go in
			 * the specified database folder.
			 * Get just the file name for the website.
			 */
	        ftpUploader.uploadFile(localFilePath, webFile, "/" + folder + "/");
	        ftpUploader.disconnect();
		    
            return(true);  

		} catch (Exception e) {
			System.err.println("uploadOdtFile exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		return( false );

	} /* end of uploadDbFile */	
	
	/* This method uploads any new lib files to the web site. */
	public static void uploadLibFiles(DirectorySetup dirSetup) 
	{
		ArrayList<String> libFileList = new ArrayList<>();
		
		/* Set up lib files path to find files */
		String libFileString = DirectorySetup.getLibFilesPath().toString();
		if ( libFileString.equals("Invalid") ) {
			return;
		}
		Path libFilesPath = Paths.get(libFileString);
		
		/* Get a list of the files in the lib folder */
		LibFiles libFiles = new LibFiles();
		libFiles.libFilesPathList = LibFiles.listSourceFiles(libFilesPath);
				
		/* We want to see if any of the lib files are newer versions than those
		 * on the website.
		 */
		try {
			Iterator<Path> fileIterator = libFiles.libFilesPathList.iterator();
			Path libFilePath;
			long libFileAge, webFileAge;
			String optionMessage, libFileName, fileMessage;
			optionMessage = "\nNewer version for the website\nWould you like to upload it?";
			int answer;
			
			/* loop through the files in the list and check each one */
			while ( fileIterator.hasNext() )  {
				
				/* get the file name */
				libFilePath = fileIterator.next();
				
				/* Get the age of the file */
				libFileAge = LibFiles.getFileAge(libFilePath);
				
				/* Get the age of the file on the website */
				libFileName = libFilePath.getFileName().toString();
				webFileAge = LibFiles.getWebAge(libFileName);
				
				/* If the web file exists and is older than the lib file,
				 * and the user is okay with it, add the newer file to the list 
				 * of files to be uploaded.
				 */
				if ( webFileAge >= 0 && webFileAge > libFileAge ) 
				{
					fileMessage = "File " + libFileName;

					answer = JOptionPane.showConfirmDialog(null, fileMessage + optionMessage , "ODT Option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					
					if ( answer == JOptionPane.YES_OPTION ) {
						libFileList.add(libFilePath.toString());
					}
					
				} /* end of if the web file is older than the existing file */
								
			} /* end of while there is another file in the list */

		} /* end of try lib file check */ 
		
		catch (Exception e) {			
			e.printStackTrace();
		}
		
		/* Try to upload files and tell user the result */
		if ( UploadOdtFile.uploadOdtFiles( libFileList ) ) {
			JOptionPane.showMessageDialog(null, "Files uploaded!");
		}
		else {
			JOptionPane.showMessageDialog(null, "No files were uploaded.");
		}
		
	} /* end uploadLibFiles */

} /* end of UpLoadOdtFile class */
