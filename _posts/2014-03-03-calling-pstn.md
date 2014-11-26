---
category: calling
title: 'Phone/PSTN Calls'
layout: nil
---

### Start a Phone Call

Bit6 interconnects with the phone networks (PSTN) and allows making outgoing phone calls.

Phone numbers must be in [E164](http://en.wikipedia.org/wiki/E.164) format, prefixed with `+`. So a US (country code `1`) number `(555) 123-1234` must be presented as `+15551231234`.

Note, that for the demo purposes you can only make 1 minute free phone calls to the US and Canada numbers.

```java
RtcDialog dialog = bit6.startPhoneCall("+15551231234");
dialog.launchInCallActivity(this);
```  
