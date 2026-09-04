public class ASTPrinter implements ASTVisitor<String>{
    
    private Scope scope = new Scope();
    
    @Override
    public String visitProgramNode(ProgramNode expr) {
        
        StringBuilder result = new StringBuilder();
        
        for (StatementNode stmt : expr.statements) {
            result.append(stmt.accept(this));
            result.append("\n");
        }
        return result.toString();
    }
    
    @Override
    public String visitVarDecNode(VarDecNode expr) {
        return "(" + expr.type + " " + expr.name.value + " = " + (expr.expression.accept(this) + ")");
    }
    
    @Override
    public String visitPkmLoadNode(PkmLoadNode expr) {
        return "(Pokemon " + expr.name.value + " = Pokemon(" + expr.path.value + ")";
    }
    
    @Override
    public String visitAssnNode(AssnNode expr) {
        return "(" + expr.target.accept(this) + " " + expr.operator.value + " " + expr.expression.accept(this) + ")";
    }
    
    @Override
    public String visitSpellDbNode(SpellDbNode expr) {
        return "(spelldatabase " + expr.path.value + ")";
    }
    
    @Override
    public String visitShowNode(ShowNode expr) {
        return "(show " + expr.target.value + ")";
    }
    
    @Override
    public String visitPrintNode(PrintNode expr) {
        return "(print " + expr.value.accept(this) + ")";
    }
    
    @Override
    public String visitMoveNode(MoveNode expr) {
        return "(" + expr.user.value + " " + expr.move.value + " " + expr.target.value + ")";
    }
    
    @Override
    public String visitIfNode(IfNode expr) {
        StringBuilder result = new StringBuilder("(if ");
        
        // Condition
        result.append(expr.condition.accept(this));
        
        // If block(s)
        result.append(" (block");
        
        for (StatementNode stmt : expr.block) {
            result.append(" ");
            result.append(stmt.accept(this));
        }
        result.append(")");
        
        // Optional elseif(s)
        for (int i = 0; i < expr.elseIfBlocks.size(); i++) {
            result.append("(elseif ");
            
            // Condition
            result.append(expr.elseIfConditions.get(i).accept(this));
            
            // Block
            result.append(" (block");
            
            for (StatementNode stmt : expr.elseIfBlocks.get(i)) {
                result.append(" ");
                result.append(stmt.accept(this));
            }
            result.append("))");
        }
        
        // Optional else block
        if (expr.elseBlock != null && !expr.elseBlock.isEmpty()) {
            result.append(" (else block");
            
            for (StatementNode stmt : expr.elseBlock) {
                result.append(" ");
                result.append(stmt.accept(this));
            }
            result.append("))");
        }
        result.append(")");
        
        return result.toString();
    }
    
    @Override
    public String visitWhileNode(WhileNode expr) {
        
        StringBuilder result = new StringBuilder();
        
        result.append("(while ");
        result.append(expr.condition.accept(this));
        
        result.append(" (block");
        for (StatementNode stmt : expr.block) {
            result.append(" ");
            result.append(stmt.accept(this));
        }
        result.append("))");
        return result.toString();
    }
    
    @Override
    public String visitForNode(ForNode expr) {
        StringBuilder result = new StringBuilder();
        
        result.append("(for ");
        result.append(expr.init.accept(this));
        result.append(" ");
        result.append(expr.condition.accept(this));
        result.append(" ");
        result.append(expr.update.accept(this));
        
        result.append(" (block");
        for (StatementNode stmt : expr.block) {
            result.append(" ");
            result.append(stmt.accept(this));
        }
        
        result.append("))");
        return result.toString();
    }
    
    @Override
    public String visitFuncDecNode(FuncDecNode expr) {
        StringBuilder result = new StringBuilder();
        
        result.append("(function ");
        result.append(expr.returnType);
        result.append(" ");
        result.append(expr.id.value);
        result.append("(params");
        
        for (FuncDecNode.ParameterNode param : expr.parameters) {
            result.append(" ");
            result.append(param.type);
            result.append(" ");
            result.append(param.id.value);
        }
        
        result.append("))");
        return result.toString();
    }
    
    @Override
    public String visitFuncCallNode(FuncCallNode expr) {
        StringBuilder result = new StringBuilder();
        
        result.append("(");
        result.append(expr.name.value);
        result.append("(");
        if (expr.args != null && !expr.args.isEmpty()) {
            
            result.append("(args");
            for (ExpressionNode expression : expr.args) {
                result.append(" ");
                result.append(expression.accept(this));
            }
            result.append("))");
        }
        result.append(")");
        return result.toString();
    }
    
    @Override
    public String visitBreakNode(BreakNode expr) {
        return "(break)";
    }
    
    @Override
    public String visitContinueNode(ContinueNode expr) {
        return "(continue)";
    }
    
    @Override
    public String visitReturnNode(ReturnNode expr) {
        return "(return " + expr.value.accept(this) + ")";
    }
    
    @Override
    public String visitParameterNode(FuncDecNode.ParameterNode expr) {
        return "(" + expr.type + " " +  expr.id.value + ")";
    }
    
    @Override
    public String visitLogicalExprNode(LogicalExprNode expr) {
        return "(" + expr.left.accept(this) + " " + expr.operator.value + " " + expr.right.accept(this) + ")";
    }
    
    @Override
    public String visitVariableNode(VariableNode expr) {
        return expr.name.value;
    }
    
    @Override
    public String visitFieldAccessNode(FieldAccessNode expr) {
        return "(" + expr.object.accept(this) + "." + expr.field.value + ")";
    }
    
    @Override
    public String visitUnaryNode(UnaryNode expr) {
        return "(" + expr.operator.value + " " + expr.right.accept(this) + ")";
    }
    
    @Override
    public String visitLiteralNode(LiteralNode expr) {
        if (expr.value == null) return "null";
        return expr.value.toString();
    }
    
    @Override
    public String visitGrouping(Grouping expr) {
        return expr.expression.accept(this);
    }
    
    @Override
    public String visitBinaryExprNode(BinaryExprNode expr) {
        return "(" + expr.left.accept(this) + " " + expr.operator.value + " " + expr.right.accept(this) + ")";
    }
    
    @Override
    public String visitIncrementNode(IncrementNode expr) {
        return "(" + expr.target.accept(this) + expr.operator.value + ")";
    }
    
    @Override
    public String visitExpressionStmtNode(ExpressionStmtNode expressionStmtNode) {
        return null;
    }
}
