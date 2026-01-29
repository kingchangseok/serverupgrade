package com.azsoft.ecams.properties;

import org.eclipse.core.runtime.preferences.InstanceScope;

import com.azsoft.ecams.core.EcamsProviderPlugin;

public class WorkspacePreferences extends EcamsPreferences
{	
	protected void setupScope()
	{
		m_original = new InstanceScope().getNode(EcamsProviderPlugin.ID);
	}
	
	public WorkspacePreferences()
	{
		setupWorkingCopy();
	}
}
