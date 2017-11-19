package odt.tool;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class StringUtils {

	  static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); 
	  
	  public static boolean isPureAscii(String charString) {
		  return asciiEncoder.canEncode(charString);
	  }

//	  public static void main (String args[])
//	    throws Exception {

//	     String test = "Réal";
//	     System.out.println(test + " isPureAscii() : " + StringUtils.isPureAscii(test));
//	     test = "Real";
//	     System.out.println(test + " isPureAscii() : " + StringUtils.isPureAscii(test));

	     /*
	      * output :
	      *   Réal isPureAscii() : false
	      *   Real isPureAscii() : true
	      */
//	  }
//	}

}
