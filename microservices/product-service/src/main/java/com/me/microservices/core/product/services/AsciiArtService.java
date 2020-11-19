package com.me.microservices.core.product.services;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
public class AsciiArtService {
	
	final String newligne = System.getProperty("line.separator");
	final String space = "   ";
	
	final String[] caracters = {"A", "B", "C", "D", "E", "F", "G", "H", "I","J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", " "};
	
	final String[] asciiAlphabet = {" #  ##   ## ##  ### ###  ## # # ###  ## # # #   # # ###  #  ##   #  ##   ## ### # # # # # # # # # # ### ### ",
                                    "# # # # #   # # #   #   #   # #  #    # # # #   ### # # # # # # # # # # #    #  # # # # # # # # # #   #   # ",
                                    "### ##  #   # # ##  ##  # # ###  #    # ##  #   ### # # # # ##  # # ##   #   #  # # # # ###  #   #   #   ## ",
                                    "# # # # #   # # #   #   # # # #  #  # # # # #   # # # # # # #    ## # #   #  #  # # # # ### # #  #  #       ",
			                        "# # ##   ## ##  ### #    ## # # ###  #  # # ### # # # #  #  #     # # # ##   #  ###  #  # # # #  #  ###  #  "};
	
	/**
	 * @param word
	 */
	public void display(String word) {
		
		WordInAsciiArt w = convertToAscii(word);
		
		if(w != null) {
			
			System.out.println(newligne);
			System.out.print(w.getLine1());
			System.out.print(w.getLine2());
			System.out.print(w.getLine3());
			System.out.print(w.getLine4());
			System.out.print(w.getLine5());
			System.out.println(newligne);
		}
	}
	
	public WordInAsciiArt convertToAscii (String word) {
	    
	    String wordNormalize = normalize(word);
	    
	    StringBuilder sA = new StringBuilder();
	    StringBuilder sB = new StringBuilder();
	    StringBuilder sC = new StringBuilder();
	    StringBuilder sD = new StringBuilder();
	    StringBuilder sE = new StringBuilder();
	    
	    for(int i=0 ; i < wordNormalize.length() ; i++) {
	        
	        String current = Character.valueOf(wordNormalize.charAt(i)).toString();
			int position = pos(current);
			
			if(position == 27) {
				
				sA.append(space).append(space);
			    sB.append(space).append(space);
			    sC.append(space).append(space);
			    sD.append(space).append(space);
			    sE.append(space).append(space);
			}
			else {
				
				if(position == -1) position = 27;
				
				//Other characters.
				int x = ((position-1) * 3) + (position - 1);
				int y = x + 3;
				sA.append(asciiAlphabet[0].substring( x, y)).append(space);
				sB.append(asciiAlphabet[1].substring( x, y)).append(space);
				sC.append(asciiAlphabet[2].substring( x, y)).append(space);
				sD.append(asciiAlphabet[3].substring( x, y)).append(space);
				sE.append(asciiAlphabet[4].substring( x, y)).append(space);
			}
	    }
	    
	    sA.append(newligne);
	    sB.append(newligne);
	    sC.append(newligne);
	    sD.append(newligne);
	    sE.append(newligne);
	    
	    return new WordInAsciiArt(sA.toString(), sB.toString(), sC.toString(), sD.toString(), sE.toString());
	}
	
	/**
	 * @param in
	 * @return the position
	 */
	public int pos(String in) {
		
		for(int i = 0 ; i < caracters.length ; i++) {
			
			if(caracters[i].equals(in)) {
				return i+1;
			}
		}
		
		return -1;
	}
	
	/**
	 * @param word
	 * @return world normalized
	 */
	public String normalize (String word) {
	    
	    if(word==null) throw new IllegalArgumentException("Word can't be null.");

	    String patter = "[a-zA-Z]";
	    
	    word = word.toUpperCase();
	    
	    StringBuilder sb =  new StringBuilder();
		
		for(int i = 0 ; i < word.length() ; i++) {
			
			String current = Character.valueOf(word.charAt(i)).toString();
			
			if(current.matches(patter) || current.equals(" "))
				sb.append(current);
			else 
				sb.append("?");
		}
	    
	    return sb.toString();
	}
	
	@Getter @AllArgsConstructor
	public static class WordInAsciiArt {
		
		private final String line1;
		private final String line2;
		private final String line3;
		private final String line4;
		private final String line5;
	}
}
