package org.apache.logging.log4j.core.appender;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.AbstractFilterable;

public abstract class AbstractAppender implements Appender {
	public static abstract class Builder<B extends Builder> extends AbstractFilterable.Builder<B> {
		public abstract B withName(String name);
		public abstract B withLayout(Layout<? extends Serializable> layout);
		public abstract B setConfiguration(Configuration config);
	}
}
