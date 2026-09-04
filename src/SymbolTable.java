import java.util.Stack;

public class SymbolTable {
    
    public SymbolTable() {
        scopeStack = new Stack<>();
    }
    
    private Stack<Scope> scopeStack = new Stack<>();
    
    public void enterScope() {
        scopeStack.push(new Scope());
    }
    
    public void exitScope() {
        scopeStack.pop();
    }
    
    public boolean define(String name, Symbol symbol) {
        return scopeStack.peek().define(name, symbol);
    }
    
    public Symbol lookup(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Symbol s = (Symbol) scopeStack.get(i).get(name);
            if (s != null) return s;
        }
        return null;
    }
}
