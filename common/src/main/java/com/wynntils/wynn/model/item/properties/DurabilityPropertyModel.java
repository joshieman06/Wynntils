/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.DurabilityProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class DurabilityPropertyModel extends Model {
    private static final ItemPropertyWriter DURABILITY_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isDurabilityItem, DurabilityProperty::new);

    public static void init() {
        Managers.ItemStackTransform.registerProperty(DURABILITY_WRITER);
    }

    public static void disable() {
        Managers.ItemStackTransform.unregisterProperty(DURABILITY_WRITER);
    }
}