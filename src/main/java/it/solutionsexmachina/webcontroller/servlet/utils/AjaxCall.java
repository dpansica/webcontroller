package it.solutionsexmachina.webcontroller.servlet.utils;

public class AjaxCall
{

	private String					bean;
	private String					method;
	private AjaxCallParameters[]	parameters;

	public String getBean()
	{
		return bean;
	}

	public void setBean(String bean)
	{
		this.bean = bean;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public AjaxCallParameters[] getParameters()
	{
		return parameters;
	}

	public void setParameters(AjaxCallParameters[] parameters)
	{
		this.parameters = parameters;
	}

}
