package com.azsoft.ecams.properties;


import org.eclipse.core.runtime.preferences.DefaultScope;

import com.azsoft.ecams.core.EcamsProviderPlugin;


public class DefaultPreferences extends EcamsPreferences {

	protected void setupScope()
	{
		m_original = new DefaultScope().getNode(EcamsProviderPlugin.ID);
	}
	
	protected void setupWorkingCopy()
	{
		setupScope();
		m_preferences = m_original;
	}	
	
	public DefaultPreferences()
	{
		setupWorkingCopy();
		
		putString(IProperty.IP,"");
		putString(IProperty.PORT,"");
		putString(IProperty.ID,"");
		putString(IProperty.PASSWD,"");
		putString(IProperty.NAME,"");
	}
}
