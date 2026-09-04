interface ASTVisitor<T> {
    T visitProgramNode(ProgramNode program);
    
    T visitVarDecNode(VarDecNode varDecNode);
    
    T visitPkmLoadNode(PkmLoadNode pkmLoadNode);
    
    T visitAssnNode(AssnNode assnNode);
    
    T visitSpellDbNode(SpellDbNode spellDbNode);
    
    T visitShowNode(ShowNode showNode);
    
    T visitPrintNode(PrintNode printNode);
    
    T visitMoveNode(MoveNode moveNode);
    
    T visitIfNode(IfNode ifNode);
    
    T visitWhileNode(WhileNode whileNode);
    
    T visitForNode(ForNode forNode);
    
    T visitFuncDecNode(FuncDecNode funcDecNode);
    
    T visitFuncCallNode(FuncCallNode funcCallNode);
    
    T visitBreakNode(BreakNode breakNode);
    
    T visitContinueNode(ContinueNode continueNode);
    
    T visitReturnNode(ReturnNode returnNode);
    
    T visitParameterNode(FuncDecNode.ParameterNode parameterNode);
    
    T visitLogicalExprNode(LogicalExprNode logicalExprNode);
    
    T visitVariableNode(VariableNode variableNode);
    
    T visitFieldAccessNode(FieldAccessNode fieldAccessNode);
    
    T visitUnaryNode(UnaryNode unaryNode);
    
    T visitLiteralNode(LiteralNode literalNode);
    
    T visitGrouping(Grouping grouping);
    
    T visitBinaryExprNode(BinaryExprNode binaryExprNode);
    
    T visitIncrementNode(IncrementNode incrementNode);
    
    T visitExpressionStmtNode(ExpressionStmtNode expressionStmtNode);
}