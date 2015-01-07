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

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

	private static final Set<String> PERF_EVENTS;

	private static final String PERF_OUTPUT_SEPARATOR = System.getProperty(
					"overseer.utils.output.separator", ",");

	private static final String PERF_OUTPUT_FILE = System.getProperty("overseer.utils.output.file",
					"perf_events.log");

	static {
		List<String> eventList = Arrays.asList(System.getProperty("overseer.utils.events",
						"LLC_REFERENCES,LLC_MISSES").split(","));

		Set<String> eventSet = new LinkedHashSet<>(eventList);

		PERF_EVENTS = Collections.unmodifiableSet(eventSet);
		
		try {
			Path outputFilePath = Paths.get(PERF_OUTPUT_FILE);
			Files.delete(outputFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Long[] results = new Long[PERF_EVENTS.size()];
	private static OverHpc oHpc = OverHpc.getInstance();

	public static void setup() {
		System.out.println("OVERSEER [SETUP]");

		Collection<String> availableEvents = Collections.unmodifiableCollection(Arrays.asList(oHpc
						.getAvailableEventsString().split("\n")));

		Collection<String> unavailableEvents = new HashSet<>(PERF_EVENTS);
		unavailableEvents.removeAll(availableEvents);

		System.out.println("  AVAILABLE EVENTS: " + String.join(",", availableEvents));
		System.out.println();
		System.out.println("       USED EVENTS: " + String.join(",", PERF_EVENTS));
		System.out.println();
		System.out.println("UNAVAILABLE EVENTS: " + String.join(",", unavailableEvents));
		System.out.println();

		// msteindorfer: disabled, because it can only check events without
		// options
		// ASSERT_RESULT(availableEvents.containsAll(EVENTS));

		ASSERT_RESULT(oHpc.initEvents(String.join(",", PERF_EVENTS)));
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
				for (int i = 0; i < PERF_EVENTS.size(); i++) {
					results[i] = oHpc.getEventFromThread(tid, i);
				}

				// intermediate results
				printResults();
			}
		}
	}

	public static void tearDown() {
		System.out.println("OVERSEER [TEAR DOWN]");

		if (!DO_START_STOP) {
			int tid = oHpc.getThreadId();

			System.out.println("OVERSEER [RECORD OFF]");
			ASSERT_RESULT(oHpc.stop());
			for (int i = 0; i < PERF_EVENTS.size(); i++) {
				results[i] = oHpc.getEventFromThread(tid, i);
			}
		}

		ASSERT_RESULT(oHpc.logToFile("overseer.log"));

		OverHpc.shutdown();

		// final results
		// printResults();
	}

	private static void printResults() {
		System.out.println();
		for (int i = 0; i < PERF_EVENTS.size(); i++) {
			System.out.println(PERF_EVENTS.toArray()[i] + ": " + String.format("%,d", results[i]));
		}
		System.out.println();

		try {
			Path outputFilePath = Paths.get(PERF_OUTPUT_FILE);

			if (!Files.exists(outputFilePath)) {
				Files.createFile(outputFilePath);

				String outputFileHeader = String.join(PERF_OUTPUT_SEPARATOR, PERF_EVENTS);

				Files.write(outputFilePath, Collections.singleton(outputFileHeader),
								StandardOpenOption.APPEND);
			}

			/*
			 * TODO: doesn't work if results is long[] instead of Long[]
			 * 
			 * See:
			 *   http://stackoverflow.com/questions/754294/convert-an-array-of-primitive-longs-into-a-list-of-longs/1974363#1974363
			 *   http://stackoverflow.com/questions/1979767/converting-an-array-of-long-to-arraylistlong
			 */
			String measurements = Arrays.asList(results).stream().map(String::valueOf)
							.collect(joining(PERF_OUTPUT_SEPARATOR));

			Files.write(outputFilePath, Collections.singleton(measurements),
							StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void ASSERT_RESULT(boolean result) {
		if (!result) {
			throw new RuntimeException("Problem with overseer library setup.");
		}
	}

}
