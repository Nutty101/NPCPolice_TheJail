package net.livecar.nuttyworks.thejail.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();

    String group();

    String badArgumentsMessage();

    String helpMessage();

    String permission();

    String[] arguments();

    boolean allowConsole();

    int minArguments();

    int maxArguments();
}
