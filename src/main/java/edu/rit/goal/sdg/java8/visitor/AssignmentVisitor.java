package edu.rit.goal.sdg.java8.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.rit.goal.sdg.java8.antlr.Java8BaseVisitor;
import edu.rit.goal.sdg.java8.antlr.Java8Parser;
import edu.rit.goal.sdg.java8.antlr.Java8Parser.ArgumentListContext;
import edu.rit.goal.sdg.java8.antlr.Java8Parser.ArrayAccessContext;
import edu.rit.goal.sdg.java8.antlr.Java8Parser.LeftHandSideContext;
import edu.rit.goal.sdg.java8.antlr.Java8Parser.MethodInvocation_lfno_primaryContext;
import edu.rit.goal.sdg.java8.antlr.Java8Parser.TypeNameContext;
import edu.rit.goal.sdg.statement.ArrayAccessAssignment;
import edu.rit.goal.sdg.statement.Assignment;
import edu.rit.goal.sdg.statement.Expr;
import edu.rit.goal.sdg.statement.MethodInvocationAssignment;
import edu.rit.goal.sdg.statement.Stmt;

public class AssignmentVisitor extends Java8BaseVisitor<Stmt> {

    @Override
    public Stmt visitAssignment(final Java8Parser.AssignmentContext ctx) {
	Stmt result = null;
	final LeftHandSideContext lhsCtx = ctx.leftHandSide();
	final String outVar = lhsCtx.getText();
	final String operator = ctx.assignmentOperator().getText();
	final ExprVisitor visitor = new ExprVisitor();
	final Expr rightHandSide = visitor.visit(ctx.expression());
	// Array access assignment
	if (isArrayAccess(ctx)) {
	    final ArrayAccessContext arrayAccessCtx = lhsCtx.arrayAccess();
	    final String expressionName = arrayAccessCtx.expressionName().getText();
	    final Expr index = visitor.visit(arrayAccessCtx.expression(0));
	    result = new ArrayAccessAssignment(expressionName, index, operator, rightHandSide);
	} else {
	    result = new Assignment(outVar, operator, rightHandSide);
	}
	// Add dependency w.r.t. variable being assigned if it is a short-hand operator
	if (VisitorUtils.isShortHandOperator(operator)) {
	    rightHandSide.getReadingVars().add(outVar);
	}
	// Method call
	final String methodName = VisitorUtils.getMethodName(ctx);
	final boolean isMethodInvocation = methodName != null;
	if (isMethodInvocation) {
	    final List<Expr> rhsList = new ArrayList<>();
	    rhsList.add(rightHandSide);
	    final ArgumentListContext argListCtx = VisitorUtils.getArgListCtx(ctx.expression());
	    final List<Expr> inVars = argListCtx.expression().stream().map(exp -> visitor.visit(exp))
		    .collect(Collectors.toList());
	    final MethodInvocation_lfno_primaryContext methodInvCtx = VisitorUtils.getMethodInvCtx(ctx);
	    final TypeNameContext typeNameCtx = methodInvCtx.typeName();
	    String refVar = null;
	    // Calling method on a referenced object
	    if (typeNameCtx != null)
		refVar = typeNameCtx.getText();
	    result = new MethodInvocationAssignment(refVar, methodName, outVar, inVars);
	}
	return result;
    }

    private boolean isArrayAccess(final Java8Parser.AssignmentContext ctx) {
	ArrayAccessContext stmnt = null;
	final LeftHandSideContext lhsCtx = ctx.leftHandSide();
	if (lhsCtx != null)
	    stmnt = lhsCtx.arrayAccess();
	return stmnt != null;
    }

}