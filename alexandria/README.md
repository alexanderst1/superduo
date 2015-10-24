# Alexandria

##### Notes for code reviewer:

1. Alexandria has barcode scanning functionality which does not require the installation of a separate app on first use.
   * Barcode scanner functionality (folders `/BarcodeScanner`) uses project "zxing" (https://github.com/zxing/zxing)
     * UI is taken from `/zxing/android` and source code not relevant for _EAN10/13_ bar code format scanning is removed 
	      (added corresponding comment in the beginning of every source file)
     * Jar files (folder `/libs`) are used from http://repo1.maven.org/maven2/com/google/zxing:
       - `/android-core/3.2.1/android-core-3.2.1.jar`, 
       - `/core/3.2.1/core-3.2.1.jar`
2. The two errors mentioned in the UX review are handled. Extra error cases are found, accounted for, and called out in code comments. 
   * Please search for "AlexSt" over source code to find comments about fixed issues
