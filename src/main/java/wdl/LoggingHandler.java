package wdl;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Class to contain the methods that setup WDL-specific logging.
 */
public class LoggingHandler {
	private LoggingHandler() {}
	
	/**
	 * Attempt to add WDL-specific logging.  
	 */
	public static void setupLogging() throws Exception {
		Logger logger = (Logger)LogManager.getRootLogger();
		if (logger.getAppenders().containsKey("WDL")) {
			return;
		}
		
		String fileName = "logs/WDL/latest.log";
		String filePattern = "logs/WDL/%d{yyyy-MM-dd}-%i.log.gz";
		String name = "WDL";
		
		TriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(
				TimeBasedTriggeringPolicy.createPolicy("1", "false"), //1 day
				OnStartupTriggeringPolicy.createPolicy());
		
		Layout<String> layout = PatternLayout.createLayout(
				"[%d{HH:mm:ss}] [%t/%level]: %msg%n", null, null, null, null);
		
		Filter filter = RegexFilter.createFilter(
				"[\\s\\S]*\\[WDL(?:Companion)?\\][\\s\\S]*", "false", "ACCEPT",
				"DENY");
		
		Configuration config = stealLoggerConfig(logger);
		
		Appender appender = RollingRandomAccessFileAppender.createAppender(
				fileName, filePattern, null, name, null, policy, null,
				layout, filter, null, null, null, config);
		
		appender.start();
		
		logger.addAppender(appender);
	}
	
	/**
	 * Uses reflection to get the protected {@link Logger#config}'s
	 * {@link Logger.PrivateConfig#config} field.
	 * 
	 * @param logger
	 * @return
	 */
	private static Configuration stealLoggerConfig(Logger logger)
			throws Exception {
		Field privateConfigField = Logger.class.getDeclaredField("config");
		privateConfigField.setAccessible(true);
		Object privateConfig = privateConfigField.get(logger);
		
		Field configField = privateConfig.getClass().getDeclaredField("config");
		configField.setAccessible(true);
		
		return (Configuration) configField.get(privateConfig);
	}
}
