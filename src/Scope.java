import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Map<String, Symbol> values = new HashMap<>();
    
    public boolean define(String key, Symbol value) {
        if (values.containsKey(key)) {
            // Key already defined
            return false;
        }
        
        values.put(key, value);
        return true;
    }

    public Object get(String name) {
//        if (values.containsKey(name)) {
            return values.get(name);
//        }
        
//        throw new Error("Undefined variable: '" + name + "'.");
    }
}


