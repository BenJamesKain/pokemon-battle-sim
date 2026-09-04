import java.util.HashMap;
import java.util.Map;

public class Pokemon {
    private int hp, attack, defense;
    private String name, type;
    private Map<String, String> spellNames = new HashMap<>();
    private final Map<String, Spell> spells = new HashMap<>();
    
    public Pokemon(String name, String type, int hp, int attack, int defense, Map<String, String> spellNames) {
        this.name = name;
        this.type = type;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spellNames = spellNames;
//        this.spells = spells;
    }
    
    public Object getField(String field) {
        switch (field) {
            case "name": return name;
            case "type": return type;
            case "hp": return hp;
            case "attack": return attack;
            case "defense": return defense;
        }
        throw new RuntimeException("Unknown field: " + field);
    }
    
    public void setField(String field, Object value) {
        switch (field) {
            case "name": name = (String) value; break;
            case "type": type = (String) value; break;
            case "hp": hp = (Integer) value; break;
            case "attack": attack = (Integer) value; break;
            case "defense": defense = (Integer) value; break;
            default:
                throw new RuntimeException("Unknown field: " + field);
        }
    }
    
    public Spell getSpell(String slot, SpellDatabase db) {
        if (!spells.containsKey(slot)) {
            String name = spellNames.get(slot);
            
            if (!db.hasSpell(name)) {
                throw new RuntimeException("Move " + name + " not found!");
            }
            
            spells.put(slot, db.getSpell(name));
        }
        return spells.get(slot);
    }
    
    
    
    public void setMove(String field, Spell spell) {
        spells.put(field, spell);
    }
    
    @Override
    public String toString() {
        return "Pokemon: " + name + " Type: " + type + " HP: " + hp + " ATK: " + attack + " DEF: " + defense + " Moves: " + spells.toString();
    }
}
