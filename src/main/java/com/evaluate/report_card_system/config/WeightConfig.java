package com.evaluate.report_card_system.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WeightConfig {
    public static final double EXAM1_WEIGHT = 0.10;
    public static final double EXAM2_WEIGHT = 0.10;
    public static final double EXAM3_WEIGHT = 0.80;

    public static final double PHYSICS_WEIGHT = 0.40;
    public static final double CHEMISTRY_WEIGHT = 0.30;
    public static final double BIOLOGY_WEIGHT = 0.30;
}