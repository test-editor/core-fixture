package org.testeditor.fixture.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation that automatically executes leave when entering equal level or
 * higher level ordinal of SemanticUnit (see
 * TestDefaultTestRunReport.testSemanticUnitLeaveMultipleLevel)
 */
public class DefaultTestRunReporter implements TestRunReporter {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractTestCase.class);

	// per semantic unit only one may be active
	private Map<SemanticUnit, String> enteredUnits = new LinkedHashMap<SemanticUnit, String>();

	// each list of listener is mapped from either exactly one combination of
	// SemanticUnit x Position ...
	private Map<Pair<SemanticUnit, Position>, List<TestRunListener>> semanticPositionListeners = new LinkedHashMap<Pair<SemanticUnit, Position>, List<TestRunListener>>();
	// .. or just the Position, thus listening to this positione (enter/leave)
	// of all unit types
	private Map<Position, List<TestRunListener>> positionListeners = new LinkedHashMap<Position, List<TestRunListener>>();
	// .. or just the SemanticUnit, thus listening to enter/leave of this unit
	// type
	private Map<SemanticUnit, List<TestRunListener>> semanticListeners = new LinkedHashMap<SemanticUnit, List<TestRunListener>>();
	// .. or nothing in particular, thus listening to all enter/leave of all
	// SemanticUnits
	private List<TestRunListener> listeners = new ArrayList<TestRunListener>();

	@Override
	public void enter(SemanticUnit unit, String msg) {
		if (enteredUnits.containsKey(unit)) {
			leave(unit); // must leave before entering a new one
		}

		informListeners(unit, Position.ENTER, msg);
		enteredUnits.put(unit, msg);
	}

	/**
	 * when leaving this unit, make sure that all lower level semantic units
	 * that were entered are left, too => when leaving SemanticUnit.TEST
	 * (highest level), all other entered units are left!
	 */
	@Override
	public void leave(SemanticUnit unit) {
		for (SemanticUnit unitToLeave : enteredSortedUnitsOfEqualOrHigherOrder(unit)) {
			informListeners(unitToLeave, Position.LEAVE, enteredUnits.get(unitToLeave));
			enteredUnits.remove(unitToLeave);
		}
	}

	/**
	 * return a reverse sorted list of SemanticUnits that were entered of equal
	 * or higher ordinal
	 */
	private List<SemanticUnit> enteredSortedUnitsOfEqualOrHigherOrder(SemanticUnit unit) {
		return enteredUnits.keySet().stream() //
				.sorted((u1, u2) -> Integer.compare(u2.ordinal(), u1.ordinal())) // reverse
				.filter((u) -> u.ordinal() >= unit.ordinal()) // only >= ordinal
				.collect(Collectors.toList());
	}

	/**
	 * make sure that all registered listeners are informed, order is not
	 * guaranteed
	 */
	private void informListeners(SemanticUnit unit, Position position, String msg) {
		List<TestRunListener> toInform = new ArrayList<TestRunListener>();
		List<TestRunListener> spLs = semanticPositionListeners.get(Pair.of(unit, position));
		List<TestRunListener> pLs = positionListeners.get(position);
		List<TestRunListener> sLs = semanticListeners.get(unit);
		toInform.addAll(spLs != null ? spLs : Collections.emptyList());
		toInform.addAll(pLs != null ? pLs : Collections.emptyList());
		toInform.addAll(sLs != null ? sLs : Collections.emptyList());
		toInform.addAll(listeners);
		for (TestRunListener listener : toInform) {
			try { // make sure that an exception is handled gracefully, so that
					// other listeners are informed, too
				listener.reported(unit, position, msg);
			} catch (Exception e) {
				logger.warn("Listener threw an exception processing unit='" + unit + "', position='" + position
						+ "', msg='" + msg + "'.", e);
			}
		}
	}

	@Override
	public void addListener(SemanticUnit unit, Position position, TestRunListener listener) {
		Pair<SemanticUnit, Position> pair = Pair.of(unit, position);

		if (semanticPositionListeners.containsKey(pair)) {
			semanticPositionListeners.get(pair).add(listener);
		} else {
			List<TestRunListener> list = new ArrayList<TestRunListener>();
			list.add(listener);
			semanticPositionListeners.put(pair, list);
		}
	}

	@Override
	public void addListener(SemanticUnit unit, TestRunListener listener) {
		if (semanticListeners.containsKey(unit)) {
			semanticListeners.get(unit).add(listener);
		} else {
			List<TestRunListener> list = new ArrayList<TestRunListener>();
			list.add(listener);
			semanticListeners.put(unit, list);
		}
	}

	@Override
	public void addListener(Position position, TestRunListener listener) {
		if (positionListeners.containsKey(position)) {
			positionListeners.get(position).add(listener);
		} else {
			List<TestRunListener> list = new ArrayList<TestRunListener>();
			list.add(listener);
			positionListeners.put(position, list);
		}
	}

	@Override
	public void addListener(TestRunListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(SemanticUnit unit, Position position, TestRunListener listener) {
		Pair<SemanticUnit, Position> pair = Pair.of(unit, position);
		if (semanticPositionListeners.containsKey(pair)) {
			semanticPositionListeners.get(pair).remove(listener);
		}
	}

	@Override
	public void removeListener(TestRunListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void removeListener(Position position, TestRunListener listener) {
		List<TestRunListener> listeners = positionListeners.get(position);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public void removeListener(SemanticUnit unit, TestRunListener listener) {
		List<TestRunListener> listeners = semanticListeners.get(unit);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

}
