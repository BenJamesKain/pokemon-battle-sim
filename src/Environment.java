import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment enclosing;
    
    // Optional returnValue for Function Calls
    private Object returnValue = null;
    
    public Environment() {
        this.enclosing = null;
    }
    
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }
    
    public Object get(Token name) {
        if (values.containsKey(name.value)) {
            return values.get(name.value);
        }
        
        if (enclosing != null) return enclosing.get(name);
        
        throw new RuntimeError(name, "Undefined variable '" + name.value + "'.");
    }
    
    void assign(Token name, Object value) {
        if (values.containsKey(name.value)) {
            values.put(name.value, value);
            return;
        }
        
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        
        throw new RuntimeError(name, "Undefined variable '" + name.value + "'.");
    }
    
    public void setReturnValue(Object value) {
        this.returnValue = value;
    }
    
    public Object getReturnValue() {
        return returnValue;
    }
    
    public void clearReturnValue() {
        returnValue = null;
    }
    
    
}
