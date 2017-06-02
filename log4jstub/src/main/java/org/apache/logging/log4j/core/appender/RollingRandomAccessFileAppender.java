package org.apache.logging.log4j.core.appender;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;

public abstract class RollingRandomAccessFileAppender extends AbstractAppender implements Appender {
	public static RollingRandomAccessFileAppender createAppender(final String fileName, final String filePattern, final String append, final String name, final String immediateFlush, final TriggeringPolicy policy, RolloverStrategy strategy, Layout<? extends Serializable> layout, final Filter filter, final String ignore, final String advertise, final String advertiseURI, final Configuration config) {
		throw new AssertionError("This is test code!");
	}

	public static Builder newBuilder() {
		throw new AssertionError("This is test code!");
	}

	public static abstract class Builder extends AbstractAppender.Builder<Builder> {
		public abstract Builder withFileName(String name);
		public abstract Builder withFilePattern(String pattern);
		public abstract Builder withPolicy(TriggeringPolicy policy);
		public abstract RollingRandomAccessFileAppender build();
	}
}
