package com.numberoverzero.snippets.dwhello.health;

import com.codahale.metrics.health.HealthCheck;

public class ShallowHealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        System.out.println("Shallow health check.");
        return Result.healthy();
    }
}
