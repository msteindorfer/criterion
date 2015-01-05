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
import java.util.LinkedHashMap;
import java.util.Map;

import ch.usi.overseer.OverHpc;

public class OverseerUtils {

	private final static Map<String, String> EVENTS;

	static {
		Map<String, String> eventMap = new LinkedHashMap<>();

		eventMap.put("UNHALTED_CORE_CYCLES", "Cycles");
		eventMap.put("INSTRUCTION_RETIRED", "Instructions");

		eventMap.put("L1-DCACHE-LOADS", "L1 hits");
		eventMap.put("L1-DCACHE-LOAD-MISSES", "L1 misses");
		
//		eventMap.put("L2_RQSTS:LD_HIT", "L2 hits");
//		eventMap.put("L2_RQSTS:LD_MISS", "L2 misses");

		eventMap.put("LLC_REFERENCES", "LLC hits");
		eventMap.put("LLC_MISSES", "LLC misses");

//		eventMap.put("PERF_COUNT_SW_CPU_MIGRATIONS", "CPU migrations");
//		eventMap.put("MEM_UNCORE_RETIRED:LOCAL_DRAM_AND_REMOTE_CACHE_HIT", "Local DRAM");
//		eventMap.put("MEM_UNCORE_RETIRED:REMOTE_DRAM", "Remote DRAM");

		EVENTS = Collections.unmodifiableMap(eventMap);
	}

	private static long[] results = new long[EVENTS.size()];
	private static OverHpc oHpc = OverHpc.getInstance();

	public static void setup() {
		System.out.println("OVERSEER [SETUP]");
		
		Collection<String> availableEvents = Collections.unmodifiableCollection(Arrays.asList(oHpc
						.getAvailableEventsString().split("\n")));
		
		Collection<String> unavailableEvents = new HashSet<>(EVENTS.keySet());
		unavailableEvents.removeAll(availableEvents);

		System.out.println("  AVAILABLE EVENTS: " + String.join(",", availableEvents));
		System.out.println();
		System.out.println("       USED EVENTS: " + String.join(",", EVENTS.keySet()));
		System.out.println();
		System.out.println("UNAVAILABLE EVENTS: " + String.join(",", unavailableEvents));
		System.out.println();
		
		ASSERT_RESULT(availableEvents.containsAll(EVENTS.keySet()));
		
		ASSERT_RESULT(oHpc.initEvents(String.join(",", EVENTS.keySet())));
		ASSERT_RESULT(oHpc.bindEventsToThread());
	}

	public static void doRecord(boolean doEnable) {
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

	public static void tearDown() {
		System.out.println("OVERSEER [TEAR DOWN]");

		ASSERT_RESULT(oHpc.logToFile("overseer.log"));
		
		OverHpc.shutdown();

		// final results
		printResults();
	}

	private static void printResults() {		
		System.out.println();
		for (int i = 0; i < EVENTS.size(); i++) {
			System.out.println(EVENTS.values().toArray()[i] + ": "
							+ String.format("%,d", results[i]));
		}
		System.out.println();
	}

	private static void ASSERT_RESULT(boolean result) {
		if (!result) {
			throw new RuntimeException("Problem with overseer library setup.");
		}
	}

}
