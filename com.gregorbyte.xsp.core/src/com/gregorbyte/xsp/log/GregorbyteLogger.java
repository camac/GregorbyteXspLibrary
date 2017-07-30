package com.gregorbyte.xsp.log;

import com.ibm.commons.log.Log;
import com.ibm.commons.log.LogMgr;

public class GregorbyteLogger extends Log {

	public static final LogMgr CORE = load("com.gregorbyte.xsp.core");
	public static final LogMgr CONTROLS = load("com.gregorbyte.xsp.controls");
	public static final LogMgr MIME = load("com.gregorbyte.xsp.mime");
	public static final LogMgr RESOURCES = load("com.gregorbyte.xsp.resources");
	
}
