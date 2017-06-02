package org.apache.logging.log4j.core.appender.rolling;


public abstract class TimeBasedTriggeringPolicy implements TriggeringPolicy {
	public static TimeBasedTriggeringPolicy createPolicy(final String inteval, final String modulate) {
		throw new AssertionError("This is test code!");
	}
}
