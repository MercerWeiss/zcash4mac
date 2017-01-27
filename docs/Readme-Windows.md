# TODO: Unfinished
# [ZCash](https://z.cash/)[®](#disclaimer) Desktop GUI Wallet - for Windows

ZCash for Windows is not yet officially supported. 
Thanks to [@radix42](https://github.com/radix42) an experimental build has been provided.
Before installing the GUI wallet on Windows you need to install ZCash on Windows.

1. Installing ZCash on Windows

   The easiest way to install it is to use the binary version provided by [@radix42](https://github.com/radix42):

   1.1. Download this file [ZCash-v1.0.5.zip](http://TODO:)

   1.2. Unzip the file so that the executables `zcashd.exe` and `zcash-cli.exe` are in one directory.

2. Installing the ZCash Desktop GUI wallet

   You may use the [latest binary release](https://github.com/vaklinov/zcash-swing-wallet-ui/releases/latest).
   Download file [ZCashSwingWalletUI.jar](https://github.com/vaklinov/zcash-swing-wallet-ui/releases/download/0.54-beta/ZCashSwingWalletUI.jar)
   and place it in the same folder as `zcashd.exe` and `zcash-cli.exe`. The result needs to be similar to:
![Screenshot](https://github.com/vaklinov/zcash-swing-wallet-ui/raw/master/docs/ZCashWalletWinDir.png "ZCash dir on Windows")

4. Running the installed ZCash GUI wallet

   Before running the GUI you need to start zcashd (e.g. `zcashd --daemon`). The wallet GUI is a Java program packaged 
   as an executable JAR file. It may be run from command line or started from another GUI tool (e.g. file manager). 
   One way to run it from command line is:
   ```
   java -jar ZCashSwingWalletUI.jar
   ```
   You may instead just use Windows the file manager and double-click on the `ZCashSwingWalletUI.jar`. 
   This will start the ZCash GUI wallet.

### Donations accepted
At the present time this project is non-commercial in nature and developed by volunteers. If you find the GUI
Wallet useful, please consider making a donation for its further development. Your contribution matters! Donations 
are accepted at ZCash T address:
```
t1UMGjLDipdfuCdNwxUZTV4FhM34FJXgM8r
```

### License
This program is distributed under an [MIT License](https://github.com/vaklinov/zcash-swing-wallet-ui/raw/master/LICENSE).

### Disclaimer
This program is not officially endorsed by or associated with the ZCash project and the ZCash company.
[ZCash®](https://trademarks.justia.com/871/93/zcash-87193130.html) and the 
[ZCash® logo](https://trademarks.justia.com/868/84/z-86884549.html) are trademarks of the
[Zerocoin Electric Coin Company](https://trademarks.justia.com/owners/zerocoin-electric-coin-company-3232749/).

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

### Known issues and limitations

1. Limitation: Wallet encryption has been temporarily disabled in ZCash due to stability problems. A corresponding issue 
[#1552](https://github.com/zcash/zcash/issues/1552) has been opened by the ZCash developers. Correspondingly
wallet encryption has been temporarily disabled in the ZCash Desktop GUI Wallet.
1. Issue: the GUI wallet does not work correctly if zcashd is started with a custom data directory, like:
`zcashd -datadir=/home/data/whatever` This will be fixed in later versions.
1. Issue: GUI data tables (transactions/addresses etc.) allow copying of data via double click but also allow editing. 
The latter needs to be disabled. 
1. Limitation: The list of transactions does not show all outgoing ones (specifically outgoing Z address 
transactions). A corresponding issue [#1438](https://github.com/zcash/zcash/issues/1438) has been opened 
for the ZCash developers. 
