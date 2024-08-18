/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.HealthTextSegment;
import com.wynntils.utils.type.CappedValue;

public class HealthTextSegmentMatcher extends AbstractTextSegmentMatcher {
    private static final SegmentSeparators SEPARATORS =
            new SegmentSeparators(NEGATIVE_SPACE_HIGH_SURROGATE, POSITIVE_SPACE_HIGH_SURROGATE);

    @Override
    protected SegmentSeparators segmentSeparators() {
        return SEPARATORS;
    }

    @Override
    protected ActionBarSegment createSegment(String segmentText, CappedValue value) {
        return new HealthTextSegment(segmentText, value);
    }
}