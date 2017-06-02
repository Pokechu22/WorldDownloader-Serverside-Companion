package org.apache.logging.log4j.core.appender.rolling;


public abstract class OnStartupTriggeringPolicy implements TriggeringPolicy {
	public static OnStartupTriggeringPolicy createPolicy() {
		throw new AssertionError("This is test code!");
	}

	public static OnStartupTriggeringPolicy createPolicy(long minSize) {
		throw new AssertionError("This is test code!");
	}
}
