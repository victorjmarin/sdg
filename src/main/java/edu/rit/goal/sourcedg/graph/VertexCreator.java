package edu.rit.goal.sourcedg.graph;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.rit.goal.sourcedg.builder.PDGBuilderConfig;
import edu.rit.goal.sourcedg.normalization.Normalizer;
import edu.rit.goal.sourcedg.util.Utils;

public class VertexCreator {

  private int id = 0;
  private PDGBuilderConfig cfg;

  public VertexCreator(PDGBuilderConfig cfg) {
    this.cfg = cfg;
  }

  public Vertex exit() {
    Vertex result = new Vertex(VertexType.EXIT, "exit");
    setId(result);
    return result;
  }

  public Vertex classOrInterfaceDeclaration(final ClassOrInterfaceDeclaration n) {
    final String label = n.getNameAsString();
    final Vertex result = new Vertex(VertexType.CLASS, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex constructorDeclaration(final ConstructorDeclaration n) {
    final String label = n.getNameAsString();
    final Vertex result = new Vertex(VertexType.INIT, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex explicitConstructorInvocationStmt(final ExplicitConstructorInvocationStmt n) {
    final String label = n.toString();
    final Vertex result = new Vertex(VertexType.CALL, label, n);
    setId(result);
    setOriginalLine(result, n);
    setSubtypes(result, n);
    return result;
  }

  public Vertex methodDeclaration(final MethodDeclaration n) {
    final String label = n.getNameAsString();
    final Vertex result = new Vertex(VertexType.ENTRY, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex formalOut() {
    final Vertex result = new Vertex(VertexType.FORMAL_OUT, "");
    setId(result);
    return result;
  }

  public Vertex actualOut(final Node n) {
    final Vertex result = new Vertex(VertexType.ACTUAL_OUT, n.toString());
    setId(result);
    setOriginalLine(result, n);
    setDef(n, result);
    return result;
  }

  public Vertex parameter(final Parameter n) {
    final String label = n.getNameAsString();
    final Vertex result = new Vertex(VertexType.FORMAL_IN, label, n);
    setId(result);
    setOriginalLine(result, n);
    setDef(n, result);
    return result;
  }

  public Vertex ifStmt(final IfStmt n) {
    final Expression cond = n.getCondition();
    final String label = cond.toString();
    final Vertex result = new Vertex(VertexType.CTRL, label, cond);
    setId(result);
    setRefs(cond, result);
    setSubtypes(result, cond);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex forStmt(final ForStmt n) {
    final Optional<Expression> cond = n.getCompare();
    String label = "";
    if (cond.isPresent())
      label = cond.get().toString();
    final Vertex result = new Vertex(VertexType.CTRL, label, cond.get());
    setId(result);
    setOriginalLine(result, n);
    if (cond.isPresent()) {
      setRefs(cond.get(), result);
      setSubtypes(result, cond.get());
    }
    return result;
  }

  public Vertex foreachStmt(final ForeachStmt n) {
    final Expression it = n.getIterable();
    final String label = it.toString();
    final Vertex result = new Vertex(VertexType.CTRL, label, it);
    setId(result);
    setOriginalLine(result, n);
    setRefs(it, result);
    setSubtypes(result, it);
    return result;
  }

  public Vertex whileStmt(final WhileStmt n) {
    final Expression cond = n.getCondition();
    final String label = cond.toString();
    final Vertex result = new Vertex(VertexType.CTRL, label, cond);
    setId(result);
    setOriginalLine(result, n);
    setRefs(cond, result);
    setSubtypes(result, cond);
    return result;
  }

  public Vertex doStmt(final DoStmt n) {
    final Expression cond = n.getCondition();
    final String label = cond.toString();
    final Vertex result = new Vertex(VertexType.CTRL, label, cond);
    setId(result);
    setOriginalLine(result, n);
    setRefs(cond, result);
    setSubtypes(result, cond);
    return result;
  }

  public Vertex variableDeclarator(final VariableDeclarator n) {
    final String label = n.toString();
    final Optional<Expression> init = n.getInitializer();
    final Vertex result = new Vertex(VertexType.ASSIGN, label, n);
    setId(result);
    setOriginalLine(result, n);
    setDef(n.getName(), result);
    setSubtypes(result, n);
    if (init.isPresent())
      setRefs(init.get(), result);
    return result;
  }

  public Vertex assignExpr(final AssignExpr n) {
    final String label = n.toString();
    final Vertex result = new Vertex(VertexType.ASSIGN, label, n);
    setId(result);
    setOriginalLine(result, n);
    setDef(n.getTarget(), result);
    setRefs(n.getValue(), result);
    setSubtypes(result, n);
    return result;
  }

  public Vertex methodCallExpr(final MethodCallExpr n) {
    final String label = n.toString();
    final Vertex result = new Vertex(VertexType.CALL, label, n);
    setId(result);
    setOriginalLine(result, n);
    setSubtypes(result, n);
    return result;
  }

  public Vertex argumentExpr(final Expression n) {
    final String label = n.toString();
    final Vertex result = new Vertex(VertexType.ACTUAL_IN, label, n);
    setId(result);
    setOriginalLine(result, n);
    setRefs(n, result);
    return result;
  }

  public Vertex unaryExpr(final UnaryExpr n) {
    final String label = n.toString();
    final Vertex result = new Vertex(VertexType.ASSIGN, label, n);
    setId(result);
    setOriginalLine(result, n);
    setDef(n, result);
    setRefs(n, result);
    setSubtypes(result, n);
    return result;
  }

  public Vertex returnStmt(final ReturnStmt n) {
    final Optional<Expression> expr = n.getExpression();
    final String label = expr.isPresent() ? expr.get().toString() : "";
    final Vertex result = new Vertex(VertexType.RETURN, label, n);
    setId(result);
    setOriginalLine(result, n);
    if (expr.isPresent()) {
      setRefs(expr.get(), result);
      setSubtypes(result, expr.get());
    }
    return result;
  }

  public Vertex breakStmt(final BreakStmt n) {
    final Optional<SimpleName> expr = n.getLabel();
    final String label = expr.isPresent() ? expr.get().toString() : "";
    final Vertex result = new Vertex(VertexType.BREAK, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex continueStmt(final ContinueStmt n) {
    final Optional<SimpleName> expr = n.getLabel();
    final String label = expr.isPresent() ? expr.get().toString() : "";
    final Vertex result = new Vertex(VertexType.CONTINUE, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex tryStmt(final TryStmt n) {
    final String label = "";
    final Vertex result = new Vertex(VertexType.TRY, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex catchClause(final CatchClause n) {
    final String label = n.getParameter().toString();
    final Vertex result = new Vertex(VertexType.CATCH, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex finallyBlock(final BlockStmt n) {
    final String label = "";
    final Vertex result = new Vertex(VertexType.FINALLY, label, n);
    setId(result);
    setOriginalLine(result, n);
    return result;
  }

  public Vertex throwStmt(final ThrowStmt n) {
    final Expression expr = n.getExpression();
    final String label = expr.toString();
    final Vertex result = new Vertex(VertexType.THROW, label, n);
    setId(result);
    setOriginalLine(result, n);
    setRefs(expr, result);
    setSubtypes(result, expr);
    return result;
  }

  public void setId(final Vertex v) {
    v.setId(id++);
  }

  public void setOriginalLine(final Vertex v, final Node n) {
    if (cfg.isOriginalLines()) {
      Integer line = null;
      if (cfg.isNormalize())
        line = findParentComment(n);
      else if (n.getRange().isPresent())
        line = n.getRange().get().begin.line;
      v.setOriginalLine(line);
    }
  }

  private Integer findParentComment(final Node n) {
    while (!n.getComment().isPresent()
        || !n.getComment().get().getContent().contains(Normalizer.COMMENT_TAG)) {
      final Node p = n.findParent(Node.class).get();
      return findParentComment(p);
    }
    return retrieveLineFromComment(n.getComment().get());
  }

  private Integer retrieveLineFromComment(final Comment comment) {
    final String intRegex = Normalizer.COMMENT_TAG + "(\\d+)";
    final String content = comment.getContent();
    final Pattern p = Pattern.compile(intRegex);
    final Matcher m = p.matcher(content);
    if (!m.find())
      return null;
    final String line = m.group(1);
    return Integer.valueOf(line);
  }

  private void setDef(final Node n, final Vertex v) {
    String def = Utils.first(names(n));
    if (n instanceof ArrayAccessExpr)
      def = ((ArrayAccessExpr) n).getName().toString();
    v.setDef(def);
  }

  private void setRefs(final Node n, final Vertex v) {
    final Set<String> uses = names(n);
    v.setRefs(uses);
  }

  // TODO: This is a preliminary construction for def and uses. Be more thorough.
  private Set<String> names(final Node ast) {
    if (ast == null)
      return new HashSet<>();
    return ast.findAll(SimpleName.class).stream().map(n -> n.getIdentifier())
        .collect(Collectors.toSet());
  }

  private void setSubtypes(final Vertex v, final Node n) {
    CollectSubtypesVisitor visitor = new CollectSubtypesVisitor();
    v.getAst().accept(visitor, v);
  }

  public static void selfCall(final Vertex v) {
    v.getSubtypes().add("SELF_CALL");
  }

  public int getId() {
    return id;
  }

  private class CollectSubtypesVisitor extends VoidVisitorAdapter<Vertex> {

    @Override
    public void visit(ArrayAccessExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("ARRAY_ACCESS");
    }

    @Override
    public void visit(BinaryExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getOperator().toString());
    }

    @Override
    public void visit(FieldAccessExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getName().toString());
    }

    @Override
    public void visit(InstanceOfExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("INSTANCE_OF");
    }

    @Override
    public void visit(ObjectCreationExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("NEW");
    }

    @Override
    public void visit(MethodCallExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getName().toString());
      v.getSubtypes().add("METHOD_CALL");
      if (n.getScope().isPresent())
        v.getSubtypes().add("SCOPED_CALL");
    }

    @Override
    public void visit(SuperExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("SUPER");
    }

    @Override
    public void visit(ThisExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("THIS");
    }

    @Override
    public void visit(UnaryExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getOperator().toString());
    }

    @Override
    public void visit(BooleanLiteralExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("" + n.getValue());
    }

    @Override
    public void visit(CharLiteralExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getValue());
    }

    @Override
    public void visit(IntegerLiteralExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getValue());
    }

    @Override
    public void visit(LongLiteralExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getValue());
    }

    @Override
    public void visit(NullLiteralExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add("null");
    }

    @Override
    public void visit(StringLiteralExpr n, Vertex v) {
      super.visit(n, v);
      v.getSubtypes().add(n.getValue());
    }

    // Expressions/statements that we may receive as a whole.
    @Override
    public void visit(ConditionalExpr n, Vertex v) {
      // We are only interested in the condition.
      n.getCondition().accept(this, v);
    }

    @Override
    public void visit(DoStmt n, Vertex v) {
      // We are only interested in the condition.
      n.getCondition().accept(this, v);
    }

    @Override
    public void visit(IfStmt n, Vertex v) {
      // We are only interested in the comparison.
      n.getCondition().accept(this, v);
    }

    @Override
    public void visit(ForStmt n, Vertex v) {
      // We are only interested in the comparison.
      n.getCompare().ifPresent(l -> l.accept(this, v));
    }

    @Override
    public void visit(ForeachStmt n, Vertex v) {
      // We are only interested in the iterable.
      n.getIterable().accept(this, v);
    }

    @Override
    public void visit(ReturnStmt n, Vertex v) {
      // We are only interested in the expression.
      n.getExpression().ifPresent(l -> l.accept(this, v));
    }

    @Override
    public void visit(WhileStmt n, Vertex v) {
      // We are only interested in the condition.
      n.getCondition().accept(this, v);
    }

	@Override
	public void visit(AssignExpr n, Vertex v) {
		super.visit(n, v);
		v.getSubtypes().add(n.getOperator().toString());
	}

  }

}
