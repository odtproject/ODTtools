package odt.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* 
 * This class handles lib files for ODT.
 */
public class LibFiles 
{
	final static String ODTURL = "http://www.rootsweb.ancestry.com/~obituary/";
	private static final int BUFFER_SIZE = 4096;
	String urlString;
	public List<Path> libFilesPathList;

	/* Try to download a new lib file.  If we aren't successful, oh well */
	public static boolean downloadWebFile ( String localFile, String webFile ) 
	{				
		try {
			/* Set up a connection to the website */
			String urlString = ODTURL + webFile;
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			
			int responseCode = urlConnection.getResponseCode();
			
			/* If the file was found on the website, copy it to the desired 
			 * local file.
			 */
			if (  responseCode == HttpURLConnection.HTTP_OK ) {
				
				/* Get an input stream from the website, and an output stream
				 * to the local file.
				 */
				InputStream inputStream = urlConnection.getInputStream();
				FileOutputStream fileOutStream = new FileOutputStream(localFile);
				
				/* Copy the file chunk by chunk */
	            int bytesRead = -1;
	            byte[] buffer = new byte[BUFFER_SIZE];
	            while ((bytesRead = inputStream.read(buffer)) != -1) {
	            	fileOutStream.write(buffer, 0, bytesRead);
	            }

	            /* Close the streams */
	            inputStream.close();
	            fileOutStream.close();
	            
	            return(true);
	            
			} /* end of if the file was found */	

		} catch (Exception e) {
			System.err.println("downloadWebFile exception: " + e.getMessage());
		}
		
		return( false );

	} /* end of downloadWebFile */
	
	/* Get the age of the file on the website */
	public static long getWebAge ( String fileName ) 
	{
		String urlString = ODTURL + fileName;
		
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			
			int responseCode = urlConnection.getResponseCode();
			
			if (  responseCode == HttpURLConnection.HTTP_OK ) {
				/* Get today's date in milliseconds */
				Date todayDate = Calendar.getInstance().getTime();
				
				/* Get last modified date of file in milliseconds */
				Date webFileDate = new Date(urlConnection.getLastModified());
				long modifiedInMillies = webFileDate.getTime();
				
				/* Get the difference in those to dates in milliseconds and
				 * convert it to days.
				 */
				long diffInMillies = todayDate.getTime() - modifiedInMillies;
			    long webFileAge = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);

			    return ( webFileAge);
			}
			
		} catch (MalformedURLException e1) {
			System.err.println("getWebAge Malformed URL " + e1.getMessage());
		} catch (IOException e1) {			
			System.err.println("getWebAge IO Exception " + e1.getMessage());
		}
		
		return ( -1 );
		
	} /* end of getWebAge */
	
	/* Get the age of a file */
	public static long getFileAge ( Path filePath ) 
	{
		Date fileDate = new Date(filePath.toFile().lastModified());
		Date todayDate = Calendar.getInstance().getTime();
		long diffInMillies = todayDate.getTime() - fileDate.getTime();
		long ageOfFile = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    
	    return (ageOfFile);
	    
	} /* end of getFileAge */
	
	/* this method gets a list of file names in a directory */
	public static List<Path> listSourceFiles(Path dir) 
	{
	    List<Path> result = new ArrayList<>();
	    
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*")) {
	    	for (Path entry: stream) {
	    		result.add(entry);
	    	}
	    } catch (Exception ex) {
	    	/* I/O error encountered during the iteration, the cause is an IOException */
	    	System.err.println("listSourceFiles exception: " + ex.getMessage());;
	    }
	    
	    return result;
	    
	} /* end of listSourceFiles method */
	
} /* end of LibFiles class */
