package com.squareup.otto;

public class Bus2 extends Bus {
    public Bus2() {
    }

    public Bus2(String identifier) {
        super(identifier);
    }

    public Bus2(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    public Bus2(ThreadEnforcer enforcer, String identifier) {
        super(enforcer, identifier);
    }

    public Bus2(ThreadEnforcer enforcer, String identifier, HandlerFinder handlerFinder) {
        super(enforcer, identifier, handlerFinder);
    }
}
