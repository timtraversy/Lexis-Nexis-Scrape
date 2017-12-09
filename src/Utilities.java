public class Utilities {
    public String formatString(String string) {
        String returnString = string.replaceAll("&amp;", "&").replaceAll("&quot;","\"");
        returnString = returnString.trim().replaceAll("\"","\"\"");
        returnString = ("\"" + returnString + "\"");
        return returnString;
    }
}
