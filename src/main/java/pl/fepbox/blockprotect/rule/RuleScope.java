package pl.fepbox.blockprotect.rule;

import java.util.Set;

public interface RuleScope {

    ScopeType getType();

    boolean contains(BlockContext context);

    /**
     * Returns a set of world names this scope may apply to,
     * or empty if it is global.
     */
    Set<String> getWorldNamesHint();

    boolean isGlobal();
}

