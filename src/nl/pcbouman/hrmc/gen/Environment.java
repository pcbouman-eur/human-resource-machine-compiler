package nl.pcbouman.hrmc.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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
 * Environment class which is used during code generation to keep track of the
 * locations of variables etc.
 */

public class Environment
{
	private TreeSet<Integer> freeSpots;
	private Map<String,Integer> addresses;
	private Map<String,Function> functions;

	private char label = 'a';

	public Environment(Map<String,Integer> constants, int freespots)
	{
		this.freeSpots = new TreeSet<>();
		for (int i=freespots; i >= 0; i--)
		{
			freeSpots.add(i);
		}
		this.addresses = new HashMap<>(constants);
		this.functions = new HashMap<>();
	}
	
	public void addFunction(Function f)
	{
		functions.put(f.getName(), f);
	}
	
	public int getAddress(String var)
	{
		if (!addresses.containsKey(var))
		{
			return createVar(var);
		}
		return addresses.get(var);
	}
	
	public int createTempVar()
	{
		Integer spot = freeSpots.last();
		freeSpots.remove(spot);
		return spot;
	}
	
	public void freeTempVar(int address)
	{
		freeSpots.add(address);
	}
	
	public int createVar(String name)
	{
		Integer spot = freeSpots.last();
		freeSpots.remove(spot);
		addresses.put(name, spot);
		return spot;
	}
	
	public void freeVar(String name)
	{
		Integer spot = addresses.remove(name);
		if (spot != null)
		{
			freeSpots.add(spot);
		}
	}

	public Function getFunction(String name)
	{
		return functions.get(name);
	}

	public String getLabel()
	{
		return ""+(label++);
	}
}
