package pl.fepbox.blockprotect.rule;

import java.util.Collections;
import java.util.Set;

public class GlobalScope implements RuleScope {

    @Override
    public ScopeType getType() {
        return ScopeType.GLOBAL;
    }

    @Override
    public boolean contains(BlockContext context) {
        return true;
    }

    @Override
    public Set<String> getWorldNamesHint() {
        return Collections.emptySet();
    }

    @Override
    public boolean isGlobal() {
        return true;
    }
}

