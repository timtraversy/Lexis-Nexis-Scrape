import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ParseHtml {

    public static void main (String[] args) {

        // Get the list of all HTML files, and declare lists of HTML strings and states
        String folderReadName = "HTML48-99";
        File[] files = new File(folderReadName).listFiles();
        ArrayList<String> htmls = new ArrayList<>();
        ArrayList<String> states = new ArrayList<>();

        // Store each file as a string, and save the state information
        try {
            int i = 0;
            for (File file : files) {
                if (file.getName().equals(".DS_Store")) {
                    continue;
                }
                htmls.add(new Scanner(new File(file.getPath())).useDelimiter("\\Z").next());
                states.add(file.getName().substring(0,2));
                ++i;
            }
        } catch (FileNotFoundException x) {
            System.err.println("Caught FileNotFoundException: " + x.getMessage());
        }

        // This try loop wraps all the String Builder calls
        try {

            // Create a file writer and add the column headers
            String folderWriteName = "Share/AllData48-99.csv";
            PrintWriter pw = new PrintWriter(new File(folderWriteName));
            StringBuilder sb = new StringBuilder();
            sb.append("State");
            sb.append(',');
            sb.append("Case Name");
            sb.append(',');
            sb.append("Court Name");
            sb.append(',');
            sb.append("Lexis Citation");
            sb.append(',');
            sb.append("Date");
            sb.append(',');
            sb.append("Subsequent History");
            sb.append(',');
            sb.append("Prior History");
            sb.append(',');
            sb.append("Procedural Posture");
            sb.append(',');
            sb.append("Overview");
            sb.append(',');
            sb.append("Outcome");
            sb.append('\n');

            // Prepare to loop. Declare a counter and an array to hold the lexis IDs (to find duplicates)
            int recordCount = 0;
            ArrayList<String> lexises = new ArrayList<>();

            // Loop over each HTML page
            for (int i = 0; i< htmls.size(); ++i) {

                // Grab the HTML Page
                String html = htmls.get(i);

                // choose HTML tag based on stylesheet of that html page
                String c11c8;
                int switchIndex = html.indexOf("font-family: 'Times New Roman'; font-size: 10pt; font-style: normal; font-weight: normal; background-color: #DCDCDC; color: #000000; text-decoration: none") - 3;
                int startSwitchIndex = html.lastIndexOf(".", switchIndex) + 1;
                if (switchIndex < 0) {
                    c11c8 = "";
                } else {
                    c11c8 = html.substring(startSwitchIndex, switchIndex);
                }

                // choose HTML tag based on stylesheet of that html page
                String c4c5;
                switchIndex = html.indexOf(".c5") + 6;
                int switchIndexTwo = html.indexOf(".c4") + 6;
                if (html.charAt(switchIndex) == html.charAt(switchIndexTwo)) {
                    c4c5 = "c4";
                } else if (html.charAt(switchIndex) != 'f') {
                    c4c5 = "c4";
                } else {
                    c4c5 = "c5";
                }

                // Reset indices for that page. Start and end wrap each string to write, next one points to the next record
                int startIndex = 0;
                int endIndex = 0;
                int nextOne = 0;

                while (nextOne != html.length()) {

                    // Declare utility to handle sb writes
                    Utilities utility = new Utilities();

                    // Print current record #, starting at 1
                    ++recordCount;
                    System.out.println("Record #: " + recordCount);

                    // Get index of current record and the next one. If there is no next one, set the next one to be end of doc
                    startIndex = html.indexOf("<DIV CLASS=\"c0\"><P CLASS=\"c1\"><SPAN CLASS=\"c2\">", startIndex) + 2;
                    nextOne = html.indexOf("<DIV CLASS=\"c0\"><P CLASS=\"c1\"><SPAN CLASS=\"c2\">", startIndex);
                    if (nextOne == -1) {
                        nextOne = html.length();
                    }

                    // Now find each column of data

                    //state
                    sb.append(states.get(i));
                    System.out.println("State: " + states.get(i));
                    sb.append(',');

                    //case name
                    startIndex = html.indexOf("c2", startIndex);
                    startIndex = html.indexOf("SPAN CLASS", startIndex);
                    startIndex += 16;
                    int endSectionIndex = html.indexOf("</DIV>", startIndex);
                    endIndex = html.indexOf("</", startIndex);
                    String caseName = html.substring(startIndex, endIndex);
                    startIndex = html.indexOf(c4c5, endIndex) + 4;
                    while (startIndex < endSectionIndex && startIndex > 0) {
                        endIndex = html.indexOf("<", startIndex);
                        String moreName = html.substring(startIndex, endIndex);
                        caseName = caseName.concat(moreName);
                        startIndex = html.indexOf(c4c5, endIndex) + 4;
                    }
                    sb.append(utility.formatString(caseName));
                    sb.append(',');
                    startIndex = endIndex;
                    System.out.println("Case Name: " + caseName);

                    //court name
                    startIndex = html.indexOf(c4c5, startIndex) + 2;
                    startIndex = html.indexOf(c4c5, startIndex) + 4;
                    endIndex = html.indexOf("</", startIndex);
                    String courtName = html.substring(startIndex, endIndex);
                    sb.append(utility.formatString(courtName));
                    sb.append(',');
                    System.out.println("Court Name: " + courtName);

                    //lexis
                    int lexisIndex = html.indexOf("LEXIS", startIndex);
                    startIndex = html.lastIndexOf(">", lexisIndex) + 1;
                    if (html.lastIndexOf(";", lexisIndex) > startIndex) {
                        startIndex = html.lastIndexOf(";", lexisIndex)+1;
                    }
                    endIndex = html.indexOf("<", lexisIndex);
                    int semiIndex = html.indexOf(";", lexisIndex);
                    if (semiIndex < endIndex && semiIndex > 0) {
                        endIndex = semiIndex;
                    }
                    String lexis = html.substring(startIndex, endIndex);
                    if (lexises.contains(lexis)) {
                        System.out.println("DUPLICATE");
                        int last = sb.lastIndexOf("\n");
                        if (last >= 0) { sb.delete(last, sb.length()); }
                    } else {
                        sb.append(utility.formatString(lexis));
                        lexises.add(lexis);
                    }
                    sb.append(',');
                    System.out.println("Lexis: " + lexis);

                    //date. use decided if there, otherwise most recent
                    endIndex = html.indexOf("</P>", endIndex) + 4;
                    int endOfDates = html.indexOf("</P>", endIndex);
                    int decidedIndex = html.indexOf("Decided", endIndex);
                    if (decidedIndex < endOfDates && decidedIndex > 0) {
                        endIndex = html.indexOf("</SPAN>", decidedIndex);
                        startIndex = html.lastIndexOf(">", endIndex) + 1;
                    } else {
                        endIndex = html.lastIndexOf("</SPAN>", endOfDates);
                        startIndex = html.lastIndexOf(">", endIndex) + 1;
                    }
                    String date = html.substring(startIndex, endIndex);
                    sb.append(utility.formatString(date));
                    System.out.println("Date: " + date);
                    sb.append(',');

                    //subsequent history
                    startIndex = html.indexOf("SUBSEQUENT HISTORY", endIndex);
                    if (startIndex < 0 || startIndex > nextOne) {
                        startIndex = endIndex;
                        sb.append(" ");
                        sb.append(',');
                    } else {
                        endIndex = html.indexOf("</SPAN></P>", startIndex);
                        startIndex = html.lastIndexOf(">", endIndex) - 1;
                        while (html.charAt(html.lastIndexOf(">", startIndex) - 1) == 'R') {
                            startIndex = html.lastIndexOf(">", startIndex) - 1;
                        }
                        startIndex += 2;
                        String subsequent = html.substring(startIndex, endIndex).replaceAll("<BR>", " ").replaceAll("&nbsp;", "");
                        sb.append(utility.formatString(subsequent));
                        System.out.println("Subsequent History: " + subsequent);
                        sb.append(',');
                    }

                    //prior history
                    startIndex = html.indexOf("PRIOR HISTORY", startIndex);
                    if (startIndex < 0 || startIndex > nextOne) {
                        startIndex = endIndex;
                        sb.append(" ");
                        sb.append(',');
                    } else {
                        endIndex = html.indexOf("</SPAN></P>", startIndex);
                        startIndex = html.lastIndexOf(">", endIndex) - 1;
                        while (html.charAt(html.lastIndexOf(">", startIndex) - 1) == 'R') {
                            startIndex = html.lastIndexOf(">", startIndex) - 1;
                        }
                        startIndex += 2;
                        String prior = html.substring(startIndex, endIndex).replaceAll("<BR>", " ").replaceAll("&nbsp;", "");
                        sb.append(utility.formatString(prior));
                        System.out.println("Prior History: " + prior);
                        sb.append(',');
                    }

                    // procedural posture
                    startIndex = html.indexOf("PROCEDURAL POSTURE:", startIndex);
                    if (startIndex < 0 || startIndex > nextOne) {
                        startIndex = endIndex;
                        sb.append(" ");
                        sb.append(',');
                    } else {
                        startIndex = html.indexOf(c11c8, startIndex) + c11c8.length() + 2;
                        endIndex = html.indexOf("</", startIndex);
                        String proceduralPosture = html.substring(startIndex, endIndex);
                        sb.append(utility.formatString(proceduralPosture));
                        sb.append(',');
                        System.out.println("Procedural Posture: " + proceduralPosture);
                    }

                    //overview
                    startIndex = html.indexOf("OVERVIEW:", startIndex);
                    if (startIndex < 0 || startIndex > nextOne) {
                        startIndex = endIndex;
                        sb.append(" ");
                        sb.append(',');
                    } else {
                        startIndex = html.indexOf(c11c8, startIndex) + c11c8.length() + 2;
                        endIndex = html.indexOf("</", startIndex);
                        String overview = html.substring(startIndex, endIndex);
                        sb.append(utility.formatString(overview));
                        sb.append(',');
                        System.out.println("Overview: " + overview);
                    }

                    //outcome
                    startIndex = html.indexOf("OUTCOME:", startIndex);
                    if (startIndex < 0 || startIndex > nextOne) {
                        startIndex = endIndex;
                        sb.append(" ");
                    } else {
                        startIndex = html.indexOf(c11c8, startIndex) + c11c8.length() + 2;
                        endIndex = html.indexOf("</", startIndex);
                        String outcome = html.substring(startIndex, endIndex);
                        sb.append(utility.formatString(outcome));
                        System.out.println("Outcome: " + outcome);
                    }

                    // start new data row
                    sb.append('\n');
                    System.out.println("---------------");

                }

                // write data from this html page and empty sb to free heap memory
                pw.write(sb.toString());
                sb.setLength(0);

            }

            pw.close();

        } catch(FileNotFoundException x){
            System.err.println("Caught FileNotFoundException: " + x.getMessage());
        }
    }
}
