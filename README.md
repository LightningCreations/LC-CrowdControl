# LC-CrowdControl
Java Library to provide CrowdControl support to games. 

The library provides a core api (needed at compile time), and a series of connectors (needed at runtime). 

Currently the connectors support an augmented version of the CrowdControl TCP v1 Protocol. When details are available, TCP v2 will be supported. 

On all provided connectors, Effect discovery is provided for compatible servers. 

Upon Connection, a Discovery Compatible Pack can send Discovery Compatible Games a `__DISCOVER` effect using the Test type. 
The client can then respond with either a Retry, or a Success where the supported effects are provided in the message.  Upon Retry, the request may be resubmitted (message is unused, and MUST be ignored). 
