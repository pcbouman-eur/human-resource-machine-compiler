package nl.pcbouman.hrmc.parser;

import java.util.List;

import nl.pcbouman.hrmc.expr.AddSub;
import nl.pcbouman.hrmc.expr.Bump;
import nl.pcbouman.hrmc.expr.Expr;
import nl.pcbouman.hrmc.expr.IO;
import nl.pcbouman.hrmc.expr.Variable;
import nl.pcbouman.hrmc.gen.ConstantRule;
import nl.pcbouman.hrmc.gen.MemsizeRule;
import nl.pcbouman.hrmc.stmt.Assignment;
import nl.pcbouman.hrmc.stmt.Block;
import nl.pcbouman.hrmc.stmt.Call;
import nl.pcbouman.hrmc.stmt.Condition;
import nl.pcbouman.hrmc.stmt.DoWhile;
import nl.pcbouman.hrmc.stmt.Function;
import nl.pcbouman.hrmc.stmt.If;
import nl.pcbouman.hrmc.stmt.Repeat;
import nl.pcbouman.hrmc.stmt.Statement;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Token;

/*
* The MIT License (MIT)
* 
* Copyright (c) 2015 Paul Bouman
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

/**
 * My JParsec based parsers are always hard to read. Java is not really the best
 * language for this kind of stuff, but my Haskell is too rusty for a silly
 * project like this.
 * 
 * I am always struggling with proper arithmetic expression parsing, since you
 * have to avoid a left recursive grammar. You need to use JParsec's OperatorTable
 * to fix this, but I was too lazy for now. So addition is kind of awkward in the
 * current language.
 */


public class SimpleParser
{
	// Keywords and symbols in the language
	private static final Terminals OPERATORS = Terminals.operators("add","sub","(", ")", ";",
				"=", "if", "do", "while", "routine", "{", "}", "++", "--", "in",
				"out", "#CONSTANT", "#MEMSIZE", "*", "iszero", "isneg", "repeat", "not");
	
	// Skip whitespaces and java style comments
	private static final Parser<Void> IGNORED =
			   Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES).skipMany();
	
	private static final Parser<?> TOKENIZER = OPERATORS.tokenizer().cast().or(Terminals.Identifier.TOKENIZER).or(Terminals.IntegerLiteral.TOKENIZER);
	
	// A variable is just a regular identifier, or an idefinitie preceded by a * (if we are dereferencing)
	private static final Parser<Variable> VARIABLE = 
			Parsers.or(	    Parsers.sequence(OPERATORS.token("*"), Terminals.identifier().map((String s) -> new Variable(s,true))),
							Terminals.identifier().map((String s) -> new Variable(s,false)));
	
	// A bump up or down operation is a variable followed by either ++ or --
	private static final Parser<Bump> BUMP =
			Parsers.or( Parsers.sequence(VARIABLE, OPERATORS.token("++"), (Variable a, Token b) -> new Bump(a, true)),
					Parsers.sequence(VARIABLE, OPERATORS.token("--"), (Variable a, Token b) -> new Bump(a, false))
					);
	
	// The inbox is just an expression
	private static final Parser<IO> IN = OPERATORS.token("in").map((Token k) -> new IO());
	
	private static final Parser.Reference<Expr> EXPR_REF = Parser.newReference();
	
	
	// Adding or subtracting is kind of awkward in the current grammar, since it is based on prefix notation.
	// So add a 3 does what you would normally write as a+3. Similarly, sub b 6 does what you would normally
	// write as b-6
	private static final Parser<AddSub> ADDSUB = 
			Parsers.or(
					Parsers.sequence(OPERATORS.token("add"), EXPR_REF.lazy(), EXPR_REF.lazy(),
											(Token t, Expr e1, Expr e2) -> new AddSub(e1,e2,true)),
					Parsers.sequence(OPERATORS.token("sub"), EXPR_REF.lazy(), EXPR_REF.lazy(),
											(Token t, Expr e1, Expr e2) -> new AddSub(e1,e2,false))
							);
	
	
	// All the above makes up and expressions. Expressions can be written between parentheses to create more
	// complex expressions.
	private static final Parser<Expr> EXPR = 
			Parsers.or( ADDSUB, IN, BUMP, VARIABLE,
					Parsers.sequence(OPERATORS.token("("), EXPR_REF.lazy(), OPERATORS.token(")"),
							(Token t1, Expr e, Token t2) -> e)
					);
	
	static {
		EXPR_REF.set(EXPR);
	}

	private static final Parser.Reference<Statement> STATEMENT_REF = Parser.newReference();
	
	// Blocks are list of statements surrounded by curly braces. A block is a statement itself.
	private static final Parser<Block> BLOCK =
			Parsers.sequence(
					OPERATORS.token("{"),
					STATEMENT_REF.lazy().many(),
					OPERATORS.token("}"),
						(Token t1, List<Statement> l, Token t2) -> new Block(l)
					);
	
	// Assignment to a variable is performed using the = symbol
	private static final Parser<Assignment> ASSIGN =
			Parsers.sequence(VARIABLE, OPERATORS.token("="), EXPR, (Variable v, Token t, Expr e) -> new Assignment(v,e));
	
	// Conditional expressions can be written using iszero( EXPR ) or isneg( EXPR )
	private static final Parser<Condition> COND =
			Parsers.or(		Parsers.sequence(OPERATORS.token("iszero"), OPERATORS.token("("), EXPR, OPERATORS.token(")"),
								(Token t1, Token t2, Expr e, Token t3) -> new Condition(e,true)),
								Parsers.sequence(OPERATORS.token("isneg"), OPERATORS.token("("), EXPR, OPERATORS.token(")"),
										(Token t1, Token t2, Expr e, Token t3) -> new Condition(e,false)));
	
	// An if-statement consists of the if-token, followed by a conditional expression, followed by a statement.
	private static final Parser<If> IF =
			Parsers.sequence(OPERATORS.token("if"), COND, STATEMENT_REF.lazy(), (Token t1, Condition c, Statement s) -> new If(c,s,false));
	
	// An not if-statement is similar to the if, expect that it is only executed if the condition is not true.
	private static final Parser<If> NOTIF =
			Parsers.sequence(OPERATORS.token("not"), OPERATORS.token("if"), COND, STATEMENT_REF.lazy(),
					(Token t1, Token t2, Condition c, Statement s) -> new If(c,s,true));
	
	// The syntax of a do while not is: do STATEMENT while not CONDITION
	private static final Parser<DoWhile> DOWHILENOT =
			Parsers.sequence(OPERATORS.token("do"), STATEMENT_REF.lazy(), OPERATORS.token("while"), OPERATORS.token("not"), COND,
					(Token t1, Statement s, Token t2, Token t3, Condition c) -> new DoWhile(c,s,true,true));

	// The syntax of a do while is: do STATEMENT while CONDITION
	private static final Parser<DoWhile> DOWHILE =
			Parsers.sequence(OPERATORS.token("do"), STATEMENT_REF.lazy(), OPERATORS.token("while"), COND,
					(Token t1, Statement s, Token t2, Condition c) -> new DoWhile(c,s,true,false));

	// The syntax of a while not is: while not CONDITION STATEMENT
	private static final Parser<DoWhile> WHILENOT =
			Parsers.sequence(OPERATORS.token("while"), OPERATORS.token("not"), COND, STATEMENT_REF.lazy(), 
					(Token t1, Token t2, Condition c, Statement s) -> new DoWhile(c,s,false,true));

	// The syntax of a while is: while CONDITION STATEMENT
	private static final Parser<DoWhile> WHILE =
			Parsers.sequence(OPERATORS.token("while"), COND, STATEMENT_REF.lazy(), 
					(Token t1, Condition c, Statement s) -> new DoWhile(c,s,false,false));
	
	// A routine named myRoutine can be called like this: myRoutine()
	private static final Parser<Call> CALL =
			Parsers.sequence(Terminals.identifier(), OPERATORS.token("("), OPERATORS.token(")"),
					(String s, Token t1, Token t2) -> new Call(s));
	
	// The repeat keyword can be used to repeat a certain statement a couple of times.
	// The syntax is: repeat 5 STATEMENT -> the code of the statement will be repeated 5 times
	//            or: repeat STATEMENT -> the code of the statement will loop forever
	private static final Parser<Repeat> REPEAT =
			Parsers.or(
						Parsers.sequence(OPERATORS.token("repeat"), Terminals.IntegerLiteral.PARSER, STATEMENT_REF.lazy(),
									(Token t, String num, Statement s) -> new Repeat(Integer.parseInt(num), s)),
						Parsers.sequence(OPERATORS.token("repeat"), STATEMENT_REF.lazy(),
									(Token t, Statement s) -> new Repeat(0, s))
					  );
	
	// Sending something to the outbox is a statement. You could write out in to send the
	// value in the inbox directly to the outbox. Or use a different expression to send
	// to the outbox.
	private static final Parser<IO> OUT =
			Parsers.sequence(OPERATORS.token("out"), EXPR, (Token t, Expr e) -> new IO(e));
	
	// These are all statements. Some followed by semicolons.
	private static final Parser<Statement> STATEMENT =
			Parsers.or( BLOCK,
					    REPEAT,
					    ASSIGN.followedBy(OPERATORS.token(";")),
					    IF,
					    NOTIF,
					    WHILE,
					    DOWHILE.followedBy(OPERATORS.token(";")),
					    CALL.followedBy(OPERATORS.token(";")),
					    EXPR.followedBy(OPERATORS.token(";")))
			.or(Parsers.or(
					    WHILENOT,
					    DOWHILENOT.followedBy(OPERATORS.token(";")),
					    OUT.followedBy(OPERATORS.token(";"))));
	
	static {
		STATEMENT_REF.set(STATEMENT);
	}
	
	// A function is defined using the syntax: routine IDENTIFIER BLOCK
	private static final Parser<Function> FUNCTION =
			Parsers.sequence( OPERATORS.token("routine"), Terminals.identifier(), BLOCK,
						(Token t1, String n, Block b) -> new Function(n,b));
	
	// Constant value (like a zero) can be define using: #COSNTANT identifier address
	//     where identifier is a variable name and address the number of the floor tile
	//     which contains the value.
	private static final Parser<ConstantRule> CONSTANTDEF =
			Parsers.sequence( OPERATORS.token("#CONSTANT"), Terminals.identifier(), Terminals.IntegerLiteral.PARSER,
						(Token t1, String var, String address) -> new ConstantRule(var, Integer.parseInt(address))
					);
	
	// The memory size must be defined so the compiler knows which floor spots can be used
	// for storing variables.
	private static final Parser<MemsizeRule> MEMSIZEDEF =
			Parsers.sequence( OPERATORS.token("#MEMSIZE"), Terminals.IntegerLiteral.PARSER,
						(Token t1, String size) -> new MemsizeRule(Integer.parseInt(size)));
	
	private static final Parser<ProgramElement> ELEMENT =
			Parsers.or( MEMSIZEDEF, CONSTANTDEF, FUNCTION );
	
	private static final Parser<Program> PROGRAM_CORE =
			ELEMENT.many().map((List<ProgramElement> elements) -> new Program(elements));
	
	public static final Parser<Program> PARSER = PROGRAM_CORE.from(TOKENIZER, IGNORED);
}
