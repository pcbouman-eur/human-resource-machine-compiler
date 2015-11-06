package nl.pcbouman.hrmc.parser;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;

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
 * Main class. Output is written to standard out.
 * If no arguments are passed, 'example.txt' is compiled.
 */

public class Main
{
	public static void main(String [] args) throws IOException
	{
		
		String fname = "example.txt";
		
		if (args.length > 0)
		{
			fname = args[0];
		}
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(fname)))
		{
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
			{
				sb.append(line+"\n");
			}
			String input = sb.toString();
			Program p = SimpleParser.PARSER.parse(input);
			System.out.println(p.buildProgram());
		}
	}
}
