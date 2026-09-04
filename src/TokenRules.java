import java.util.ArrayList;
import java.util.regex.Pattern;

public class TokenRules {

    public TokenType type;
    public String pattern;
    public Pattern compiledPattern;
    
    public TokenRules() {
    }
    
    public TokenRules(TokenType type, String pattern) {
        this.type = type;
        this.pattern = pattern;
        
        // Put this here so the patterns only need to compile once
        this.compiledPattern = Pattern.compile("^" + pattern);
    }
    
    // Populate a given Array with the following token rules
    public void populate(ArrayList<TokenRules> list) {
        // ===== KEYWORDS =====
        list.add(new TokenRules(TokenType.KW_POKEMON, "Pokemon"));
        list.add(new TokenRules(TokenType.KW_INT, "int"));
        list.add(new TokenRules(TokenType.KW_STRING, "String"));
        list.add(new TokenRules(TokenType.KW_BOOLEAN, "boolean"));
        list.add(new TokenRules(TokenType.KW_FUNCTION, "function"));
        list.add(new TokenRules(TokenType.KW_VOID, "void"));
        list.add(new TokenRules(TokenType.KW_IF, "if"));
        list.add(new TokenRules(TokenType.KW_ELSEIF, "elseif"));
        list.add(new TokenRules(TokenType.KW_ELSE, "else"));
        list.add(new TokenRules(TokenType.KW_WHILE, "while"));
        list.add(new TokenRules(TokenType.KW_FOR, "for"));
        list.add(new TokenRules(TokenType.KW_BREAK, "break"));
        list.add(new TokenRules(TokenType.KW_CONTINUE, "continue"));
        list.add(new TokenRules(TokenType.KW_RETURN, "return"));
        list.add(new TokenRules(TokenType.KW_PRINT, "print"));
        list.add(new TokenRules(TokenType.KW_SHOW, "show"));
        list.add(new TokenRules(TokenType.KW_SPELLDATABASE, "spelldatabase"));
        list.add(new TokenRules(TokenType.KW_MOVE1, "move1"));
        list.add(new TokenRules(TokenType.KW_MOVE2, "move2"));
        list.add(new TokenRules(TokenType.KW_MOVE3, "move3"));
        list.add(new TokenRules(TokenType.KW_MOVE4, "move4"));
//        list.add(new TokenRules(TokenType.KW_HP, "hp"));
//        list.add(new TokenRules(TokenType.KW_ATTACK, "attack"));
//        list.add(new TokenRules(TokenType.KW_DEFENSE, "defense"));
//        list.add(new TokenRules(TokenType.KW_NAME, "name"));
//        list.add(new TokenRules(TokenType.KW_TYPE, "type"));
        
        list.add(new TokenRules(TokenType.KW_TRUE, "true"));
        list.add(new TokenRules(TokenType.KW_FALSE, "false"));


        // ===== LITERALS =====
        list.add(new TokenRules(TokenType.INT_LITERAL, "0|[1-9][0-9]*"));
        list.add(new TokenRules(TokenType.STRING_LITERAL, "\"[^\"]*\""));


        // ===== IDENTIFIERS =====
        list.add(new TokenRules(TokenType.IDENTIFIER, "[a-zA-Z][a-zA-Z0-9]*"));


        // ===== OPERATORS =====
        list.add(new TokenRules(TokenType.OP_GREATER_OR_EQUAL, ">="));
        list.add(new TokenRules(TokenType.OP_LESS_OR_EQUAL, "<="));
        list.add(new TokenRules(TokenType.OP_PLUS_ASSIGN, "\\+="));
        list.add(new TokenRules(TokenType.OP_MINUS_ASSIGN, "-="));
        list.add(new TokenRules(TokenType.OP_NOTEQUAL, "!="));
        list.add(new TokenRules(TokenType.OP_EQUALS, "=="));
        list.add(new TokenRules(TokenType.OP_AND, "&&"));
        list.add(new TokenRules(TokenType.OP_OR, "\\|\\|"));
        
        list.add(new TokenRules(TokenType.OP_INCREMENT, "\\+\\+"));
        
        list.add(new TokenRules(TokenType.OP_GREATER, ">"));
        list.add(new TokenRules(TokenType.OP_LESS, "<"));
        list.add(new TokenRules(TokenType.OP_PLUS, "\\+"));
        list.add(new TokenRules(TokenType.OP_MINUS, "-"));
        list.add(new TokenRules(TokenType.OP_ASSIGN, "="));
        list.add(new TokenRules(TokenType.OP_NOT, "!"));
        list.add(new TokenRules(TokenType.OP_MULTIPLY, "\\*"));
        list.add(new TokenRules(TokenType.OP_DIVIDE, "/"));


        // ===== DELIMITERS =====
        list.add(new TokenRules(TokenType.LPAREN, "\\("));
        list.add(new TokenRules(TokenType.RPAREN, "\\)"));
        list.add(new TokenRules(TokenType.LBRACE, "\\{"));
        list.add(new TokenRules(TokenType.RBRACE, "\\}"));
        list.add(new TokenRules(TokenType.LBRACKET, "\\["));
        list.add(new TokenRules(TokenType.RBRACKET, "\\]"));
        list.add(new TokenRules(TokenType.SEMICOLON, ";"));
        list.add(new TokenRules(TokenType.COMMA, ","));
        list.add(new TokenRules(TokenType.DOT, "\\."));


        // ===== SPECIAL =====
        list.add(new TokenRules(TokenType.KEYWORD_EOF, "KEYWORD_EOF"));
    }
}
