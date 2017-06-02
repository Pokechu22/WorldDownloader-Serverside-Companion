# log4jstub

This is simply a bit of code to allow compilation to target both log4j 2.0-beta9 and 2.8.1 (both versions Minecraft uses).  Since WDLC references some internal log4j fields (to inject custom logging), this is needed.  It's a very ugly hack, but it works.  Note that not all methods declared here will be present at runtime.