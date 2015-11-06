package nl.pcbouman.hrmc.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.pcbouman.hrmc.gen.ConstantRule;
import nl.pcbouman.hrmc.gen.Environment;
import nl.pcbouman.hrmc.gen.MemsizeRule;
import nl.pcbouman.hrmc.stmt.Function;

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
 * Class which models a full parsed program, including the constant and memsize
 * directives.
 */

public class Program
{
	private List<Function> functions;
	private List<ConstantRule> rules;
	private MemsizeRule msr;
	
	public Program(List<ProgramElement> els)
	{
		functions = new ArrayList<>();
		rules = new ArrayList<>();
		
		for (ProgramElement pe : els)
		{
			if (pe instanceof Function)
			{
				functions.add((Function) pe);
			}
			if (pe instanceof ConstantRule)
			{
				rules.add((ConstantRule) pe);
			}
			if (pe instanceof MemsizeRule)
			{
				if (msr != null)
				{
					throw new IllegalStateException("Memsize can only be defined once!");
				}
				msr = (MemsizeRule) pe;
			}
		}
		if (msr == null)
		{
			throw new IllegalStateException("Memsize must be defined in the program!");
		}
	}
	
	public Environment buildEnvironment()
	{
		Map<String,Integer> addresses = new HashMap<>();
		for (ConstantRule cr : rules)
		{
			addresses.put(cr.getName(), cr.getAddress());
		}
		
		Environment e = new Environment(addresses, msr.getMemSize());
		for (Function f : functions)
		{
			e.addFunction(f);
		}
		return e;
	}
	
	public String buildProgram()
	{
		Environment e = buildEnvironment();
		Function main = e.getFunction("main");
		if (main == null)
		{
			throw new IllegalStateException("No main routine defined!");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("-- HUMAN RESOURCE MACHINE PROGRAM --\n\n");
		sb.append(main.getCode(e));
		return sb.toString();
	}
}
