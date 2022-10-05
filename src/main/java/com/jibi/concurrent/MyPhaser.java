package com.jibi.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

public class MyPhaser extends Phaser {

    private static final int THRESHOLD = 65535;
    List<Phaser> childPhasers = new ArrayList<>();

    public int register() {
        if (getRegisteredParties() >= THRESHOLD) {
            Optional<Phaser> optionalChildPhaser = childPhasers.stream().filter(phaser -> phaser.getRegisteredParties() < THRESHOLD).findFirst();
            if (optionalChildPhaser.isPresent()) {
                Phaser existingFreePhaser = optionalChildPhaser.get();
                return existingFreePhaser.register();
            } else {
                Phaser newChildPhaser = new Phaser(this);
                childPhasers.add(newChildPhaser);
                return newChildPhaser.register();
            }
        } else {
            return super.register();
        }
    }

    public int getRegisteredParties() {
        AtomicInteger registeredParties = new AtomicInteger(super.getRegisteredParties());
        childPhasers.stream().forEach(phaser -> registeredParties.addAndGet(phaser.getRegisteredParties()));
        return registeredParties.get();
    }

    public int arriveAndDeregister() {
        return super.arriveAndDeregister();
    }

}
