package org.apache.logging.log4j.core.appender.rolling;

public abstract class CompositeTriggeringPolicy implements TriggeringPolicy {
	public static CompositeTriggeringPolicy createPolicy(final TriggeringPolicy... policies) {
		throw new AssertionError("This is test code");
	}
}
