
## PROPAGATION
```
* REQUIRED (default) - if TRX not exists - creates new TRX
* REQUIRES_NEW - always creates new TRX
* MANDATORY - TRX must exist, if not exists - exception
* NESTED - create save point, rolback to save point, it works like REQUIRED
* SUPPORTS - if TRX exists use existing TRX, if TRX not exists go without TRX
* NOT_SUPPORTED - if TRX exists - suspends and go without TRX
* NEVER - TRX must not exist, if TRX exists - exception
```

## ISOLATION