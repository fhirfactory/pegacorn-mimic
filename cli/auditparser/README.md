# ReadMe for auditEventComparator

## parseLokiLogsForMLLP

### Log Level

Firstly, set your preferred DEBUG/INFO printing level via:

```
export ROOT_LOG_LEVEL=INFO
```

If you don't specify anything, it will default to TRACE or DEBUG and 
result in quite a bit of information being printed on the screen. _Too much 
information for normal use._

### Command Invocation

The commandline is invoked via 

```
java -jar target/pegacorn-mimic-cli-auditparser-1.5.0-SNAPSHOT-jar-with-dependencies.jar parseLokiLogsForMLLP \
    --ingresSystemLogDirectory=<directory for ingres loki system logs> \
    --ingresPort=<Ingres Port Number> \
    --ingresAuditEventDirectory=<director for ingres audit-event file-set> \
    --egressSystemLogDirectory=<directory for egress loki system logs>  \
    --ingresSystemName=<ingres system name> \
    --outputDirectory=<output directory>
```

### Notes

When you run it... _please make sure you have the same time-ranges of
log-files & audit-files for the ingres subsystem directories, and any logs from 
the lower-time-range through to present in the egress subsystem directory._