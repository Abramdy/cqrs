package de.sven_torben.cqrs.infrastructure;

import java.util.List;
import java.util.function.Consumer;

import de.sven_torben.cqrs.domain.IAmAnEvent;

public final class InMemoryEventBus extends InMemoryBus<IAmAnEvent> implements
		ITransferEvents {

	@Override
	protected void handle(final IAmAnEvent msg, final List<Consumer<IAmAnEvent>> handlers) {
		handlers.parallelStream().forEach(h -> h.accept(msg));		
	}


}
