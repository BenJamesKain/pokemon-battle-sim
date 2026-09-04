import java.util.HashMap;
import java.util.Map;

public class SpellDatabase {
    Map<String, Spell> spells = new HashMap<>();
    
    public void addSpell(Spell spell) {
        spells.put(spell.getName(), spell);
    }
    
    public Spell getSpell(String name) {
        return spells.get(name);
    }
    
    public boolean hasSpell(String name) {
        return spells.containsKey(name);
    }
}
