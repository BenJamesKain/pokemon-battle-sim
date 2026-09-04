public class Spell {
    private String name;
    private String type;
    private int value;
    
    public Spell(String name, String type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "Spell Name: " + this.name + ", Type: " + this.type + ", Value: " + this.value;
    }
}
