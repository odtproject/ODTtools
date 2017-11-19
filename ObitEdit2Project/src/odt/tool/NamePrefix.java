package odt.tool;
import java.util.ArrayList;
import java.util.HashMap;

/* This class keeps track of the acceptable prefixes
 * in last names.
 */
public class NamePrefix {

	/* fields in this class */
	HashMap<String, String> capToProper;
	ArrayList<String >properPrefix;
	String prefix;
	
	public static void initPrefixMaps( NamePrefix prefixMap ) 
	{		
		prefixMap.capToProper = new HashMap<String,String>();
		prefixMap.properPrefix = new ArrayList<>();
		
		prefixMap.capToProper.put("AU", "Au");
		prefixMap.capToProper.put("EL", "El");
		prefixMap.capToProper.put("AL", "Al");
		prefixMap.capToProper.put("BEN", "Ben");
		prefixMap.capToProper.put("MC", "Mc");
		prefixMap.capToProper.put("MAC", "Mac");
		prefixMap.capToProper.put("ST", "St");
		prefixMap.capToProper.put("STE", "Ste");
		prefixMap.capToProper.put("MT", "Mt");
		prefixMap.capToProper.put("DE", "De");
		prefixMap.capToProper.put("SANTA", "Santa");
		prefixMap.capToProper.put("SanDe", "SanDe");
		prefixMap.capToProper.put("SAN", "San");
		prefixMap.capToProper.put("VAN", "Van");
		prefixMap.capToProper.put("VON", "Von");
		prefixMap.capToProper.put("DA", "Da");
		prefixMap.capToProper.put("DI", "Di");
		prefixMap.capToProper.put("DU", "Du");
		prefixMap.capToProper.put("LA", "La");
		prefixMap.capToProper.put("LE", "Le");
		prefixMap.capToProper.put("LI", "Li");
		prefixMap.capToProper.put("LO", "Lo");
		prefixMap.capToProper.put("VO", "Vo");
		prefixMap.capToProper.put("LOS", "Los");
		prefixMap.capToProper.put("LAS", "Las");
		prefixMap.capToProper.put("DALLA", "Dalla");
		prefixMap.capToProper.put("DELA", "DeLa");
		prefixMap.capToProper.put("DeLA", "DeLa");
		prefixMap.capToProper.put("DELAS", "DeLas");
		prefixMap.capToProper.put("DeLAS", "DeLas");
		prefixMap.capToProper.put("DeLas", "DeLas");
		prefixMap.capToProper.put("DELOS", "DeLos");
		prefixMap.capToProper.put("DeLOS", "DeLos");
		prefixMap.capToProper.put("DeST", "DeSt");
		prefixMap.capToProper.put("DEL", "Del");
		prefixMap.capToProper.put("DELL", "Dell");
		prefixMap.capToProper.put("DELLE", "Delle");
		prefixMap.capToProper.put("DELLA", "Della");
		prefixMap.capToProper.put("DELLO", "Dello");
		prefixMap.capToProper.put("DEN", "Den");
		prefixMap.capToProper.put("DER", "Der");
		prefixMap.capToProper.put("VER", "Ver");
		prefixMap.capToProper.put("DES", "Des");
		prefixMap.capToProper.put("DOS", "Dos");
		prefixMap.capToProper.put("TE", "Te");
		prefixMap.capToProper.put("TEN", "Ten");
		prefixMap.capToProper.put("VANDE", "Vande");
		prefixMap.capToProper.put("VanDE", "Vande");
		prefixMap.capToProper.put("VANMC", "VanMc");
		prefixMap.capToProper.put("VanMC", "VanMc");
		prefixMap.capToProper.put("VANDER", "Vander");
		prefixMap.capToProper.put("VanDER", "VanDer");
		prefixMap.capToProper.put("VANDEN", "Vanden");
		prefixMap.capToProper.put("VanDEN", "VanDen");
		prefixMap.capToProper.put("VONDER", "Vonder");
		prefixMap.capToProper.put("VonDER", "VonDer");

		prefixMap.properPrefix.add("Al");
		prefixMap.properPrefix.add("Au");
		prefixMap.properPrefix.add("Ben");
		prefixMap.properPrefix.add("El");
		prefixMap.properPrefix.add("Ap");
		prefixMap.properPrefix.add("Mc");
		prefixMap.properPrefix.add("Mac");
		prefixMap.properPrefix.add("St");
		prefixMap.properPrefix.add("Ste");
		prefixMap.properPrefix.add("Mt");
		prefixMap.properPrefix.add("De");
		prefixMap.properPrefix.add("San");
		prefixMap.properPrefix.add("Santa");
		prefixMap.properPrefix.add("SanDe");
		prefixMap.properPrefix.add("Van");
		prefixMap.properPrefix.add("Von");
		prefixMap.properPrefix.add("Da");
		prefixMap.properPrefix.add("Ma");
		prefixMap.properPrefix.add("Fitz");
		prefixMap.properPrefix.add("Dal");
		prefixMap.properPrefix.add("Di");
		prefixMap.properPrefix.add("Du");
		prefixMap.properPrefix.add("La");
		prefixMap.properPrefix.add("Le");
		prefixMap.properPrefix.add("Li");
		prefixMap.properPrefix.add("Lo");
		prefixMap.properPrefix.add("Vo");
		prefixMap.properPrefix.add("Los");
		prefixMap.properPrefix.add("Las");
		prefixMap.properPrefix.add("Dalla");
		prefixMap.properPrefix.add("DeLa");
		prefixMap.properPrefix.add("Dela");
		prefixMap.properPrefix.add("Dell");
		prefixMap.properPrefix.add("Della");
		prefixMap.properPrefix.add("Delle");
		prefixMap.properPrefix.add("Dello");
		prefixMap.properPrefix.add("DeLos");
		prefixMap.properPrefix.add("Delos");
		prefixMap.properPrefix.add("DeLas");
		prefixMap.properPrefix.add("Delas");
		prefixMap.properPrefix.add("Del");
		prefixMap.properPrefix.add("Dell");
		prefixMap.properPrefix.add("Den");
		prefixMap.properPrefix.add("Der");
		prefixMap.properPrefix.add("Des");
		prefixMap.properPrefix.add("DeSt");
		prefixMap.properPrefix.add("Dos");
		prefixMap.properPrefix.add("Ver");
		prefixMap.properPrefix.add("Ten");
		prefixMap.properPrefix.add("Te");
		prefixMap.properPrefix.add("Vander");
		prefixMap.properPrefix.add("Vande");
		prefixMap.properPrefix.add("VanDe");
		prefixMap.properPrefix.add("VanMc");
		prefixMap.properPrefix.add("Vanden");
		prefixMap.properPrefix.add("VanDen");
		prefixMap.properPrefix.add("VanDer");
		prefixMap.properPrefix.add("Vonder");
		prefixMap.properPrefix.add("VonDer");
		
	} /* end of method initPrefixMap */
	
	/* This method checks the prefix in question to see if it
	 * is a capitalized version of an acceptable prefix and
	 * returns the acceptable version of that prefix.
	 */
	public static String checkCapPrefix( NamePrefix prefixToCheck ) 
	{		
		String prefix;
		
		prefix = prefixToCheck.capToProper.get(prefixToCheck.prefix);
		
		return( prefix );
		
	} /* end of method checkCapPrefix */
	
	/* This method checks the list of acceptable prefixes and returns
	 * true if it finds the prefix in question in the list.
	 */
	public static boolean checkProperPrefix ( NamePrefix prefixToCheck ) 
	{		
		boolean isPrefix;
		
		isPrefix = prefixToCheck.properPrefix.contains(prefixToCheck.prefix);
		
		return ( isPrefix );
		
	} /* end of method checkProperPrefix */
	
} /* end of NamePrefix class */
