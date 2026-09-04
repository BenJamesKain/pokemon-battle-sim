import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Tokenizer {
    private String input;
    private int currentPosition;
    
    // Track line numbers
    private int currentLine;
    
    TokenRules rules = new TokenRules();
    ArrayList<TokenRules> list = new ArrayList<>();
    
    public Tokenizer(String input) {
        this.input = input;
        this.currentPosition = 0;
        this.currentLine = 1;
        
        rules.populate(list);
    }
    
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        
        while (currentPosition < input.length()) {
            char currentChar = input.charAt(currentPosition);
            
            // Handle whitespace
            if (Character.isWhitespace(currentChar)) {
//            if (currentChar == ' ' || currentChar == '\t' || currentChar == '\r') {
                
                if (currentChar == '\n') {
                    currentLine++;
                }
                currentPosition++;
                continue;
            }
            
            // Handle single-line comments
            if (currentChar == '/' &&
                    currentPosition + 1 < input.length() &&
                    input.charAt(currentPosition + 1) == '/') {
                
                // Skip until end of line
                currentPosition += 2; // skip the //
                while (currentPosition < input.length() &&
                        input.charAt(currentPosition) != '\n') {
                    currentPosition++;
                }
                continue;
            }
            
            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            } else {
                // error handling for unfound tokens
                throw new RuntimeException("Unknown Character: " + currentChar);
            }
        }
        
        // Add the EOF keyword to the end of the file
        tokens.add(new Token(TokenType.KEYWORD_EOF, "KEYWORD_EOF", "EOF", currentLine));
        return tokens;
    }
    
    private Token nextToken() {
        // Error handling
        if (currentPosition >= input.length()) {
            return null;
        }
        
        
        // Try and find the longest legal match
        
        // If we find a match, continue adding additional tokens to see if they also make a match.
        // Keep doing this until we reach a dead state, and when we do, go back to the last match.
        TokenRules longestRule = null;
        String longestMatch = "";
        
        // Iterate through the list and match
        for (TokenRules rule : list) {
            Matcher matcher = rule.compiledPattern.matcher(input.substring(currentPosition));
            if (matcher.lookingAt()) {
                String match = matcher.group();
                
                if (match.length() > longestMatch.length()) {
                    longestMatch = match;
                    longestRule = rule;
                    
                }
            }
        }
        
        // Error handling
        if (longestRule == null) {
            throw new RuntimeException("Zero Length");
        }
        
        // Count newlines in the matched token
        int newlines = (int) longestMatch.chars().filter(c -> c == '\n').count();
        int tokenLine = currentLine;
        currentLine += newlines;
        
        currentPosition += longestMatch.length();
        
        Object literal = null;
        
        switch (longestRule.type) {
            case INT_LITERAL:
                literal = Integer.parseInt(longestMatch);
                break;
            case STRING_LITERAL:
                literal = longestMatch;
                break;
            case KW_TRUE: case KW_FALSE:
                literal = Boolean.parseBoolean(longestMatch);
                break;
            default:
                literal = "";
        }
        return new Token(longestRule.type, longestMatch, literal, tokenLine);
    }
}
        
//        for (int i = 0; i < tokenPatterns.length; i++) {
//            Pattern pattern = Pattern.compile("^" + tokenPatterns[i]);
//            Matcher matcher = pattern.matcher(input.substring(currentPosition));
//
//            if (matcher.lookingAt()) {
//                String value = matcher.group();
//                currentPosition += value.length();
//                return new Token(tokenTypes[i], value);
//            }
//        }