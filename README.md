EasyBeans
=========

Pay for your office coffee capsules with PayPal - built for [HACKED.io](http://hacked.io/almanac/paypal/)

#Application Architecture

This app solves a simple but important problem encountered by office workers that have a cash tin to pay for capsules for a shared coffee machine where normal vending isn't suitable (primarily due to the quality of the coffee).

The main aim of this app is to make
it effortless for office workers to buy coffee capsules without having to leave
a box of cash sat next to their coffee machine for long periods of time.

At the coffee station there will be several buckets or boxes with capsules in them
for each of the different flavours of coffee available. Each bucket will have an
NFC tag beside it and all the user needs to do to purchase a capsule is pass their
device over the tag and pick out the capsule.

In the same way that conventional office coffee systems are based on honour codes
this application also assumes users will be honest about the capsules they take.
This is because the main aim of this app was to remove the need to have a cash box
around the office all the time.

To denote the various flavours available we store the following URIs on the tags:

    http://nfc.danhagon.com/[flavour]

##Usability

To ensure that once launched our app won't be launched again once the tag is touched again
we use [Foreground Dispatch](https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html#foreground-dispatch)

#Technical Requirements

This app will aim to use the most common type of NFC tag with NDEF data which is the
most easily applied format for the Android SDK. Specifically
it was tested using NFC Forum Type 2 Tags (NTAG203) with 137 bytes of data available.
In testing the tags were not locked but in a practical application this might be
necessary. Testing was done using a Nexus 7 with preconfigured tags, written using
the [NXP Tagwriter App](https://play.google.com/store/apps/details?id=com.nxp.nfc.tagwriter&hl=en)

To process payments we use the [PayPal SDK](https://developer.paypal.com/webapps/developer/docs/integration/mobile/android-integration-guide/)

For licensing of the [PayPal SDK](https://github.com/paypal/PayPal-Android-SDK/)
please see `PayPal_SDK_acknowledgements.md` and `PayPal_SDK_LICENSE.md`

#License

Copyright (c) 2013 Dan Hagon.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.