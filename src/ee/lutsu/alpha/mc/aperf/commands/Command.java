package ee.lutsu.alpha.mc.aperf.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String syntax();
    String description();
    String permission() default "";
    boolean isPrimary() default false;
    boolean isPlayerOnly() default false;
}