package org.apache.logging.log4j.core.layout;

import java.io.Serializable;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

public abstract class PatternLayout implements Layout<Serializable> {
	public static PatternLayout createLayout(final String pattern,
			Configuration config, final RegexReplacement replace,
			final String charsetName, final String always) {
		throw new AssertionError("This is test code");
	}

	public static Builder newBuilder() {
		throw new AssertionError("This is test code");
	}

	public static abstract class Builder {
		public abstract Builder withPattern(String pattern);
		public abstract Builder withHeader(String header);
		public abstract PatternLayout build();
	}
}
