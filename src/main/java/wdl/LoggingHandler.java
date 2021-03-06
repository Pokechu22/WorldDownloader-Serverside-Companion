package wdl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.zip.Deflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.helper.Action;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * Class to contain the methods that setup WDL-specific logging. <br/>
 * This works by modifying the existing server logger to also log to a new log
 * file. It has to take a private field and do some other silly things, but it's
 * not too weird (at least it's not obfuscated). <br/>
 * The log is generated in a subfolder of the server logs folder, named "WDL".
 */
public class LoggingHandler {
	private LoggingHandler() { }

	private static final String FILE_NAME = "logs/WDL/latest.log";
	private static final String APPENDER_NAME = "WDL";

	/**
	 * Attempt to add WDL-specific logging.
	 * 
	 * @param mode
	 *            The mode to use, from the config key 'wdl.logMode'.
	 */
	public static void setupLogging(String mode) throws Exception {
		if (mode == null || mode.equalsIgnoreCase("none")) {
			return;
		}

		Logger logger = (Logger) LogManager.getRootLogger();
		if (logger.getAppenders().containsKey(APPENDER_NAME)) {
			// Already initalized
			return;
		}

		Field privateConfigField;
		boolean isNewLog4j;
		// Handle (and use) the field name change between log4j versions
		try {
			privateConfigField = Logger.class.getDeclaredField("privateConfig");
			isNewLog4j = true;
		} catch (NoSuchFieldException ex) {
			privateConfigField = Logger.class.getDeclaredField("config");
			isNewLog4j = false;
		}
		privateConfigField.setAccessible(true);
		Object privateConfig = privateConfigField.get(logger);

		Field configField = privateConfig.getClass().getDeclaredField("config");
		configField.setAccessible(true);

		Configuration config = (Configuration) configField.get(privateConfig);

		if (isNewLog4j) {
			handleNewLog4j(config, logger, mode);
		} else {
			handleOldLog4j(config, logger, mode);
		}
	}

	/** Sets up logging for new (2.8.1) log4j, used in Minecraft after 1.12 */
	private static void handleNewLog4j(Configuration config, Logger logger, String mode) {
		StringBuilder msg = new StringBuilder();
		for (String line : logHeader) {
			msg.append(line).append(System.lineSeparator());
		}

		PatternLayout layout = PatternLayout.newBuilder()
				.withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n")
				.withHeader(msg.toString())
				.build();

		Filter filter = RegexFilter.createFilter(
				"[\\s\\S]*\\[WDL[\\s\\S]*\\][\\s\\S]*", new String[0], Boolean.FALSE, Result.ACCEPT,
				Result.DENY);

		if (mode.equalsIgnoreCase("individual")) {
			String filePattern = "logs/WDL/%d{yyyy-MM-dd}-%i.log.gz";

			TriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(
					TimeBasedTriggeringPolicy.createPolicy("1", "false"),
					OnStartupTriggeringPolicy.createPolicy(0));

			Appender appender = RollingRandomAccessFileAppender.newBuilder()
					.withFileName(FILE_NAME)
					.withFilePattern(filePattern)
					.withName(APPENDER_NAME)
					.withPolicy(policy)
					.withLayout(layout)
					.withFilter(filter)
					.setConfiguration(config)
					.build();

			appender.start();

			logger.addAppender(appender);
		} else if (mode.equalsIgnoreCase("combined")) {
			Appender appender = RandomAccessFileAppender.newBuilder()
					.setFileName(FILE_NAME)
					.withName(APPENDER_NAME)
					.withLayout(layout)
					.withFilter(filter)
					.setConfiguration(config)
					.build();

			appender.start();

			logger.addAppender(appender);
		}
	}

	/** Sets up logging for old (2.0-beta9) log4j, used in Minecraft before 1.12 */
	private static void handleOldLog4j(Configuration config, Logger logger, String mode) {
		// Wrapper class only for setting a hook to execute()
		class DelegateAction implements Action {
			final Action delegate;
			final String fileName;

			final org.apache.logging.log4j.Logger logger;

			public DelegateAction(final Action delegate, final String fileName,
					org.apache.logging.log4j.Logger errorLogger) {
				this.delegate = delegate;
				this.fileName = fileName;

				this.logger = errorLogger;
			}

			@Override
			public void run() {
				delegate.run();
			}

			@Override
			public boolean execute() throws IOException {
				boolean ret = delegate.execute();

				BufferedWriter writer = null;
				try {
					writer = new BufferedWriter(new FileWriter(new File(fileName),
							true));

					for (String s : logHeader) {
						writer.write(s);
						writer.write(System.lineSeparator());
					}
				} catch (Throwable e) {
					logger.error("Writing to top of new logfile \"" + fileName
							+ "\" with", e);
				} finally {
					try {
						writer.close();
					} catch (Throwable e) {
						logger.error("Closing writer", e);
					}
				}

				return ret;
			}

			@Override
			public void close() {
				delegate.close();
			}

			@Override
			public boolean isComplete() {
				return delegate.isComplete();
			}
		}

		/**
		 * Wrapper class only for setting a hook to getSynchronous().execute()
		 */
		class DelegateRolloverDescription implements RolloverDescription {
			private final RolloverDescription delegate;
			private final org.apache.logging.log4j.Logger logger;

			public DelegateRolloverDescription(RolloverDescription delegate,
					org.apache.logging.log4j.Logger errorLogger) {
				this.delegate = delegate;
				this.logger = errorLogger;
			}

			@Override
			public String getActiveFileName() {
				return delegate.getActiveFileName();
			}

			@Override
			public boolean getAppend() {
				// return delegate.getAppend();
				// As long as we already put some data to the top of the new
				// logfile, subsequent writes should be performed with "append".
				return true;
			}

			// The synchronous action is for renaming, here we want to hook
			@Override
			public Action getSynchronous() {
				Action delegateAction = delegate.getSynchronous();
				if (delegateAction == null) {
					return null;
				}
				return new DelegateAction(delegateAction,
						delegate.getActiveFileName(), logger);
			}

			// The asynchronous action is for compressing, we don't need to hook
			// here
			@Override
			public Action getAsynchronous() {
				return delegate.getAsynchronous();
			}
		}

		/**
		 * A RolloverStrategy to hook the DefaultRolloverStrategy's rollover events
		 * to write headers and footers. <br/>
		 * Yes, this is stupidly large. It's the only way with the needed version of
		 * log4j, though. <br/>
		 * Based off of Joe Merten's code here: http://stackoverflow.com/a/20979314
		 */
		class HeaderRolloverStrategy extends DefaultRolloverStrategy {
			public HeaderRolloverStrategy(Configuration config) {
				super(1, 1000, true, Deflater.DEFAULT_COMPRESSION, config
						.getStrSubstitutor());
			}

			public RolloverDescription rollover(final RollingFileManager manager) {
				RolloverDescription ret = super.rollover(manager);
				return new DelegateRolloverDescription(ret, LOGGER);
			}
		}

		PatternLayout layout = PatternLayout.createLayout(
				"[%d{HH:mm:ss}] [%t/%level]: %msg%n", null, null, null, null);

		Filter filter = RegexFilter.createFilter(
				"[\\s\\S]*\\[WDL[\\s\\S]*\\][\\s\\S]*", "false", "ACCEPT",
				"DENY");

		if (mode.equalsIgnoreCase("individual")) {
			String filePattern = "logs/WDL/%d{yyyy-MM-dd}-%i.log.gz";

			TriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(
					TimeBasedTriggeringPolicy.createPolicy("1", "false"),
					OnStartupTriggeringPolicy.createPolicy());

			RolloverStrategy strategy = new HeaderRolloverStrategy(config);

			Appender appender = RollingRandomAccessFileAppender.createAppender(
					FILE_NAME, filePattern, null, APPENDER_NAME, null, policy, strategy,
					layout, filter, null, null, null, config);

			appender.start();

			logger.addAppender(appender);
		} else if (mode.equalsIgnoreCase("combined")) {
			File logFile = new File(FILE_NAME);
			if (!logFile.exists()) {
				logFile.getParentFile().mkdirs(); // Ensure folder exists

				BufferedWriter writer = null;
				try {
					writer = new BufferedWriter(new FileWriter(logFile, true));

					for (String s : logHeader) {
						writer.write(s);
						writer.write(System.lineSeparator());
					}
				} catch (Throwable e) {
					StatusLogger.getLogger().error(
							"Writing to top of new logfile \"" + FILE_NAME
							+ "\" with", e);
				} finally {
					try {
						writer.close();
					} catch (Throwable e) {
						StatusLogger.getLogger().error("Closing writer", e);
					}
				}
			}

			Appender appender = RandomAccessFileAppender.createAppender(
					FILE_NAME, null, APPENDER_NAME, null, null, layout, filter, null,
					null, config);

			appender.start();

			logger.addAppender(appender);
		}
	}

	private static final String[] logHeader = {
"This is a log of all of the messages generated by the WDL Companion plugin.",
"It can be used to quickly find who has WDL installed.  However, this should",
"*not* be used to ban players.  First off, WDL is mainly intended for making",
"backups of one's own creations.  Banning a player for that is not nice.  In",
"addition, the fact that a player appears here only means that they have the",
"mod installed, not that they used it on the server.  (Technically, it means",
"that the server received a 'WDL|INIT' plugin channel packet).  Finally, the",
"player wouldn't have been able to download on the server (unless the plugin",
"is configured to allow it, or they were manually exempted).",
"",
"Some further information: 'WDL|INIT' is sent whenever the player has joined",
"the server, whenever they change worlds on the server (including going from",
"and to the nether and the end), and when the saving of a world is finished.",
"Please visit wiki.vg/Plugin_channels/World_downloader for more information.",
"",
"If you want to disable this log, please set logType to 'none' in the plugin",
"config file.  The actual log begins below.", "" };
}
