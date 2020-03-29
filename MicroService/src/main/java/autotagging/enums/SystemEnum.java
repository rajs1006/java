package de.funkedigital.autotagging.enums;

/**
 * We save files in 2 systems
 * --> In normal execution : in Database
 * --> If normal executions fails: in FileSystem
 * {@link de.funkedigital.autotagging.interceptors.FailSafeInterceptor}
 */
public enum SystemEnum {

    // Takes or executes repositories
    Database,

    // Takes or executes file system
    FileSystem;
}
