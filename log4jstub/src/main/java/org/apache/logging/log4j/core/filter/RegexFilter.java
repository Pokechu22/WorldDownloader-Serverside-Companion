package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.core.Filter;

public abstract class RegexFilter implements Filter {

	public static RegexFilter createFilter(final String regex, final String useRawMsg, final String match, final String mismatch) {
		throw new AssertionError("This is test code");
	}

	public static RegexFilter createFilter(final String regex, final String[] flags, Boolean useRawMsg, final Result match, final Result mismatch) {
		throw new AssertionError("This is test code");
	}
}
