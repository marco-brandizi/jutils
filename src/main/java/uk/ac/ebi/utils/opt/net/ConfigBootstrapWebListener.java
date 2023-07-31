package uk.ac.ebi.utils.opt.net;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import uk.ac.ebi.utils.collections.OptionsMap;

/**
 * A utility {@link ServletContextListener} to manage the bootstrap initialisation of a web application. 
 * 
 * It works this way: 
 * 
 * <ul>
 * <li>You need to configure it as {@code <listener>} in your web.xml (or equivalent)</li>
 * <li>In the same file, you need to use {@code <context-param>}, to add context parameters. 
 *     Then, each named parameter will first be read from {@link System#getProperties()} and, if
 *     not null from there, the value in web.xml will be taken</li>
 * </ul>
 * 
 * <p>So, this is a way to have defaults set in the web app configuration, which can be later overridden via
 * JVM properties. There are other ways to do the same (eg, Spring), this is a very simple one, which 
 * relies on standard Java and servlet components only.</p>
 * 
 * <p><b>WARNING</b>: from the explanation above, you have to name the desired parameters you want possible override 
 * in web.xml, else they won't be ignored, even if you inject them from eg, command line (via -D).</p>
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Jul 2022</dd></dl>
 *
 * TODO: unit tests.
 */
public class ConfigBootstrapWebListener implements ServletContextListener
{
	private static final Map<String, String> bootstrapParameters = new HashMap<> ();
	
	@Override
	public void contextInitialized ( ServletContextEvent sce )
	{
		sce.getServletContext ().getInitParameterNames ().asIterator ()
		.forEachRemaining ( initParamName ->
		{
			String paramValue = System.getProperty ( 
				initParamName,
				sce.getServletContext ().getInitParameter ( initParamName )
			);
			
			bootstrapParameters.put ( initParamName, paramValue );
		});
	}

	@Override
	public void contextDestroyed ( ServletContextEvent sce )
	{
		// Does nothing, added just for compatibility with servlet APIs.
	}

	
	@SuppressWarnings ( "unchecked" )
	public static OptionsMap getBootstrapParameters ()
	{
		return OptionsMap.unmodifiableOptionsMap ( (Map<String, Object>) (Map<?, ?>) bootstrapParameters );
	}
}
