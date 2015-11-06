package nl.pcbouman.hrmc.stmt;

import nl.pcbouman.hrmc.gen.Environment;

/**
 * Class which models all kinds of loop;
 * do-while, while, infinite repeat. Also variants with a not.
 */

public class DoWhile extends Statement
{
	private Condition condition;
	private Statement repeat;
	private boolean dowhile;
	private boolean not;

	public DoWhile(Condition cond, Statement rep, boolean b, boolean n)
	{
		condition = cond;
		repeat = rep;
		dowhile = b;
		not = n;
	}
	
	@Override
	public String getCode(Environment env)
	{
		StringBuilder sb = new StringBuilder();
		if (not)
		{
			if (dowhile)
			{
				String label = env.getLabel();
				sb.append(label+":\n");
				sb.append(repeat.getCode(env));
				String label2 = env.getLabel();
				sb.append(condition.getJump(env, label2));
				sb.append("    JUMP     "+label+"\n");
				sb.append(label2+":\n");
			}
			else
			{
				String label = env.getLabel();
				String label2 = env.getLabel();
				sb.append(label+":\n");
				sb.append(condition.getJump(env,label2));
				sb.append(repeat.getCode(env));
				sb.append("    JUMP     "+label+"\n");
				sb.append(label2+":\n");
			}
		}
		else
		{
			if (dowhile)
			{
				String label = env.getLabel();
				sb.append(label+":\n");
				sb.append(repeat.getCode(env));
				sb.append(condition.getJump(env, label));
			}
			else
			{
				String label1 = env.getLabel();
				String label2 = env.getLabel();
				sb.append(label1+":\n");
				sb.append(condition.getJump(env, label2));
				sb.append(repeat.getCode(env));
				sb.append(label2+":\n");
			}
		}
		return sb.toString();
	}

}
