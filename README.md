EyeNetra Lensometer
===================

This is a modified Lensometer App for the EyeNetra Netrometer to be used by 
students and technologists wishing to understand how the lensometry technology 
deployed by EyeNetra works.  

This application has not been clinically tested, approved by or registered in any health agency. 
Even though this repository grants licenses to use to any person that follow it's license, any 
clinical or commercial use must additionally follow the laws and regulations of the pertinent
jurisdictions. Having a license to use the source code does not imply on having regulatory 
approvals to market any part of this code.

# Development Overview

## Prerequisites

You will need the following things properly installed on your computer:
* Android SDK (min API 21)
* Android Studio
* Git

## Building
Build the app:
```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Installing on device
```bash
./gradlew installDebug
```

## Deploying

1. Generate a new signing key
```
keytool -genkey -v -keystore <my-release-key.keystore> -alias <alias_name> -keyalg RSA -keysize 2048 -validity 10000
openssl base64 < <my-release-key.keystore> | tr -d '\n' | tee some_signing_key.jks.base64.txt
```
2. Create four Secret Key variables on your GitHub repository and fill in the signing key information
    - `KEY_ALIAS` <- `<alias_name>`
    - `KEY_PASSWORD` <- `<your password>`
    - `KEY_STORE_PASSWORD` <- `<your key store password>`
    - `SIGNING_KEY` <- the data from `<my-release-key.keystore>`
3. Change the `versionCode` and `versionName` on `app/build.gradle`
4. Commit and push.
5. Tag the commit with `v{x.x.x}`
6. Let the [Create Release GitHub Action](https://github.com/vitorpamplona/netrometer/actions/workflows/create-release.yml) build a new `apk` file.
7. Add your CHANGE LOG to the description of the new release

# Contributing

[Issues](https://github.com/vitorpamplona/netrometer/issues) and [pull requests](https://github.com/vitorpamplona/netrometer/pulls) are very welcome.

# Regulatory Notice

The source code and any packaged version of them have not been cleared, certified, or 
otherwise approved by the Food and Drug Administration (FDA), the National Institute 
for Occupational Safety and Health (NIOSH), or any equivalent national regulatory agency. 
Contributors do not offer any warranty or assume any liability for the use of the source code. 
Projects should only be used if cleared by the FDA or relevant regulatory agency. Nothing 
hosted on or linked to from this site is recommended for use in a clinical setting.

You should consult regulatory guidance and any other applicable laws or regulations 
regarding any use of this source code.

# Notice To Manufacturers

You are responsible for any equipment you manufacture or sell utilizing any materials 
hosted by or linked to from this repository. You are also responsible for any federal 
or state regulatory requirements that apply to the manufacturing or sales of products 
intended for medical use, and are responsible for informing health care providers to which 
the product is supplied that they are responsible for decisions regarding appropriate 
personal protective equipment for their personnel. 

# License

Copyright (C) 2024 Vitor Pamplona

This program is offered under a commercial and under the AGPL license.
For commercial licensing, contact me at vitor@vitorpamplona.com.
For AGPL licensing, see below.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.