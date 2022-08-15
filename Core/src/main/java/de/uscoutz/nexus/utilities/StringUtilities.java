package de.uscoutz.nexus.utilities;

public class StringUtilities {

    public static int countChar(String string, char character) {
        int count = 0;
        for(int i = 0; i < string.length(); i++) {
            if(string.charAt(i) == character) {
                count++;
            }
        }

        return count;
    }
}
