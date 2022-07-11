package hystrix.circuit.breaker;

import com.netflix.turbine.init.TurbineInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnusedDeclaration")
public class TurbineContextListener implements ServletContextListener {
	private static AtomicBoolean turbineInited = new AtomicBoolean(false);
	private static final Logger log = LoggerFactory.getLogger(TurbineContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		if (!turbineInited.getAndSet(true)) {
			try {
				TurbineInit.init();
			} catch (Exception e) {
				if (!e.getMessage().contains("already started")) {
					log.error("Calling TurbineInit.init()", e);
				}
			}
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do
	}
}
