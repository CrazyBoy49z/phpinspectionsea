<?php

class CasesHolder {
    private function simplification() {
        return [
            <warning descr="'empty(...) && ... === null' here can be replaced with '!isset(...)'.">empty($x)</warning> && $x === null,
            empty($x) && $x !== null,
            !<warning descr="!empty(...) || ... !== null' here can be replaced with 'isset(...)'.">empty($x)</warning> || $x !== null,
            !empty($x) || $x === null,
            <warning descr="'isset(...) && ...' here can be replaced with '!empty(...)'.">isset($x)</warning> && $x,
            isset($x) && !$x,
            !<warning descr="'!isset(...) || !...' here can be replaced with 'empty(...)'.">isset($x)</warning> || !$x,
            !isset($x) || $x,
        ];
    }

    private function processAll() {
        return [
            isset($x)
                && !<warning descr="Doesn't match to previous null value handling (perhaps always false when reached).">isset($x)</warning>
                && <warning descr="Seems to be always true when reached.">$x !== null</warning>,
            empty($x)
                && !<warning descr="Doesn't match to previous falsy value handling (perhaps always false when reached).">empty($x)</warning>
                && !<warning descr="Seems to be always true when reached.">$x</warning>,
        ];
    }

    private function issetEmptyMix() {
        return[
            isset($x) && !empty($x),
            isset($x) && <warning descr="Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
            isset($x) || !empty($x),
            isset($x) || <warning descr="Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
            !isset($x) || empty($x),
            !isset($x) || !<warning descr="Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
            !isset($x) && empty($x),
            !isset($x) && !<warning descr="Doesn't match to previous isset-alike handling (perhaps always false when reached).">empty($x)</warning>,
        ];
    }

    private function nullValueChecks() {
        return [
            isset($x) && <warning descr="Seems to be always true when reached.">$x !== null</warning>,
            isset($x) || <warning descr="Seems to be always true when reached.">$x !== null</warning>,
            !isset($x) || <warning descr="Seems to be always true when reached.">$x === null</warning>,
            !isset($x) && <warning descr="Seems to be always true when reached.">$x === null</warning>,
            isset($x) && <warning descr="Doesn't match to previous null value handling (perhaps always false when reached).">$x === null</warning>,
            isset($x) || <warning descr="Doesn't match to previous null value handling (perhaps always false when reached).">$x === null</warning>,
            !isset($x) || <warning descr="Doesn't match to previous null value handling (perhaps always false when reached).">$x !== null</warning>,
            !isset($x) && <warning descr="Doesn't match to previous null value handling (perhaps always false when reached).">$x !== null</warning>,
        ];
    }

    private function falsyValueChecks() {
        return [
            empty($x) && !<warning descr="Seems to be always true when reached.">$x</warning>,
            !empty($x) || <warning descr="Seems to be always true when reached.">$x</warning>,
            empty($x) && <warning descr="Doesn't match to previous falsy value handling (perhaps always false when reached).">$x</warning>,
            !empty($x) || !<warning descr="Doesn't match to previous falsy value handling (perhaps always false when reached).">$x</warning>,
        ];
    }

    private function reportingTargetsSelection() {
        return [
            isset(<warning descr="'isset(...) && ...' here can be replaced with '!empty(...)'.">$x</warning>, $y) && $x,
            !isset(<warning descr="'!isset(...) || !...' here can be replaced with 'empty(...)'.">$x</warning>, $y) || !$x,
        ];
    }

    private function issetInNullCoallescingContext($parameter) {
        return [
            <weak_warning descr="'$parameter ?? '...'' can be used instead (reduces cognitive load).">isset($parameter) ?? '...'</weak_warning>,
            <weak_warning descr="'$parameter ?? '...'' can be used instead (reduces cognitive load).">isset($parameter) ?: '...'</weak_warning>,
        ];
    }

    private function emptyInDirectArgumentUsageContext($parameter) {
        if (<weak_warning descr="'$parameter' can be used instead (reduces cognitive load).">!empty($parameter)</weak_warning>) {}
        if (<weak_warning descr="'!$parameter' can be used instead (reduces cognitive load).">empty($parameter)</weak_warning>) {}

        return [
            <weak_warning descr="'!$parameter' can be used instead (reduces cognitive load).">empty($parameter)</weak_warning> ? '+' : '-',
            <weak_warning descr="'$parameter' can be used instead (reduces cognitive load).">!empty($parameter)</weak_warning> ? '+' : '-',
        ];
    }
}