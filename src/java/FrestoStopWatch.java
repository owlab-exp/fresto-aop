/**************************************************************************************
 * Copyright 2013 TheSystemIdeas, Inc and Contributors. All rights reserved.          *
 *                                                                                    *
 *     https://github.com/owlab/fresto                                                *
 *                                                                                    *
 *                                                                                    *
 * ---------------------------------------------------------------------------------- *
 * This file is licensed under the Apache License, Version 2.0 (the "License");       *
 * you may not use this file except in compliance with the License.                   *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 * 
 **************************************************************************************/
package fresto.aspects;

public class FrestoStopWatch {
	public long startTime;
	public long endTime;

	public FrestoStopWatch() {
		this.startTime = System.currentTimeMillis();
	}

	public long start() {
		this.startTime = System.currentTimeMillis();
		return this.startTime;
	}

	public long stop() {
		this.endTime = System.currentTimeMillis();
		return this.endTime;
	}

	public int getElapsedTime() {
		return (int)(endTime - startTime);
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}
}
