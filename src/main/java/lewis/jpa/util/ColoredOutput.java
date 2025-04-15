package lewis.jpa.util;

/**
 * Utility class for colorful console output
 */
public class ColoredOutput {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Background colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // Styles
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    /**
     * Create a colorful demo header with the given text
     * 
     * @param demoNumber The demo number
     * @param title The demo title
     * @return A formatted and colored header string
     */
    public static String demoHeader(int demoNumber, String title) {
        String header = "DEMO " + demoNumber + ": " + title;
        String padding = "=".repeat(5);
        
        StringBuilder sb = new StringBuilder("\n\n");
        sb.append(BOLD).append(BG_BLUE).append(WHITE);
        sb.append(padding).append(" ").append(header).append(" ").append(padding);
        sb.append(RESET).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Create a colorful demo completion footer
     * 
     * @param demoNumber The demo number
     * @param title The demo title
     * @return A formatted and colored footer string
     */
    public static String demoFooter(int demoNumber, String title) {
        String footer = "DEMO " + demoNumber + ": " + title + " COMPLETE";
        String padding = "=".repeat(3);
        
        StringBuilder sb = new StringBuilder("\n");
        sb.append(BOLD).append(GREEN);
        sb.append(padding).append(" ").append(footer).append(" ").append(padding);
        sb.append(RESET).append("\n\n");
        
        return sb.toString();
    }
} 