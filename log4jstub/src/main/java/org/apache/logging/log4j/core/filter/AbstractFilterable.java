package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.core.Filter;

public class AbstractFilterable {
	public static abstract class Builder<B extends Builder> {
		public abstract B withFilter(Filter filter);
	}
}