package app.core.context;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EcamsContext implements ApplicationContextAware {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private static ApplicationContext appCtx;

	public EcamsContext() {
	}

	/** Spring supplied interface method for injecting app context. */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appCtx = applicationContext;
	}

	/** Access to spring wired beans. */
	public static ApplicationContext getContext() {
		return appCtx;
	}

	

}
