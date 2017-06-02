package org.apache.logging.log4j.core.appender;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.Configuration;

public abstract class RandomAccessFileAppender extends AbstractAppender implements Appender {
	public static RandomAccessFileAppender createAppender(final String fileName, final String append, final String name, final String immediateFlush, final String ignore, Layout<? extends Serializable> layout, final Filter filter, final String advertise, final String advertiseURI, final Configuration config) {
		throw new AssertionError("This is test code!");
	}

	public static Builder newBuilder() {
		throw new AssertionError("This is test code!");
	}

	public static abstract class Builder extends AbstractAppender.Builder<Builder> {
		public abstract Builder setFileName(String fileName);
		public abstract RandomAccessFileAppender build();
	}
}
