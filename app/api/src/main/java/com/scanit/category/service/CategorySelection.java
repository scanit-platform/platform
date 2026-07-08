package com.scanit.category.service;

import com.scanit.category.model.CustomCategory;
import com.scanit.category.model.GeneralCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Carries managed JPA category references resolved inside the service transaction."
)
public record CategorySelection(
        GeneralCategory generalCategory,
        CustomCategory customCategory
) { }
