import java.util.List;

public class FunctionSymbol extends Symbol {
    List<Type> paramTypes;
    List<String> paramNames;
    List<StatementNode> body;
    Type returnType;
    
    FunctionSymbol(String name, List<Type> paramTypes, List<String> paramNames, List<StatementNode> body, Type returnType) {
        super(name, Type.FUNCTION); // Placeholder type
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
        this.body = body;
        this.returnType = returnType;
    }
}
