import java.util.List;

public class Token {
    final Object literal;
    final TokenType type;
    final String value;
    final int line;
    
    public Token(TokenType type, String value, Object literal, int line) {
        this.type = type;
        this.value = value;
        this.literal = literal;
        this.line = line;
    }
}