/*******************************************************************************
 * Copyright (c) 2012-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.clojure;

import static io.usethesource.capsule.AbstractSpecialisedImmutableMap.entryOf;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import clojure.lang.APersistentMap;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashSet;
import io.usethesource.capsule.AbstractSpecialisedImmutableMap;
import io.usethesource.capsule.ImmutableSet;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhValue;

public class ClojureSetMultimap implements JmhSetMultimap {

	protected final IPersistentMap xs;

	protected ClojureSetMultimap(IPersistentMap xs) {
		this.xs = xs;
	}

	// @Override
	// public boolean isEmpty() {
	// return size() == 0;
	// }
	//
	// @Override
	// public int size() {
	// return xs.count();
	// }

	@Override
	public JmhSetMultimap put(JmhValue key, JmhValue value) {
		Object singletonOrSet = xs.valAt(key);

		if (singletonOrSet == null) {
			return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, value));
		} else if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) singletonOrSet;
			return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, set.cons(value)));
		} else if (singletonOrSet.equals(value)) {
			return this;
		} else {
			IPersistentSet set = PersistentHashSet.create(singletonOrSet, value);
			return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, set));
		}
	}

	@Override
	public JmhSetMultimap remove(JmhValue key, JmhValue value) {
		Object singletonOrSet = xs.valAt(key);

		if (singletonOrSet == null) {
			return this;
		} else if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet oldSet = (IPersistentSet) singletonOrSet;
			IPersistentSet newSet = oldSet.disjoin(value);

			switch (newSet.count()) {
			case 0:
				return new ClojureSetMultimap((IPersistentMap) xs.without(key));
			case 1:
				return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, newSet.seq().first()));
			default:
				return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, newSet));
			}
		} else {
			if (singletonOrSet.equals(value)) {
				return new ClojureSetMultimap((IPersistentMap) xs.without(key));
			} else {
				return this;
			}
		}
	}


	// @Override
	// public JmhMap removeKey(JmhValue key) {
	// return new ClojureSetMultimap((IPersistentMap) xs.without(key));
	// }

	// @Override
	// public JmhValue get(JmhValue key) {
	// return (JmhValue) xs.valAt(key);
	// }

	@Override
	public boolean containsKey(JmhValue key) {
		return xs.containsKey(key);
	}

	@Override
	public boolean contains(JmhValue key, JmhValue value) {
		Object singletonOrSet = xs.valAt(key);
		
		if (singletonOrSet == null) {
			return false;
		} else if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) singletonOrSet;
			return set.contains(value);
		} else {
			return singletonOrSet.equals(value);
		}
	}

//	@Override
//	public boolean containsValue(JmhValue value) {
//		return ((APersistentMap) xs).containsValue(value);
//	}

	@Override
	public int hashCode() {
		return xs.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other == null)
			return false;

		if (other instanceof ClojureSetMultimap) {
			ClojureSetMultimap that = (ClojureSetMultimap) other;

			return xs.equals(that.xs);
		}

		return false;
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterator<JmhValue> iterator() {
//		return ((APersistentMap) xs).keySet().iterator();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterator<JmhValue> valueIterator() {
//		return ((APersistentMap) xs).values().iterator();
//	}

	@Override
	public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
		return untypedEntryStream().flatMap(ClojureSetMultimap::dispatchOnTypeAndFlatten)
						.iterator();
	}
	
	@SuppressWarnings("unchecked")
	private Stream<Entry<JmhValue, Object>> untypedEntryStream() {
		int size = xs.count();
		Iterator<Entry<JmhValue, Object>> it = ((APersistentMap) xs).entrySet().iterator();

		Spliterator<Entry<JmhValue, Object>> split = Spliterators.spliterator(it, size,
						Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED);

		return StreamSupport.stream(split, false);
	}
	
	@SuppressWarnings("unchecked")
	private static Stream<Entry<JmhValue, JmhValue>> dispatchOnTypeAndFlatten(
					Entry<JmhValue, Object> tuple) {
		Object singletonOrSet = tuple.getValue();

		if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) singletonOrSet;

			Iterator<Entry<JmhValue, JmhValue>> it = new MultimapEntryToMapEntriesIterator(
							tuple.getKey(), ((Iterable<JmhValue>) set).iterator());

			Spliterator<Entry<JmhValue, JmhValue>> split = Spliterators.spliterator(it, set.count(),
							Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED);

			return StreamSupport.stream(split, false);
		} else {
			return Stream.of((Map.Entry<JmhValue, JmhValue>) (Object) tuple);
		}
	}
	
	private static class MultimapEntryToMapEntriesIterator
					implements Iterator<Map.Entry<JmhValue, JmhValue>> {

		final JmhValue key;
		final Iterator<JmhValue> valueIterator;

		public MultimapEntryToMapEntriesIterator(JmhValue key, Iterator<JmhValue> valueIterator) {
			this.key = key;
			this.valueIterator = valueIterator;
		}

		@Override
		public boolean hasNext() {
			return valueIterator.hasNext();
		}

		@Override
		public Entry<JmhValue, JmhValue> next() {
			return entryOf(key, valueIterator.next());
		}

	}
	
//	  @Override
//	  public Iterator<V> valueIterator() {
//	    return valueCollectionsStream().flatMap(Set::stream).iterator();
//	  }
//
//	  @Override
//	  public Iterator<Map.Entry<K, V>> entryIterator() {
//	    return new SetMultimapTupleIterator<>(rootNode, AbstractSpecialisedImmutableMap::entryOf);
//	  }
//
//	  @Override
//	  public <T> Iterator<T> tupleIterator(final BiFunction<K, V, T> tupleOf) {
//	    return new SetMultimapTupleIterator<>(rootNode, tupleOf);
//	  }
//
//	  private Spliterator<ImmutableSet<V>> valueCollectionsSpliterator() {
//	    /*
//	     * TODO: specialize between mutable / immutable ({@see Spliterator.IMMUTABLE})
//	     */
//	    int characteristics = Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED;
//	    return Spliterators.spliterator(new SetMultimapValueIterator<>(rootNode), size(),
//	        characteristics);
//	  }	

}
