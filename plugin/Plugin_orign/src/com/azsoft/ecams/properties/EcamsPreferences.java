package com.azsoft.ecams.properties;


import com.azsoft.ecams.core.EcamsProviderPlugin;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.preferences.WorkingCopyManager;


public class EcamsPreferences {
	private Logger logger = Logger.getLogger(this.getClass());
	
	protected IScopeContext[] m_searchScope;
	
	protected IEclipsePreferences m_preferences;
	
	protected IEclipsePreferences m_original;
	
	protected IPreferencesService m_preferencesService;
	
	protected void setupWorkingCopy()
	{
		setupScope();
		m_preferences = new WorkingCopyManager().getWorkingCopy(m_original);
	}
	
	protected void setupScope(){}
	
	public EcamsPreferences()
	{
		m_preferencesService = Platform.getPreferencesService();	
		m_searchScope = null;	
	}
		
	public void restoreDefaults()
	{
		try{
			remove();
		}catch(BackingStoreException e){
			logger.error("restoreDefaults(), BackingStoreException");
		}
	}
	
	public void save()
	{
		try{
			logger.debug("SAVING PREFERENCES HERE");
			m_preferences.flush();
		}catch(BackingStoreException e){
			logger.debug("save(), BackingStoreException");
		}
	}
	
	public void restore()
	{
		m_preferences = m_original;
	}
	
	public void remove() throws BackingStoreException
	{
	    String[] children = m_original.childrenNames();
	    // Remove all keys in this node
	    m_original.clear();
	    // Now remove all children nodes
	    for (int i = 0; i < children.length; i++) {
	    	m_original.node(children[i]).removeNode();
	    }
	    // Persist to backing store
	    m_original.flush();
	    setupWorkingCopy();
	}
	

	
	public boolean getBoolean(String key)
	{
		return m_preferencesService.getBoolean(EcamsProviderPlugin.ID, key, false, m_searchScope);
	}
	
	public void putBoolean(String key, boolean value)
	{
		m_preferences.putBoolean(key, value);
	}
	
	public String getString(String key)
	{
		return m_preferencesService.getString(EcamsProviderPlugin.ID, key, "CANNOT_FIND", m_searchScope);
	}
	
	public void putString(String key, String value)
	{
		m_preferences.put(key, value);
	}
	
	public int getInt(String key)
	{
		return m_preferencesService.getInt(EcamsProviderPlugin.ID, key, -1, m_searchScope);
	}
	
	public void putInt(String key, int value)
	{
		m_preferences.putInt(key, value);
	}
}
