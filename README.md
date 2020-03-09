![](https://img.shields.io/badge/License-EPL%202.0-red.svg)
![](https://travis-ci.org/j-barata/mms-capella.svg?branch=master)

<img align="right" width="160" height="110" src="docs/openmbee-capella.png" alt="OpenMBEE"/>

# OpenMBEE MMS Connector for Capella

This connector integrates the Open Source [Capella](https://www.eclipse.org/capella) MBSE solution <sup>[\[1\]](#References)</sup> into the [OpenMBEE](https://www.openmbee.org) engineering environment & ecosystem <sup>[\[2\]](#References)</sup>.

## Capabilities

- [x] Push data from Capella to an MMS Repository
- [x] Pull data from an MMS Repository to Capella 
- [x] Manage versions and branches
- [x] Supports MMS 3.x <sup>[\[3\]](#References)</sup>
- [x] Supports Capella 1.3.x <sup>[\[4\]](#References)</sup>

![MMS Connector](docs/mms-connector-capella.png)

## Installation Guide

The installation guide can be found [here](docs/installation-guide.md).

## User Quick Start Guide

The user guide is provided by this add-on and accessible directly within Capella (see menu **Help Content**).

## Developer Quick Start Guide

The developer guide can be found [here](docs/developer-guide.md#_prepare_eclipse_development_environment).

## References

\[1\] : [https://github.com/eclipse/capella](https://github.com/eclipse/capella)

\[2\] : [https://github.com/Open-MBEE](https://github.com/Open-MBEE)

\[3\] : Support for MMS 4 is currently under development, a specific branch has been created for that

\[4\] : Although the connector has been fully tested with Capella 1.3.x only, it might work with other major versions of Capella (if it doesn't, please let us known and open an [issue](https://github.com/open-mbee/mms-capella/issues))
