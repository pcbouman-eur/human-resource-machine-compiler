package nl.pcbouman.hrmc.stmt;

import nl.pcbouman.hrmc.expr.Expr;
import nl.pcbouman.hrmc.gen.Environment;

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
 * Class which models an conditional expression.
 */

public class Condition
{
	private Expr condition;
	private boolean eq;
	
	public Condition(Expr c, boolean equality)
	{
		condition = c;
		eq = equality;
	}
	
	public String getJump(Environment env, String jumpTo)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(condition.getCode(env));
		if (eq)
		{
			sb.append("    JUMPZ    "+jumpTo+"\n");
		}
		else
		{
			sb.append("    JUMPN    "+jumpTo+"\n");
		}
		return sb.toString();
	}
	
}
