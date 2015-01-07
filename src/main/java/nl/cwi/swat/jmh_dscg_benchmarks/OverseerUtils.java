/*******************************************************************************
 * Copyright (c) 2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package nl.cwi.swat.jmh_dscg_benchmarks;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.usi.overseer.OverHpc;

public class OverseerUtils {

	private static final boolean DO_START_STOP = true; // System.getProperties().containsKey("overseer.utils.doStartStop");
	
	private final static Set<String> EVENTS;

	static {
		List<String> eventList = Arrays.asList(System.getProperty("overseer.utils.events",
						"LLC_REFERENCES,LLC_MISSES").split(","));

		Set<String> eventSet = new LinkedHashSet<>(eventList);

		EVENTS = Collections.unmodifiableSet(eventSet);
	}

	private static long[] results = new long[EVENTS.size()];
	private static OverHpc oHpc = OverHpc.getInstance();

	public static void setup() {
		System.out.println("OVERSEER [SETUP]");
		
		Collection<String> availableEvents = Collections.unmodifiableCollection(Arrays.asList(oHpc
						.getAvailableEventsString().split("\n")));
		
		Collection<String> unavailableEvents = new HashSet<>(EVENTS);
		unavailableEvents.removeAll(availableEvents);

		System.out.println("  AVAILABLE EVENTS: " + String.join(",", availableEvents));
		System.out.println();
		System.out.println("       USED EVENTS: " + String.join(",", EVENTS));
		System.out.println();
		System.out.println("UNAVAILABLE EVENTS: " + String.join(",", unavailableEvents));
		System.out.println();
		
		// msteindorfer: disabled, because it can only check events without options
		// ASSERT_RESULT(availableEvents.containsAll(EVENTS));
		
		ASSERT_RESULT(oHpc.initEvents(String.join(",", EVENTS)));
		ASSERT_RESULT(oHpc.bindEventsToThread());
		
		if (!DO_START_STOP) {
			System.out.println("OVERSEER [RECORD ON]");
			ASSERT_RESULT(oHpc.start());
		}
	}

	public static void doRecord(boolean doEnable) {
		if (DO_START_STOP) {	
			int tid = oHpc.getThreadId();
	
			if (doEnable) {
				System.out.println("OVERSEER [RECORD ON]");
				ASSERT_RESULT(oHpc.start());
			} else {
				System.out.println("OVERSEER [RECORD OFF]");
				ASSERT_RESULT(oHpc.stop());
				for (int i = 0; i < EVENTS.size(); i++) {
					results[i] = oHpc.getEventFromThread(tid, i);
				}
				
				// intermediate results
				// printResults();
			}
		}
	}

	public static void tearDown() {
		System.out.println("OVERSEER [TEAR DOWN]");

		if (!DO_START_STOP) {
			int tid = oHpc.getThreadId();
			
			System.out.println("OVERSEER [RECORD OFF]");
			ASSERT_RESULT(oHpc.stop());
			for (int i = 0; i < EVENTS.size(); i++) {
				results[i] = oHpc.getEventFromThread(tid, i);
			}			
		}
		
		ASSERT_RESULT(oHpc.logToFile("overseer.log"));		
		
		OverHpc.shutdown();

		// final results
		printResults();
	}

	private static void printResults() {
		System.out.println();
		for (int i = 0; i < EVENTS.size(); i++) {
			System.out.println(EVENTS.toArray()[i] + ": " + String.format("%,d", results[i]));
		}
		System.out.println();
	}

	private static void ASSERT_RESULT(boolean result) {
		if (!result) {
			throw new RuntimeException("Problem with overseer library setup.");
		}
	}

}
